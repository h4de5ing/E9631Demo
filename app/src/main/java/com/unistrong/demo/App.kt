package com.unistrong.demo

import android.app.Application

class App : Application() {
    companion object {
        var aECUType: Byte = 0xDF.toByte() //选中的ECU  E8 E9 需要 - 0x08
    }
}