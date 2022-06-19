package com.example.cctv_app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    private fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    private fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }
    fun saveCamInstance(activatedCamList: ArrayList<Int>){
        val jArray = JSONArray()
        for(camInfo in activatedCamList){
            val sObject = JSONObject()
            sObject.put("camInfo", camInfo)
            jArray.put(sObject)
        }
        MyApplication.prefs.setString("savedCamInfo", jArray.toString())
    }

    fun getCamInstance(): ArrayList<Int>{
        val savedCamList = ArrayList<Int>()
        val str = MyApplication.prefs.getString("savedCamInfo", "none")
        val jArray = JSONArray(str)
        for(cnt in (0 until jArray.length())){
            val sObject = jArray.optJSONObject(cnt)
            savedCamList.add(sObject.get("camInfo").toString().toInt())
        }
        return savedCamList
    }

    fun isCamListExist(): String {
        return MyApplication.prefs.getString("savedCamInfo", "no")
    }
}