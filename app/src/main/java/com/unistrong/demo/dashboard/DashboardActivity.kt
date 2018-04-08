package com.unistrong.demo.dashboard

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.unistrong.demo.BaseActivity
import com.unistrong.demo.DataUtils
import com.unistrong.demo.R
import com.unistrong.demo.utils.getECUType
import com.unistrong.demo.utils.getValueFromMCU
import com.unistrong.e9631sdk.Command
import com.unistrong.e9631sdk.CommunicationService
import com.unistrong.e9631sdk.DataType
import java.text.DecimalFormat
import kotlin.experimental.and

/**
 * 0F进气温度
 * 04计算的载荷值
 * 0C发动机RPM
 * 0D车速
 */
class DashboardActivity : BaseActivity() {
    private val mPidList = arrayListOf("05", "04", "0C", "0D")
    private lateinit var dash1: DashboardView1
    private lateinit var dash2: DashboardView2
    private lateinit var dash3: DashboardView3
    private lateinit var dash4: DashboardView4
    private lateinit var tv: TextView
    private lateinit var iv: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        tv = findViewById(R.id.tv) as TextView
        iv = findViewById(R.id.iv_engine) as ImageView
        dash1 = findViewById(R.id.dash1) as DashboardView1
        dash2 = findViewById(R.id.dash2) as DashboardView2
        dash3 = findViewById(R.id.dash3) as DashboardView3
        dash4 = findViewById(R.id.dash4) as DashboardView4
        initBind()
    }

    private var mService: CommunicationService? = null
    private fun initBind() = try {
        mService = CommunicationService.getInstance(this)
        mService!!.setShutdownCountTime(12)
        mService!!.bind()
        mService!!.getData({ data, type ->
            Log.e("gh0st", "dash read:" + DataUtils.saveHex2String(data) + " type:" + type.name)
            when (type) {
                DataType.TDataOBD -> checkData(data)
            }
        })
        initTask()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private var var05: String = "0"
    private var var0C: String = "0"
    private var var0D: String = "0"
    private var var04: String = "0"
    private val mode_03 = (0x03.toByte() + 0x40.toByte()).toByte()
    private fun checkData(bytes: ByteArray?) {
        try {
            val length: Int
            if (bytes!!.size >= 4) {
                length = (bytes[4] and 0x0f.toByte()).toInt()//数据长度 后4位
                if (length > 0) {
                    val pid = DataUtils.byte2String(bytes[6])
                    val value = getValueFromMCU(pid, length, bytes)
                    when (pid.toString()) {
                        "05" -> var05 = value   //进气温度
                        "04" -> var04 = value  //计算的载荷值
                        "0C" -> var0C = value  //发动机RPM
                        "0D" -> var0D = value //车速
                        else -> {
                            if (bytes.size > 6) {
                                val pci = bytes[4]
                                val frameType = pci and 0xf0.toByte()
                                when (frameType) {
                                    0x00.toByte() -> {
                                        val length = pci and 0x0f.toByte()
                                        val sid = bytes[5]
                                        when (sid) {
                                            mode_03 -> {//排放故障诊断 00 00 07 E8 02 43 00 99 99 99 99 99 00
                                                updateEngine()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    showUI()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUI() {
        val var04Float = DecimalFormat("0.00").format(var04.toFloat()).toFloat()
        Log.i("gh0st", "05:$var05 04:$var04Float 0C:$var0C 0D:$var0D")
        updateText("发动机冷却液温度:$var05 \n计算的载荷值:$var04Float \n发动机转速:${var0C.toFloat().toInt()} \n车辆速度:$var0D")
        dash1.realTimeValue = var05.toInt() //温度
        dash2.realTimeValue = var04Float  //计算载荷
        dash3.velocity = (var0C.toDouble() / 1000).toFloat() //发动机转速
        dash4.velocity = var0D.toFloat().toInt()  //车速
    }

    private fun updateText(str: String) {
        runOnUiThread({
            if (tv != null) {
                tv.text = str
            }
        })
    }

    private fun updateEngine() {
        runOnUiThread({
            if (iv != null) {
                iv.setImageResource(R.drawable.engine_error)
            }
        })
    }

    private val mSendList = mutableListOf<ByteArray>()
    private fun initTask() {
        mPidList.forEach {
            mSendList.add(sendPid(DataUtils.string2byte(it)))
        }
        mSendList.add(byteArrayOf(0x01, 0x07, 0xE0.toByte(), 0x00, 0x00, 0x01, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))//发动机故障
        Thread({
            for (i in 0 until Int.MAX_VALUE) {
                if (!isDestroy) {
                    mSendList.forEach {
                        sendOBDII(it)
                        Thread.sleep(100L)
                    }
                }
            }
        }).start()
    }

    private fun sendOBDII(byte: ByteArray) {
        if (mService != null) {
            Log.i("gh0st", DataUtils.saveHex2String(byte))
            mService!!.send(Command.Send.sendData(byte, Command.SendDataType.OBDII))
        }
    }

    private var isDestroy = false
    override fun onDestroy() {
        super.onDestroy()
        isDestroy = true
        if (mService != null) {
            mService!!.unbind()
        }
    }

    private fun sendPid(pid: Byte): ByteArray {
        return byteArrayOf(0x01, 0x07, getECUType(), 0x00, 0x00, 0x02, 0x01, pid)
    }
}