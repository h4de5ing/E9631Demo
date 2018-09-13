package com.unistrong.uarttest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import com.unistrong.e9631sdk.Command
import com.unistrong.e9631sdk.CommunicationService
import com.unistrong.e9631sdk.DataType
import com.unistrong.uarttest.utils.J1939Utils
import com.unistrong.uarttest.utils.OperationUtils
import com.unistrong.uarttest.utils.SpannableStringUtils
import kotlinx.coroutines.experimental.async
import java.nio.charset.Charset

class CanActivity : BaseActivity() {
    private val TAG: String = "gh0st"
    private var mService: CommunicationService? = null
    private lateinit var mSpFormat: Spinner
    private lateinit var mSpType: Spinner
    private lateinit var mSpBaud: Spinner
    private lateinit var mSpChannel: Spinner
    private lateinit var mEtId: EditText
    private lateinit var mEtData: EditText
    private lateinit var mEtNumber: EditText
    private lateinit var mEtCycle: EditText
    private lateinit var mBtnSend: Button
    private lateinit var mBtnClear: ImageButton
    private lateinit var mBtnCleanCount: Button
    private lateinit var mTvResult: TextView
    private lateinit var mTvCount: TextView
    private lateinit var mCbNotShow: CheckBox
    private lateinit var mIDInc: CheckBox
    private lateinit var mDataInc: CheckBox
    private var mSendCycle = 250L
    private var mSendCount = 1L
    private var mCountSend = 0L
    private var mCountReceived = 0L
    private var mError = 0L
    private var mIsShow = false
    private var mIsAccoff = false
    private var mIDinc = false
    private var mDatainc = false
    private var mInitList = mutableListOf<ByteArray>()

