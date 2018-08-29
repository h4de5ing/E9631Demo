package com.unistrong.uarttest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.unistrong.uarttest.utils.*
import com.van.uart.LastError
import com.van.uart.UartManager


class UartActivity : BaseActivity() {
    private val TAG: String = "gh0st"
    private lateinit var mSpName: Spinner
    private lateinit var mSpBaud: Spinner
    private lateinit var mEtData: EditText
    private lateinit var mEtNumber: EditText
    private lateinit var mEtCycle: EditText
    private lateinit var mTvResult: TextView
    private lateinit var mBtnClear: ImageButton
    private lateinit var mBtnCleanCount: Button
    private lateinit var mBtnSend: Button
    private lateinit var mTvCount: TextView
    private lateinit var mCbDatainc: CheckBox
    private lateinit var mCbHex: CheckBox
    private lateinit var mCbNotShow: CheckBox
    private var mIsHex = true
    private var mIsShow = true
    private var mIsDatainc = true
    private var mSendCycle = 50L
    private var mSendCount = 1L
    private var mCountSend = 0L
    private var mCountReceived = 0L
    private var manager: UartManager? = null
    private var devices: Array<String>? = null
    private var intBaud = 115200
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
        mBtnCleanCount = findViewById(R.id.btn_clean_count)
        mCbDatainc = findViewById(R.id.cb_datainc)
        mCbHex = findViewById(R.id.cb_hex)
        mCbNotShow = findViewById(R.id.cb_not_show)
        mSpName = findViewById(R.id.sp_name)
        mSpBaud = findViewById(R.id.sp_baud)
        mBtnSend.setOnClickListener({ sendData() })
        mBtnClear.setOnClickListener({ mTvResult.text = "" })
        mBtnCleanCount.setOnClickListener({
            mCountSend = 0
            mCountReceived = 0
            mTvCount.text = "s:$mCountSend r:$mCountReceived"
        })
        mCbDatainc.setOnCheckedChangeListener({ _: CompoundButton, b: Boolean ->
            mIsDatainc = b
        })
        mCbHex.setOnCheckedChangeListener({ _: CompoundButton, b: Boolean ->
            mIsHex = b
        })
        mCbNotShow.setOnCheckedChangeListener({ _: CompoundButton, b: Boolean ->
            mIsShow = b
        })
        manager = UartManager()
        intBaudPosition = SPUtils.getSp(this@UartActivity, "baudPosition", 1) as Int
        val spDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.uart_name))
        val spUartAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.uart_baud))
        mSpName.adapter = spDevicesAdapter
        mSpBaud.adapter = spUartAdapter
        mSpBaud.setSelection(intBaudPosition)
        mSpBaud.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                intBaud = parent!!.getItemAtPosition(position).toString().toInt()
                intBaudPosition = position
                SPUtils.setSP(this@UartActivity, "baudPosition", intBaudPosition)
            }
        }
        if (mainHandler == null) {
            mainHandler = Handler(Looper.getMainLooper())
        }
    }

    private val readThread = ReadThread()
    private var mainHandler: Handler? = null
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

    private fun sendInThread(data: String) {
        Thread {
            (0 until mSendCount).forEach {
                val sendData = if (mIsDatainc) OperationUtils.hexAddUI(data, it.toInt()) else data
                val sendString = if (mIsHex) J1939Utils.saveHex2String(sendData.toByteArray()) else sendData
                mCountSend += sendData.length
                updateSend2UI(sendString)
                write(data = sendData.toByteArray())
                Thread.sleep(mSendCycle)
            }
        }.start()
    }

    private fun write(data: ByteArray) {
        if (manager != null) {
            Log.e(TAG, "write: ${J1939Utils.saveHex2String(data)}")
            manager!!.write(data, data.size)
        }
    }

    private inner class ReadThread : Runnable {
        private var thread: Thread? = null
        fun startMonitor() {
            stopMonitor()
            thread = Thread(this)
            thread!!.isDaemon = true
            thread!!.start()
        }

        fun stopMonitor() {
            if (thread != null && thread!!.isAlive) {
                try {
                    thread!!.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            thread = null
        }

        override fun run() {
            val recv = ByteArray(2048)
            if (manager != null) {
                while (manager!!.isOpen) {
                    var length = 0
                    try {
                        length = manager!!.read(recv, recv.size, 50, 1)
                        if (length > 1) {
                            val data = ByteArray(length)
                            System.arraycopy(recv, 0, data, 0, length)
                            Log.e(TAG, "read: " + J1939Utils.saveHex2String(data))
                            mCountReceived += data.size
                            runOnUiThread({
                                mTvCount.text = "s:$mCountSend r:$mCountReceived"
                            })
                            if (mIsShow) {
                                if (mIsHex) updateReceived2UI(J1939Utils.saveHex2String(data)) else updateReceived2UI(String(data))
                            }
                        }
                    } catch (e: LastError) {
                        e.printStackTrace()
                    }
                }
            }
        }
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
            try {
                if (manager != null) {
                    if (!manager!!.isOpen) {//打开
                        manager!!.open(mSpName.selectedItem.toString(), BaudUtils.getBaudRate(intBaud))
                        item.title = getString(R.string.close)
                        mTvResult.append("open success ${mSpName.selectedItem} ${BaudUtils.getBaudRate(intBaud)} \n")
                        mBtnSend.isEnabled = true
                        readThread.startMonitor()
                    } else {//关闭
                        manager!!.close()
                        readThread.stopMonitor()
                        item.title = getString(R.string.open)
                        mTvResult.append("close success \n")
                    }
                }
            } catch (e: LastError) {
                mTvResult.append(" open failed :$e\n")
                e.printStackTrace()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (manager != null) {
            manager!!.close()
        }
    }
}
