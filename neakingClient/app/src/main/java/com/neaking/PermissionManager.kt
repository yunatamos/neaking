package com.neaking

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.neaking.ConnectionManager.context
import org.json.JSONArray
import org.json.JSONObject

object PermissionManager {
    fun getGrantedPermissions(): JSONObject {
            val data = JSONObject()
            try {
                val perms = JSONArray()
                val pi: PackageInfo? =
                    context?.getPackageName()
                        ?.let { context?.getPackageManager()?.getPackageInfo(it, PackageManager.GET_PERMISSIONS) }
                if (pi != null) {
                    for (i in pi.requestedPermissions.indices) {
                        if (pi.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) perms.put(
                            pi.requestedPermissions[i]
                        )
                    }
                }
                data.put("permissions", perms)
            } catch (e: Exception) {
            }
            return data
        }


    fun canIUse(perm: String?): Boolean {
        return if (context!!.packageManager.checkPermission(
                perm!!,
                context!!.packageName
            ) === PackageManager.PERMISSION_GRANTED
        ) true else false
    }

}