    private val mBHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_can)
        //initBind()
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        mSpFormat = findViewById(R.id.sp_format)
        mSpType = findViewById<Spinner>(R.id.sp_type)
        mSpBaud = findViewById<Spinner>(R.id.sp_baud)
        mSpChannel = findViewById<Spinner>(R.id.sp_channel)
        mEtId = findViewById<EditText>(R.id.et_id)
        mEtData = findViewById<EditText>(R.id.et_data)
        mEtNumber = findViewById<EditText>(R.id.et_number)
        mEtCycle = findViewById<EditText>(R.id.et_send_cycle)
        mBtnSend = findViewById<Button>(R.id.btn_send)
        mBtnClear = findViewById<ImageButton>(R.id.btn_clear)
        mBtnCleanCount = findViewById<Button>(R.id.btn_clean_count)
        mTvResult = findViewById(R.id.tv_result)
        mTvCount = findViewById(R.id.tv_count)
        mIDInc = findViewById(R.id.cb_id_inc)
        mDataInc = findViewById(R.id.cb_data_inc)
        mCbNotShow = findViewById<CheckBox>(R.id.cb_not_show)
        mTvResult.movementMethod = ScrollingMovementMethod.getInstance()
        val spFormatAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.can_format))
        val spTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.can_type))
        val spBaudAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.can_baud))
        mSpFormat.adapter = spFormatAdapter
        mSpType.adapter = spTypeAdapter
        mSpBaud.adapter = spBaudAdapter
        mSpChannel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.can_channel))
        mEtId.addTextChangedListener(CustomTextWatcher(mEtId))
        mEtData.addTextChangedListener(CustomTextWatcher(mEtData))
        mBtnSend.setOnClickListener {
            if ("停止" == mBtnSend.text) {
                mBtnSend.text = "发送"
                isSendEnd = true
            } else {
                mBtnSend.text = "停止"
                isSendEnd = false
                sendData()
            }
        }
        mBtnClear.setOnClickListener { mTvResult.text = "" }
        mCbNotShow.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            mIsShow = b
        }
        mBtnCleanCount.setOnClickListener {
            mError = 0
            mCountSend = 0
            mCountReceived = 0
            mTvCount.text = "s:$mCountSend r:$mCountReceived e:$mError"
        }
        mIDInc.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            mIDinc = isChecked
        }
        mDataInc.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            mDatainc = isChecked
        }
        mInitList.add(Command.Send.Version())
        mInitList.add(Command.Send.SearchChannel())
        mInitList.add(Command.Send.ModeCan())
        mInitList.add(Command.Send.Switch500K())
        mSpChannel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> write2Activity(Command.Send.Channel1())
                    1 -> write2Activity(Command.Send.Channel2())
                }
            }
        }
        mSpBaud.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.i(TAG, "onItemSelected ${parent!!.getItemAtPosition(position)}")
                when (position) {
                    0 -> write2Activity(Command.Send.Switch500K())
                    1 -> write2Activity(Command.Send.Switch250K())
                    2 -> write2Activity(Command.Send.Switch125K())
                }
            }
        }
        findViewById<Button>(R.id.btn_version).setOnClickListener {
            write2Activity(Command.Send.Version())
        }
    }

    private fun sendInit() {
        mBHandler.postDelayed({
            Thread {
                mInitList.forEach {
                    write2Activity(it)
                    Thread.sleep(1000)
                }
            }.start()
        }, 2000)
    }

    private fun sendData() {
        val sendCount = mEtNumber.text.toString()
        val sendCycle = mEtCycle.text.toString()
        val idString = mEtId.text.toString()
        val dataString = mEtData.text.toString()
        mSendCycle = if (sendCycle.isEmpty()) 250 else sendCycle.toLong()
        if (sendCount.isEmpty()) mEtNumber.error = "请输入发送数量" else {
            if (idString.isEmpty()) mEtId.error = "请输入Can ID" else {
                if (dataString.isEmpty()) mEtData.error = "请输入Can Data" else {
                    var idStr = idString.replace(" ", "")
                    if (idStr.length % 2 != 0) {
                        idStr = idStr.substring(0, idStr.length - 1) + "0" + idStr.substring(idStr.length - 1, idStr.length)
                    }
                    val len = idStr.length / 2
                    var id = 0
                    for (i in 0 until len) {
                        id = id shl 8
                        id = id or (Integer.valueOf(idStr.substring(i * 2, i * 2 + 2), 16) and 0xff)
                    }
                    mSendCount = sendCount.toLong()
                    sendInThread(idStr, dataString)
                }
            }
        }
    }

    var isSendEnd = false
    private fun sendInThread(idStr: String, dataString: String) {
        val data = J1939Utils.int2bytes2(dataString)
        val idData = J1939Utils.int2bytes2(idStr)
        val format = mSpFormat.selectedItemPosition
        val type = mSpType.selectedItemPosition
        async {
            (0 until mSendCount).forEach {
                if (isSendEnd) {
                    return@async
                }
                mCountSend++
                val ds = dataString.replace(" ", "")
                val sendData = if (mDatainc) OperationUtils.hexAddByteArray(ds, it.toInt()) else data
                try {
                    //if (!mIsAccoff) {//add off 不再发送数据
                    writeCan2Activity(format, type, idData, sendData)
                    updateCount()
                    //}
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (it == mSendCount) {
                    mBtnSend.text = "发送"
                    isSendEnd = true
                }
                Thread.sleep(mSendCycle)
            }
            mBtnSend.text = "发送"
            isSendEnd = true
        }
    }

    private fun writeCan2Activity(frameFormat: Int, frameType: Int, id: ByteArray, data: ByteArray) {
        if (mService != null) {
            if (mService!!.isBindSuccess) {
                mService!!.sendCan(frameFormat, frameType, id, data)
            }
        }
    }

    private fun write2Activity(byteArray: ByteArray) {
        if (mService != null) {
            if (mService!!.isBindSuccess) {
                mService!!.send(byteArray)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun update2UIMain(send: String, s: String) {
        async {
            runOnUiThread {
                if (!mIsShow) {
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
                updateCount()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCount() {
        async {
            runOnUiThread {
                mTvCount.text = "s:$mCountSend r:$mCountReceived e:$mError"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initBind()
        if (mService != null) {
            if (!mService!!.isBindSuccess) {
                mService!!.bind()
                Log.i("gh0st", "绑定")
                sendInit()
            }
        }
    }

    private fun initBind() = try {
        mService = CommunicationService.getInstance(this)
        mService!!.setShutdownCountTime(12)
        mService!!.bind()
        mService!!.getData { data, type ->
            when (type) {
                DataType.TAccOn -> {
                    mIsAccoff = false
                }
                DataType.TAccOff -> {
                    mIsAccoff = true
                }
                DataType.TDataCan -> {
                    mCountReceived++
                    updateCount()
                    update2UIMain("rev:", J1939Utils.saveHex2String(data))
                }
                DataType.TChannel -> update2UIMain("tips:", " channel ${J1939Utils.byte2String(data[0])}")
                DataType.TDataMode -> update2UIMain("tips:", " dataMode ${getMode(data[0])}")
                DataType.TCan250 -> update2UIMain("tips:", " set can 250k success")
                DataType.TCan500 -> update2UIMain("tips:", " set can 500k success")
                DataType.TMcuVersion -> update2UIMain("tips:", " Mcu Version: ${String(data, Charset.defaultCharset())}")
                DataType.TUnknow -> mError++
                else -> {
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            mService!!.unbind()
            Log.i("gh0st", "解除绑定")
        }
    }

    private fun getMode(byte: Byte): String {
        var modeString = ""
        when (byte) {
            0x00.toByte() -> modeString = "Command mode"
            0x01.toByte() -> modeString = "J1939 mode"
            0x02.toByte() -> modeString = "OBD mode"
            0x03.toByte() -> modeString = "Can mode"
        }
        return modeString
    }
}
