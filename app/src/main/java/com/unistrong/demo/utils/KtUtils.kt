package com.unistrong.demo.utils

class KtUtils {

    //无符号右移bit位
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
    /**
     * ID
     *
     * 占用1个字节，其余填写0
     * 优先级3位  000  ~ 111  缺省值 3-011
     * 保留位1位 0
     * 数据页1位    0 当前使用     1 将来使用
     *
     * 00   20    40    60    80     A0    C0    E0
     *
     * pgn 的保留字节 1个字节
     *
     *PF格式 占用1个字节
     * 如果发送PGN请求报文，那么PF的值 J1939_PF_REQUEST
     * 如果不是PGN请求报文，那么PF的值有可能是其他的值
     * 234 -> EA
     *
     *
     * PS return 目的地址 1 字节
     * PS有两种含义
     * 1.如果pf的值小于等于239 那么ps的含义就是目的地址 ds
     * 2.如果pf的值大于239 那么ps的含义就是群扩展 取pgn的第三个字节
     *
     * 数据域
     * PGN占3个字节
     * 第一个字节0x00:没用,pgn没有达到这个范围
     * 59904 00EA00  请求参数群
     *
     * //src 源地址 1字节
     * 发送pgn  dlc只能是3
     * 数据域 用户输入的data
     * 第一个字节 00
     * 第二个字节 EA  PF
     * 第三个字节：输入的 ds  或则  群扩展
     * 第四个字节
     * 剩下的字节  数据域
     */



    fun J1939TranslateIdExt(srcString: String, priString: String, psString: String): Int {
        val pf = 234 //PDUFormat  hex(EA)=dec(234)
        val pri = J1939Utils.string2byte(priString).toInt()
        val ps = J1939Utils.string2byte(psString).toInt()
        val src = Integer.valueOf(srcString, 16) and 0xff
        var id_Ext = pri shl 26
        id_Ext = ((id_Ext shr 16) or pf) shl 16
        id_Ext = ((id_Ext shr 8) or ps) shl 8
        return id_Ext or src
    }
}