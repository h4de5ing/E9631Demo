package com.unistrong.uarttest.uart2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.unistrong.uarttest.BaseIO
import com.unistrong.uarttest.Log
import com.unistrong.uarttest.R
import com.unistrong.uarttest.uart2.SerialPortIO.findAllTtysDevices
import com.unistrong.uarttest.utils.J1939Utils
import com.unistrong.uarttest.utils.OperationUtils
import com.unistrong.uarttest.utils.SPUtils
import com.unistrong.uarttest.utils.SpannableStringUtils
import kotlinx.coroutines.experimental.async
import java.io.*

class Uart2Activity : AppCompatActivity() {
    private lateinit var mSpName: Spinner
    private lateinit var mSpBaud: Spinner
    private lateinit var mEtData: EditText
    private lateinit var mEtNumber: EditText
    private lateinit var mEtCycle: EditText
    private lateinit var mTvResult: TextView
    private lateinit var mBtnClear: ImageButton
    private lateinit var mBtnCleanCount: Button
    private lateinit var mBtn485: Button
    private lateinit var mBtnSend: Button
    private lateinit var mTvCount: TextView
    private lateinit var mCbDatainc: CheckBox
    private lateinit var mCbHex: CheckBox
    private lateinit var mCbNotShow: CheckBox
    private var mIsHex = true
    private var mIsShow = true
    private var mIsDatainc = true
    private var mIsOpen = false
    private var mSendCycle = 50L
    private var mSendCount = 1L
    private var mCountSend = 0L
    private var mCountReceived = 0L
    private var intBaud = 115200
    private var intName = 0
    private var intBaudPosition = 1
    private val mBHandler = Handler {
        when (it.what) {
            0 -> update2UIMain("send", it.obj.toString())
            1 -> update2UIMain("received", it.obj.toString())
            else -> {
            }
        }
        true
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uart)

