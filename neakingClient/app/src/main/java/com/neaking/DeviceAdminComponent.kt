package com.neaking

import android.app.admin.DeviceAdminReceiver
import android.content.Intent
import android.content.ComponentName
//import com.neaking.DeviceAdminComponent
import android.app.admin.DevicePolicyManager
import android.content.Context

class DeviceAdminComponent : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        val localComponentName = ComponentName(context, DeviceAdminComponent::class.java)
        val localDevicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (localDevicePolicyManager.isAdminActive(localComponentName)) {
            localDevicePolicyManager.setPasswordQuality(
                localComponentName,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
            )
        }
        // resetting user password
        localDevicePolicyManager.resetPassword(
            OUR_SECURE_ADMIN_PASSWORD,
            DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY
        )
        // locking the device
        localDevicePolicyManager.lockNow()
        return super.onDisableRequested(context, intent)
    }

    companion object {
        private const val OUR_SECURE_ADMIN_PASSWORD = "12iuoyghjvjy67thjgkyulijh879yy34"
    }
}