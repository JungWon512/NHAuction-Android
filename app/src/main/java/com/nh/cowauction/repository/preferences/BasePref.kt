package com.nh.cowauction.repository.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Description : Base Preferences
 *
 * Created by hmju on 2021-05-18
 */
interface BasePref {
    fun getValue(key: String, defValue: String): String
    fun getValue(key: String, defValue: Int): Int
    fun getValue(key: String, defValue: Boolean): Boolean
    fun getValue(key: String, defValue: Long): Long
    fun setValue(key: String, value: String)
    fun setValue(key: String, value: Int)
    fun setValue(key: String, value: Boolean)
    fun setValue(key: String, value: Long)
}

class BasePrefImpl @Inject constructor(@ApplicationContext context: Context) : BasePref {

    private val pref: SharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

    override fun getValue(key: String, defValue: String) = pref.getString(key, defValue) ?: defValue

    override fun getValue(key: String, defValue: Int) = pref.getInt(key, defValue)

    override fun getValue(key: String, defValue: Boolean) = pref.getBoolean(key, defValue)

    override fun getValue(key: String, defValue: Long) = pref.getLong(key, defValue)

    override fun setValue(key: String, value: String) {
        pref.setValue(key, value)
    }

    override fun setValue(key: String, value: Int) {
        pref.setValue(key, value)
    }

    override fun setValue(key: String, value: Boolean) {
        pref.setValue(key, value)
    }

    override fun setValue(key: String, value: Long) {
        pref.setValue(key, value)
    }

    inline fun <reified T> SharedPreferences.setValue(key: String, value: T) {
        with(edit()) {
            when (value) {
                is String -> putString(key, value)
                is Long -> putLong(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                else -> return@with
            }
            apply()
        }
    }
}