package com.unistrong.uarttest

import android.os.Bundle

class SettingActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().add(android.R.id.content, SettingsPreferenceFragment()).commit()
    }

}