        mTvResult = findViewById(R.id.tv_result)
        mBtnClear = findViewById(R.id.btn_clear)
        mEtData = findViewById(R.id.sendEditText)
        mBtnSend = findViewById(R.id.sendButton)
        mEtNumber = findViewById(R.id.et_number)
        mEtCycle = findViewById(R.id.et_send_cycle)
        mTvCount = findViewById(R.id.tv_count)
        mBtn485 = findViewById(R.id.btn_485)
        mBtnCleanCount = findViewById(R.id.btn_clean_count)
        mCbDatainc = findViewById(R.id.cb_datainc)
        mCbHex = findViewById(R.id.cb_hex)
        mCbNotShow = findViewById(R.id.cb_not_show)
        mSpName = findViewById(R.id.sp_name)
        mSpBaud = findViewById(R.id.sp_baud)
        mBtn485.setOnClickListener {
            val status485 = get485()
            if ("1" == status485) writeFile("0") else writeFile("1")
            mBHandler.postDelayed({
                mBtn485.text = if (get485() == "1") "485(写)" else "485(读)"
            }, 300)
        }
        mBtnSend.setOnClickListener {
            val cycle = mEtCycle.text.toString().toInt()
            if (cycle < 50) {
                mEtCycle.error = "串口发送周期必须大于50ms"
            } else {
                if ("停止" == mBtnSend.text) {
                    mBtnSend.text = "发送"
                    isSendEnd = true
                } else {
                    mBtnSend.text = "停止"
                    isSendEnd = false
                    sendData()
                }
            }
            //val name = mSpName.getItemAtPosition(intName)
            //if ("ttyS7" == name) sendTtys7() else
        }
        mBtnClear.setOnClickListener { mTvResult.text = "" }
        mBtnCleanCount.setOnClickListener {
            mCountSend = 0
            mCountReceived = 0
            mTvCount.text = "s:$mCountSend r:$mCountReceived"
        }
        mCbDatainc.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            mIsDatainc = b
        }
        mCbHex.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            mIsHex = b
        }
        mCbNotShow.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            mIsShow = b
        }
        intBaudPosition = SPUtils.getSp(this@Uart2Activity, "baudPosition", 1) as Int
        intName = SPUtils.getSp(this@Uart2Activity, "namePosition", 0) as Int
        val devices = findAllTtysDevices()
        val spDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, /*resources.getStringArray(R.array.uart_name)*/devices)
        val spUartAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.uart_baud))
        mSpName.adapter = spDevicesAdapter
        mSpBaud.adapter = spUartAdapter
        mSpName.setSelection(intName)
        mSpBaud.setSelection(intBaudPosition)
        mSpName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                intName = position//parent!!.getItemAtPosition(position).toString().toInt()
                SPUtils.setSP(this@Uart2Activity, "namePosition", intName)
            }

        }
        mSpBaud.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                intBaud = parent!!.getItemAtPosition(position).toString().toInt()
                intBaudPosition = position
                SPUtils.setSP(this@Uart2Activity, "baudPosition", intBaudPosition)
            }
        }
    }

    private fun sendData() {
        val sendCount = mEtNumber.text.toString()
        val sendCycle = mEtCycle.text.toString()
        val dataString = mEtData.text.toString()
        mSendCycle = if (sendCycle.isEmpty()) 250 else sendCycle.toLong()
        if (sendCount.isEmpty()) mEtNumber.error = "请输入发送数量" else {
            if (dataString.isEmpty()) mEtData.error = "请输入串口数据" else {
                mSendCount = sendCount.toLong()
                mIsHex = mCbHex.isChecked //hex显示
                mIsShow = mCbNotShow.isChecked  //是否回显
                mIsDatainc = mCbDatainc.isChecked //是否数据递增
                sendInThread(dataString)
            }
        }
    }

    var isSendEnd = false
    @SuppressLint("SetTextI18n")
    private fun sendInThread(data: String) {
        //Thread {
        async {
            (0 until mSendCount).forEach {
                if(isSendEnd){
                    return@async
                }
                val sendData = if (mIsDatainc) OperationUtils.hexAddUI(data, it.toInt()) else data
                val sendString = if (mIsHex) J1939Utils.saveHex2String(sendData.toByteArray()) else sendData
                mCountSend += sendData.length
                updateSend2UI(sendString)
                runOnUiThread {
                    mTvCount.text = "s:$mCountSend r:$mCountReceived"
                }
                write(data = sendData.toByteArray())
                if (it == mSendCount) {
                    mBtnSend.text = "发送"
                    isSendEnd = true
                }
                Thread.sleep(mSendCycle)
            }
            mBtnSend.text = "发送"
            isSendEnd = true
        }
        //}.start()
    }

    private fun updateSend2UI(s: String) {
        val msg = Message()
        msg.what = 0
        msg.obj = s
        mBHandler.sendMessage(msg)
    }

    private fun updateReceived2UI(s: String) {
        val msg = Message()
        msg.what = 1
        msg.obj = s
        mBHandler.sendMessage(msg)
    }

    private fun write(data: ByteArray) {
        SerialPortIO.write(BaseIO.Packet(data))
    }

    @SuppressLint("SetTextI18n")
    private fun update2UIMain(send: String, s: String) {
        if (mIsShow) {
            val sp = SpannableStringUtils.getBuilder(send)
                    .setForegroundColor(Color.parseColor("#9933cc"))
                    .append(J1939Utils.getCurrentDateTimeString())
                    .setForegroundColor(Color.parseColor("#ff33b5e5"))
                    .append(s)
                    .setForegroundColor(Color.RED)
                    .append("\n")
                    .create()
            mTvResult.append(sp)
            var offset = mTvResult.lineCount * mTvResult.lineHeight - mTvResult.height
            if (offset > 6000) {
                mTvResult.text = ""
            }
            if (offset < 0) {
                offset = 0
            }
            mTvResult.scrollTo(0, offset)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uart_control, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_open) {
            if (!mIsOpen) {
                val name = mSpName.getItemAtPosition(intName)
                try {
                    SerialPortIO.start("/dev/$name", intBaud) { buffer, size ->
                        Log.d("gh0st ${J1939Utils.saveHex2String(buffer)}")
                        val data = ByteArray(size)
                        System.arraycopy(buffer, 0, data, 0, size)
                        mCountReceived += data.size
                        runOnUiThread {
                            mTvCount.text = "s:$mCountSend r:$mCountReceived"
                        }
                        if (mIsHex) updateReceived2UI(J1939Utils.saveHex2String(data)) else updateReceived2UI(String(data))
                    }
                    mIsOpen = true
                    item.title = getString(R.string.close)
                    mTvResult.append("open success $name $intBaud\n")
                    mBtnSend.isEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    mTvResult.append("exception: $e")
                    mBtnSend.isEnabled = false
                }
            } else {
                item.title = getString(R.string.open)
                mTvResult.append("close success \n")
                mIsOpen = false
                mBtnSend.isEnabled = false
                SerialPortIO.stop()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun writeFile(string: String) {
        val f = File("/sys/class/misc/sunxi-gps/rf-ctrl/max485_state")
        val b = BufferedWriter(FileWriter(f))
        b.write(string)
        b.flush()
        b.close()
    }

    private fun get485(): String {
        val f = File("/sys/class/misc/sunxi-gps/rf-ctrl/max485_state")
        return BufferedReader(FileReader(f)).readLine()
    }

    override fun onDestroy() {
        super.onDestroy()
        SerialPortIO.stop()
    }
}
