package com.unistrong.e9631sdk

open class KtUtils {
    //无符号右移 bit位
    open fun ushr3(id: ByteArray, bit: Int): ByteArray {
        val byte0 = J1939Utils.byte2int(id[0])
        val byte1 = J1939Utils.byte2int(id[1])
        val byte2 = J1939Utils.byte2int(id[2])
        val byte3 = J1939Utils.byte2int(id[3])
        val count = ((byte0 and 0xff) shl 24) or ((byte1 and 0xff) shl 16) or ((byte2 and 0xff) shl 8) or (byte3 and 0xff)
        var ushrInt = count ushr bit
        val zeroString = "00000000000000000000000000000000"
        var ushrBinary = Integer.toBinaryString(ushrInt)
        if (ushrBinary.length < 32) {
            ushrBinary = zeroString.substring(0, 32 - ushrBinary.length) + ushrBinary
        }
        for (i in id.indices) {
            id[i] = J1939Utils.string2byte(J1939Utils.getHex(ushrBinary.substring(i * 8, (i + 1) * 8)))
        }
        return id
    }

    open fun handleJ1939(bytes: ByteArray): ByteArray {
        val id = ByteArray(4)
        System.arraycopy(bytes, 1, id, 0, id.size)
        val byte0 = J1939Utils.byte2int(id[0])
        val byte1 = J1939Utils.byte2int(id[1])
        val byte2 = J1939Utils.byte2int(id[2])
        val byte3 = J1939Utils.byte2int(id[3])
        val count = ((byte0 and 0xff) shl 24) or ((byte1 and 0xff) shl 16) or ((byte2 and 0xff) shl 8) or (byte3 and 0xff)
        var ushrInt = count ushr 3
        val zeroString = "00000000000000000000000000000000"
        var ushrBinary = Integer.toBinaryString(ushrInt)
        if (ushrBinary.length < 32) {
            ushrBinary = zeroString.substring(0, 32 - ushrBinary.length) + ushrBinary
        }
        for (i in id.indices) {
            id[i] = J1939Utils.string2byte(J1939Utils.getHex(ushrBinary.substring(i * 8, (i + 1) * 8)))
        }
        return id
    }
}