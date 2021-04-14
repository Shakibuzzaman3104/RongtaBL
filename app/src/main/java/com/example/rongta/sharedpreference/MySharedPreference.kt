package com.example.rongta.sharedpreference

import android.content.Context
import android.content.SharedPreferences



class MySharedPreference private constructor(context: Context) {


    init {
        sharedPreferences = context.applicationContext.getSharedPreferences("SharedPreferenceName", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()
        editor?.apply()
    }

    companion object {
        private var myPreferences: MySharedPreference? = null
        private var sharedPreferences: SharedPreferences?=null
        private var editor: SharedPreferences.Editor?=null

        fun getPreferencesInstance(context: Context): MySharedPreference {
            if (myPreferences == null) myPreferences = MySharedPreference(context)
            return myPreferences!!
        }
    }

    fun setPrinter(printer:String?)
    {
        editor?.putString("printer", printer)
        editor?.apply()
    }

    fun getPrinter(): String? {
        return sharedPreferences?.getString("printer", null)
    }



}
