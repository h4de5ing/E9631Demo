package com.unistrong.uarttest

/**
 * Created by gh0st on 2017/12/25.
 * 协议模式的枚举
 */
enum class DataModeType(val keyName: Byte) {
    ModeCommand(0x00), ModeJ9139(0x01), ModeOBD(0x02), ModeCAN(0x03), ModeUNKNOW(0x09)
}