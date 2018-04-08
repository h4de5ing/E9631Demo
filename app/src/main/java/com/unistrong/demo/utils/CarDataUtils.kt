package com.unistrong.demo.utils

import com.unistrong.demo.App
import com.unistrong.demo.DataUtils

class CarDataUtils {
    companion object {
        val split = "#"  //字符串拼接符号 separator
    }
}

fun getECUType(): Byte {
    return App.aECUType
}

/**
 * 从mcu返回的数据取出值，用于UI显示
 * 00 00 07 E9 06 41 01 00 07 61 00
 * iso文档Annex B 115页
 */
fun getValueFromMCU(pid: String, valueLength: Int, data: ByteArray): String {
    var value = ""
    print("${DataUtils.saveHex2String(data)}--- pid:[$pid]--- valueLength:[$valueLength] data:[")
    if (valueLength >= 3) {
        val bytes = ByteArray(valueLength - 2)//存储数据部分
        System.arraycopy(data, data.size - valueLength + 2, bytes, 0, bytes.size)
        bytes.forEach { print(DataUtils.byte2String(it)) }
        when (pid) {
            "01" -> {//01-故障清除之后的监视器状态  00 07 61 00
                value = pid01Value(bytes)
            }
            "03" -> {//03-燃油系统状态  02  00
                value = pid03Value(bytes)
            }
            "04" -> {//04-计算的载荷值
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "05" -> {//05-发动机冷却液温度
                value = (DataUtils.byte2int(bytes[0]) - 40).toString()
            }
            "06" -> {//06-短时燃油修正-Bank 1
                value = ((DataUtils.byte2int(bytes[0]) - 128) * 100.0 / 128.0).toString()
            }
            "07" -> {//07-长时燃油修正-Bank 1
                value = ((DataUtils.byte2int(bytes[0]) - 128) * 100.0 / 128.0).toString()
            }
            "0B" -> {//0B-进气歧管绝对压力
                value = bytes[0].toString()
            }
            "0C" -> {//0C-发动机RPM
                value = if (bytes.size == 2) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) / 4).toString()
                } else {
                    "0."
                }
            }
            "0D" -> {//0D-车辆速度
                value = DataUtils.byte2int(bytes[0]).toString()
            }
            "0E" -> {//0E-1号气缸点火提前角
                value = (DataUtils.byte2int(bytes[0]) / 2.0 - 64.0).toString()
            }
            "0F" -> {//0F-进气温度
                value = (DataUtils.byte2int(bytes[0]) - 40).toString()
            }
            "10" -> {//10-空气流速
                value = if (bytes.size == 2) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) / 100.0).toString()
                } else {
                    "0."
                }
            }
            "11" -> {//11-节气门绝对位置
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "1C" -> {//1C-对和梁设计的OBD要求
                value = "EOBD:" + bytes[0].toString()
            }
            "1F" -> {//1F-发动机点火后运行时间
                value = if (bytes.size == 2) {
                    (DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])).toString()
                } else {
                    "0."
                }
            }
            "21" -> {//21-MIL亮起后的行驶距离
                value = if (bytes.size == 2) {
                    (DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])).toString()
                } else {
                    "0."
                }
            }
            "2C" -> {//2C-指令的EGR
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "2E" -> {//2E-指令的燃油蒸汽排出
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "30" -> {//30-故障代码清空后热车次数
                value = bytes[0].toString()
            }
            "31" -> {
                value = if (bytes.size == 2) {
                    (DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])).toString()
                } else {
                    "0."
                }
            }
            "33" -> {//33-气压
                value = bytes[0].toString()
            }
            "34" -> {//34-Bank 1 -传感器 1  包含2个值
                value = if (bytes.size == 4) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) / 32768.0).toString() + "#" + ((DataUtils.byte2int(bytes[2]) * 256 + DataUtils.byte2int(bytes[3])) / 256.0 - 128).toString()
                } else {
                    "0."
                }
            }
            "3C" -> {//3C-催化器温度Bank 1,传感器1
                value = if (bytes.size == 2) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) / 10 - 40).toString()
                } else {
                    "0."
                }
            }
            "41" -> {//41-监测此次驾驶循环状态
                value = pid41Value(bytes)
            }
            "42" -> {//42-控制模块电压
                value = if (bytes.size == 2) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) / 1000.0).toString()
                } else {
                    "0."
                }
            }
            "43" -> {//43-绝对载荷值
                value = if (bytes.size == 2) {
                    ((DataUtils.byte2int(bytes[0]) * 256.0 + DataUtils.byte2int(bytes[1])) * 100.0 / 255.0).toString()
                } else {
                    "0."
                }
            }
            "45" -> {//45-节气门相对位置
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "47" -> {//47-油门踏板位置 B
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "49" -> {//49-油门踏板位置 D
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "4A" -> {//4A-油门踏板位置 E
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
            "4C" -> {//4C-指令的节气门传动装置控制
                value = (DataUtils.byte2int(bytes[0]) * 100.0 / 255.0).toString()
            }
        }
        println("]   value:$value")
    } else {
        value = "<3"
    }
    return value
}

/**
 * @param data 8个字节
 * @return value
 * 计算方法
 * 参考j1939 71
 *
 * 类型一：解析数据(十六进制转换十进制) * 分辨率 + 偏移量
 *
 * 类型二：直接取值
 */
private val notParse = "暂时没有解析数据"

fun getValueFromMcu(data: ByteArray, pgn: Int): String {
    var value = notParse
    when (pgn) {
        65244 -> {
            if (data.size == 8) {
                val byte14 = DataUtils.cutByteArray(data, 0, 4)
                val data1 = byteArray2Int(byte14) * 0.5f
                val byte58 = DataUtils.cutByteArray(data, 5, 8)
                value = "空转燃料消耗:$data1-消耗时间:V"
            }
        }
        65245 -> {
        }
        65246 -> {
        }
        65247 -> {
        }
        65248 -> {
        }
        65249 -> {
        }
        65250 -> {
        }
        65251 -> {
        }
        65252 -> {

        }
        65253 -> {
        }
        65254 -> {
        }
        65255 -> {
        }
        65256 -> {
        }
        65257 -> {
        }
        65258 -> {
        }
        65259 -> {
        }
        65260 -> {
        }
        65261 -> {
        }
        65262 -> {
        }
        65263 -> {
        }
        65264 -> {
        }
        65265 -> {
        }
        65266 -> {
        }
        65267 -> {
        }
        65268 -> {
        }
        65269 -> {
        }
        65270 -> {
        }
        65271 -> {
        }
        65272 -> {
        }
        65273 -> {
        }
        65274 -> {
        }
        65275 -> {
        }
        65276 -> {
        }
        65277 -> {
        }
        65278 -> {
        }
        65279 -> {
        }
        65243 -> {
        }
        65242 -> {
        }
        65241 -> {
        }
        65237 -> {
        }
        65223 -> {
        }
        65221 -> {
        }
        65219 -> {
        }
        65218 -> {
        }
        65217 -> {
        }
        65216 -> {
        }
        65215 -> {
        }
        65214 -> {
        }
        65213 -> {
        }
        65212 -> {
        }
        65211 -> {
        }
        65210 -> {
        }
        else -> {
            value = notParse
        }
    }
    return value
}

fun byteArray2Int(byteArray: ByteArray): Int {
    var value = 0
    byteArray.forEach {
        value = value.shl(8)
        value = value or it.toInt().and(0xff)
    }
    return value
}

/**
 * 4 个字节 00 07 61 00
 *
 * 第一字节的0-6 位转换10进制
 * 最大3F
 */
fun pid01Value(data: ByteArray): String {
    var pid01Value = "unknow#unknow"
    if (data.size == 4) {
        val binValues = byteArray2BooleanArray(data)
        val sb = StringBuilder()
        val ctcCnt = (0 until 6)
                .filter { binValues[it] }
                .sumByDouble { Math.pow(2.0, 5.0 - it) }
        sb.append("DTC_CNT:$ctcCnt\n")
        binValues.forEachIndexed { index, b ->
            when (index) {
            //第一个字节
                7 -> if (b) sb.append("MIL ON\n") else sb.append("MIL OFF\n")
            //第二个字节
                8 -> if (b) sb.append("MIS_SUP YES\n") else sb.append("MIS_SUP NO\n")
                9 -> if (b) sb.append("FUEL_SUP YES\n") else sb.append("FUEL_SUP NO\n")
                10 -> if (b) sb.append("CCM_SUP YES\n") else sb.append("CCM_SUP NO\n")
                12 -> if (b) sb.append("MIS_RDY NO\n") else sb.append("MIS_RDY YES\n")
                13 -> if (b) sb.append("FUEL_RDY NO\n") else sb.append("FUEL_RDY YES\n")
                14 -> if (b) sb.append("CCM_RDY NO\n") else sb.append("CCM_RDY YES\n")
            //第三个字节
                16 -> if (b) sb.append("CAT_SUP YES\n") else sb.append("CAT_SUP NO\n")
                17 -> if (b) sb.append("HCAT_SUP YES\n") else sb.append("HCAT_SUP NO\n")
                18 -> if (b) sb.append("EVAP_SUP YES\n") else sb.append("EVAP_SUP NO\n")
                19 -> if (b) sb.append("AIR_SUP YES\n") else sb.append("AIR_SUP NO\n")
                20 -> if (b) sb.append("ACRF_SUP YES\n") else sb.append("ACRF_SUP NO\n")
                21 -> if (b) sb.append("O2S_SUP YES\n") else sb.append("O2S_SUP NO\n")
                22 -> if (b) sb.append("HTR_SUP YES\n") else sb.append("HTR_SUP NO\n")
                23 -> if (b) sb.append("EGR_SUP YES\n") else sb.append("EGR_SUP NO\n")
            //第四个字节
                24 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                25 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                26 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                27 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                28 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                29 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                30 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
                31 -> if (b) sb.append("EGR_RDY NO\n") else sb.append("EGR_RDY YES\n")
            }
        }
        pid01Value = sb.toString()
    }
    return pid01Value
}

/**
 * 2 个字节 02  00
 */
fun pid03Value(data: ByteArray): String {
    var pid03Value = "unknow#unknow"
    if (data.size == 2) {
        val sb1 = StringBuilder()
        val sb2 = StringBuilder()
        val binValues = byteArray2BooleanArray(data)
        binValues.forEachIndexed { index, b ->
            when (index) {
                0 -> if (b) sb1.append("OL\n")
                1 -> if (b) sb1.append("CL\n")
                2 -> if (b) sb1.append("OL-Drive\n")
                3 -> if (b) sb1.append("OL-Fault\n")
                4 -> if (b) sb1.append("Cl-Fault\n")
                8 -> if (b) sb2.append("OL\n")
                9 -> if (b) sb2.append("CL\n")
                10 -> if (b) sb2.append("OL-Drive\n")
                11 -> if (b) sb2.append("OL-Fault\n")
                12 -> if (b) sb2.append("Cl-Fault\n")
            }
        }
        pid03Value = "$sb1${CarDataUtils.split}$sb2"
    }
    return pid03Value
}

/**
 * 4 个字节 00 07 61 00
 * 41 03 02 00
 * 41 03 F8 F8
 */
fun pid41Value(data: ByteArray): String {
    var pid41Value = "unknow#unknow"
    if (data.size == 4) {
        val sb = StringBuilder()
        val binValues = byteArray2BooleanArray(data)
        binValues.forEachIndexed { index, b ->
            when (index) {
            //第一个字节不解析
            //第二个字节
                8 -> if (b) sb.append("MIS_ENA YES#") else sb.append("MIS_ENA NO#")
                9 -> if (b) sb.append("FUEL_ENA YES#") else sb.append("FUEL_ENA NO#")
                10 -> if (b) sb.append("CCM_ENA YES#") else sb.append("CCM_ENA NO#")

                12 -> if (b) sb.append("MIS_CMPL NO#") else sb.append("MIS_CMPL YES#")
                13 -> if (b) sb.append("FUEL_CMPL NO#") else sb.append("FUEL_CMPL YES#")
                14 -> if (b) sb.append("CCM_CMPL NO#") else sb.append("CCM_CMPL YES#")

            //第三个字节
                16 -> if (b) sb.append("CAT_ENA YES#") else sb.append("CAT_ENA NO#")
                17 -> if (b) sb.append("HCAT_ENA YES#") else sb.append("HCAT_ENA NO#")
                18 -> if (b) sb.append("EVAP_ENA YES#") else sb.append("EVAP_ENA NO#")
                19 -> if (b) sb.append("AIR_ENA YES#") else sb.append("AIR_ENA NO#")
                20 -> if (b) sb.append("ACRF_ENA YES#") else sb.append("ACRF_ENA NO#")
                21 -> if (b) sb.append("O2S_ENA YES#") else sb.append("O2S_ENA NO#")
                22 -> if (b) sb.append("HTR_ENA YES#") else sb.append("HTR_ENA NO#")
                23 -> if (b) sb.append("EGR_ENA YES#") else sb.append("EGR_ENA NO#")
            //第四个字节
                24 -> if (b) sb.append("CAT_CMPL NO#") else sb.append("CAT_CMPL YES#")
                25 -> if (b) sb.append("HCAT_CMPL NO#") else sb.append("HCAT_CMPL YES#")
                26 -> if (b) sb.append("EVAP_CMPL NO#") else sb.append("EVAP_CMPL YES#")
                27 -> if (b) sb.append("AIR_CMPL NO#") else sb.append("AIR_CMPL YES#")
                28 -> if (b) sb.append("ACRF_CMPL NO#") else sb.append("ACRF_CMPL YES#")
                29 -> if (b) sb.append("O2S_CMPL NO#") else sb.append("O2S_CMPL YES#")
                30 -> if (b) sb.append("HTR_CMPL NO#") else sb.append("HTR_CMPL YES#")
                31 -> if (b) sb.append("EGR_CMPL NO#") else sb.append("EGR_CMPL YES#")
            }
        }
        pid41Value = sb.toString()
    }
    return pid41Value
}

/**
 * 将byte 字节转换成2进制数组
 * 02 00
 * 0000 0010 0000 0000
 */
fun byteArray2BooleanArray(data: ByteArray): BooleanArray {
    val binValues = BooleanArray(data.size * 8)
    for ((ii, i) in (0..(data.size - 1)).withIndex()) {
        val value = data[i]
        var tmp = value.toInt()
        for (x in 0..7) {
            binValues[(ii * 8) + (7 - x)] = (tmp and 1) == 1
            tmp = tmp ushr 1
        }
    }
    return binValues
}