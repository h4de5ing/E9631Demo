package com.unistrong.e9631sdk


enum class DataModeType(val keyName: Byte) {
    ModeCommand(0x00), ModeJ9139(0x01), ModeOBD(0x02), ModeCAN(0x03), ModeUNKNOW(0x09)
}