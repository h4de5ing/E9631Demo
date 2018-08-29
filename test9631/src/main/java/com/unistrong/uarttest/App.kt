package com.unistrong.uarttest

import android.app.Application

/**
 * Created by John on 2018/2/5.
 */
class App : Application() {
    companion object {
        lateinit var app: App
        var mode: Byte = 0x09
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}