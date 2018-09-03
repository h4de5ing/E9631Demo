package com.unistrong.uarttest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import com.unistrong.e9631sdk.Command
import com.unistrong.e9631sdk.CommunicationService
import com.unistrong.e9631sdk.DataType
import com.unistrong.uarttest.utils.J1939Utils
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
    private var mSendCycle = 250L
    private var mSendCount = 1L
    private var mCountSend = 0L
    private var mCountReceived = 0L
    private var mIsShow = false
    private var mInitList = mutableListOf<ByteArray>()

    private val mBHandler = Handler {
        when (it.what) {
            0 -> update2UIMain("send", it.obj.toString())
            1 -> update2UIMain("received", it.obj.toString())
            else -> {
            }
        }
        true
    }

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
        mEtId = findViewById(R.id.et_id)
        mEtData = findViewById<EditText>(R.id.et_data)
        mEtNumber = findViewById<EditText>(R.id.et_number)
        mEtCycle = findViewById<EditText>(R.id.et_send_cycle)
        mBtnSend = findViewById<Button>(R.id.btn_send)
        mBtnClear = findViewById<ImageButton>(R.id.btn_clear)
        mBtnCleanCount = findViewById<Button>(R.id.btn_clean_count)
        mTvResult = findViewById(R.id.tv_result)
        mTvCount = findViewById(R.id.tv_count)
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
            mCountSend = 0
            mCountReceived = 0
            mTvCount.text = "s:$mCountSend r:$mCountReceived"
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
            Log.e("gh0st", "TMcuVersion")
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
                    var idStr = "01" + idString.replace(" ", "")
                    if (idStr.length % 2 != 0) {
                        idStr = idStr.substring(0, idStr.length - 1) + "0" + idStr.substring(idStr.length - 1, idStr.length)
                    }
                    val len = idStr.length / 2
                    var id = 0
                    for (i in 0 until len) {
                        id = id shl 8
                        id = id or (Integer.valueOf(idStr.substring(i * 2, i * 2 + 2), 16) and 0xff)
                    }
                    val data = J1939Utils.int2bytes2(dataString)
                    val idData = J1939Utils.int2bytes2(idStr)
                    val intFrameType = J1939Utils.getFrameType(idData)
                    val intIdType = J1939Utils.getFrameFormat(idData)
                    mSendCount = sendCount.toLong()
                    sendInThread(idData, data)
                    /*           if (mSpType.selectedItemPosition != intFrameType || mSpFormat.selectedItemPosition != intIdType) {
                                   if (mSpType.selectedItemPosition == 0) {//数据帧
                                       if (mSpFormat.selectedItemPosition == 0) {//标准帧
                                           updateSend2UI("请输入正确的标准数据帧id")
                                       } else {
                                           updateSend2UI("请输入正确的扩展数据帧id")
                                       }
                                   } else {//远程帧
                                       if (mSpFormat.selectedItemPosition == 0) {//标准帧
                                           updateSend2UI("请输入正确的标准远程帧id")
                                       } else {
                                           updateSend2UI("请输入正确的扩展远程帧id")
                                       }
                                   }
                               } else {
                                   //循环发送
                                   mSendCount = sendCount.toLong()
                                   sendInThread(idData, data)
                               }*/
                }
            }
        }
    }

    var isSendEnd = false
    private fun sendInThread(idData: ByteArray, data: ByteArray) {
        //Thread {
        async {
            (0 until mSendCount).forEach {
                if (isSendEnd) {
                    return@async
                }
                mCountSend++
                val idcandata = J1939Utils.byteArrayAddByteArray(idData, data)
                updateSend2UI(J1939Utils.saveHex2String(idcandata))
                write2Activity(Command.Send.sendData(idcandata, Command.SendDataType.Can))
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

    private fun write2Activity(byteArray: ByteArray) {
        if (mService != null) {
            if (mService!!.isBindSuccess) {
                Log.e(TAG, "write " + J1939Utils.saveHex2String(byteArray))
                mService!!.send(byteArray)
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
        mTvCount.text = "s:$mCountSend r:$mCountReceived"
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
            Log.e("gh0st", "read:" + J1939Utils.saveHex2String(data) + " type:" + type.name)
            when (type) {
                DataType.TAccOn -> {
                    //记录日志
                    J1939Utils.saveDataInfo2File("acc on")
                }
                DataType.TAccOff -> {
                    //记录日志
                    J1939Utils.saveDataInfo2File("acc off")
                }
                DataType.TDataCan -> {
                    mCountReceived++
                    updateReceived2UI(J1939Utils.saveHex2String(data))
                }
                DataType.TChannel -> updateReceived2UI(" channel ${J1939Utils.byte2String(data[0])}")
                DataType.TDataMode -> updateReceived2UI(" dataMode ${getMode(data[0])}")
                DataType.TCan125 -> updateReceived2UI(" set can 125k success")
                DataType.TCan250 -> updateReceived2UI(" set can 250k success")
                DataType.TCan500 -> updateReceived2UI(" set can 500k success")
                DataType.TMcuVersion -> updateReceived2UI(" Mcu Version: ${String(data, Charset.defaultCharset())}")
                else -> {
                }
            }
        }
    } catch (e: Exception) {
        updateSend2UI("mService is null " + e.toString())
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
