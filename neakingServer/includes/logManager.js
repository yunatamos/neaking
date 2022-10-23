const db = require('./databaseGateway');
const LogsData =require("./../models/Logs")

const log = async (type, message,userName) => {
    console.log(type.name, message);
    await LogsData.updateOne(
        { user:  userName },
        {
             $push:{logs:{type,message,time:new Date()}} ,
            //  $setOnInsert: dataOfClientUpdate
        },
        { upsert: true }
    )
}

module.exports = {
    log,
    getLogs: async (userName) => {
        let logs=await LogsData.findOne({user:userName}).sort({ time: 1 }).lean()
        if(logs){
            logs= logs.logs
        }else {
            logs=[]
        }
       return logs;
      //  return db.maindb.get('admin.logs').sortBy('time').reverse().value();
    }
}
