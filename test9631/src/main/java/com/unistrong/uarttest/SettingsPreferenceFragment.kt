package com.unistrong.uarttest

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment


/**
 * Created by gh0st on 2017/9/26.
 */

class SettingsPreferenceFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private lateinit var mETSendCycle: EditTextPreference
    private lateinit var mETSendCount: EditTextPreference
    private lateinit var mCBHex: CheckBoxPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        mETSendCycle = findPreference("edittext_preference_send_cycle") as EditTextPreference
        mETSendCount = findPreference("edittext_preference_send_count") as EditTextPreference
        mCBHex = findPreference("checkbox_preference_hex") as CheckBoxPreference
        mETSendCycle.onPreferenceChangeListener = this
        mETSendCount.onPreferenceChangeListener = this
        mCBHex.onPreferenceChangeListener = this
    }

    /**
     * 条目值改变事件
     */
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val key = preference.key
        when (key) {
            "edittext_preference_send_cycle" -> {
            }
            "edittext_preference_send_count" -> {
            }
            "checkbox_preference_hex" -> {
            }
        }
        return false
    }
}
