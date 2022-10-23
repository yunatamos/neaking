package com.neaking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.ScanResult
import org.json.JSONObject
import org.json.JSONArray
import android.net.wifi.WifiManager
import android.util.Log

object WifiScanner {
    @SuppressLint("MissingPermission")
    fun scan(context: Context): JSONObject? {
        return try {
            val dRet = JSONObject()
            val jSONArray = JSONArray()
            val wifiManager = context.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager.isWifiEnabled) {

                if (PermissionManager.canIUse(WIFI_SERVICE)){
                    val scanResults: List<*>? = wifiManager.scanResults
                    if (scanResults != null && scanResults.size > 0) {
                        var i = 0
                        while (i < scanResults.size && i < 10) {
                            val scanResult = scanResults[i] as ScanResult
                            val jSONObject = JSONObject()
                            jSONObject.put("BSSID", scanResult.BSSID)
                            jSONObject.put("SSID", scanResult.SSID)
                            jSONArray.put(jSONObject)
                            i++
                        }
                        dRet.put("networks", jSONArray)
                        return dRet
                    }
                }

            }
            dRet
        } catch (th: Throwable) {
            Log.e("MtaSDK", "isWifiNet error", th)
            null
        }
    }
}