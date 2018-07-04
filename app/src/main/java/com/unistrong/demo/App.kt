package com.unistrong.demo

import android.app.Application
import android.util.Log
import android.widget.Toast

class App : Application() {
    companion object {
        lateinit var app: App
        var aECUType: Byte = 0xDF.toByte() //选中的ECU  E8 E9 需要 - 0x08
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.i("gh0st", "onLowMemory")
        //System.gc()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.i("gh0st", "onTrimMemory：$level")
    }
}