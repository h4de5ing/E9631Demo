package com.unistrong.demo

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import com.unistrong.demo.utils.J1939Utils
import com.unistrong.demo.utils.JSONHelper
import com.unistrong.demo.utils.KtUtils
import com.unistrong.demo.utils.SPUtils
import com.unistrong.e9631sdk.Command
import com.unistrong.e9631sdk.CommunicationService
import com.unistrong.e9631sdk.DataType


/**
 * 1. search can channel and setting channel
 * 2. search mode and setting [J1939] mode
 * 3. setting can baud
 * 4. send j1939 command
 * 5. analysis value
 *
 *
 *
 *
 *
 *
 * 1.查询mcu 设置的can通道 如果can通道不正确,设置对应的can通道
 * 2.查询协议模式，如果不是J1939模式，设置J1939模式
 * 3.设置can设备的通信波特率
 * 4.发送对于的J1939指令
 * 5.解析对应值
 *
 * TODO J1939协议过滤需要上位机自行保存历史记录，重复过滤不发送
 * J1939 Max filter 9
 * 更多关于J1939 协议的说明请阅读J1939相关文档
 */
class J1939Activity : BaseActivity(), View.OnClickListener, TextWatcher {

    private var mService: CommunicationService? = null
    private var mTv: TextView? = null

    private var mSpPriority: Spinner? = null
    private var mSpDlc: Spinner? = null
    private var mEtDst: EditText? = null
    private var mEtSrc: EditText? = null
    private var mEtFilterPgn: EditText? = null
    private var mEtPf: EditText? = null
    private var mEtPs: EditText? = null
    private var mEtData: EditText? = null
    private var mFilterList = mutableListOf<String>()
    private var filterStr = ""
    private var mHandle = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_j1939)
        findViewById(R.id.btn_search_channel).setOnClickListener(this)
        findViewById(R.id.btn_set_channel1).setOnClickListener(this)
        findViewById(R.id.btn_set_channel2).setOnClickListener(this)
        findViewById(R.id.btn_search_mode).setOnClickListener(this)
        findViewById(R.id.btn_set_j1939_mode).setOnClickListener(this)
        findViewById(R.id.btn_set_baud).setOnClickListener(this)
        findViewById(R.id.btn_send_data).setOnClickListener(this)
        findViewById(R.id.btn_clean).setOnClickListener(this)
        mSpPriority = findViewById(R.id.sp_priority) as Spinner
        mSpDlc = findViewById(R.id.sp_dlc) as Spinner
        mEtDst = findViewById(R.id.et_dst) as EditText
        mEtSrc = findViewById(R.id.et_src) as EditText
        mEtFilterPgn = findViewById(R.id.et_filter_pgn) as EditText
        mEtPf = findViewById(R.id.et_pf) as EditText
        mEtPs = findViewById(R.id.et_ps) as EditText
        mEtData = findViewById(R.id.et_data) as EditText
        mTv = findViewById(R.id.tv_result) as TextView
        mTv!!.movementMethod = ScrollingMovementMethod()
        findViewById(R.id.btn_filter).setOnClickListener(this)
        findViewById(R.id.btn_filter_cancel).setOnClickListener(this)
        val filter = findViewById(R.id.cb_filter) as CheckBox
        filter.setOnCheckedChangeListener { _, isChecked -> mFilter = (if (isChecked) 0x01 else 0x00).toByte() }
        mSpPriority!!.adapter = ArrayAdapter(this@J1939Activity, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.priority))
        mSpPriority!!.setSelection(1)
        mSpDlc!!.adapter = ArrayAdapter(this@J1939Activity, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.dlc))
        mSpDlc!!.setSelection(2)
        initBind()
        val pfString = mEtPf!!.text.toString()
        val intPf = J1939Utils.byte2int(J1939Utils.string2byte(pfString))
        //updateText(if (intPf < 240) "if PF<240(0xF0),PDU1 format ;PDU1 -> PS is dst address " else "if PF>=240(0xF0),PDU2 format ;PDU2 -> PS Domain as group expansion(GE)")
        mFilter = if (filter.isChecked) 0x01 else 0x00
        mHandle.postDelayed({ sendCommand(Command.Send.Channel1()) }, 100)
        mHandle.postDelayed({ sendCommand(Command.Send.ModeJ1939()) }, 200)
        mHandle.postDelayed({ sendCommand(Command.Send.Switch250K()) }, 300)
        mHandle.postDelayed({ sendCommand(Command.Send.cancelFilterCan()) }, 400)
        mEtFilterPgn!!.addTextChangedListener(this)
        val json = SPUtils.getSp(this@J1939Activity, "filterList", "").toString()
        try {
            val list = JSONHelper.parseArrays2List(json)
            list.map { updateText(it) }
            //list.forEach { updateText()it.toString()) }
            mFilterList.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initBind() {
        try {
            mService = CommunicationService.getInstance(this)
            mService!!.setShutdownCountTime(12)
            mService!!.bind()
            mService!!.getData { bytes, dataType ->
                Log.e(TAG, dataType.name + " " + DataUtils.saveHex2String(bytes))
                when (dataType) {
                    DataType.TAccOn -> {
                    }
                    DataType.TAccOff -> {
                    }
                    DataType.TMcuVersion -> {
                    }
                    DataType.TMcuVoltage -> {
                    }
                    DataType.TCan250 -> updateText("can 250K set success")
                    DataType.TCan500 -> updateText("can 500K set success")
                    DataType.TChannel -> updateText("current channel " + bytes[0])
                    DataType.TDataMode -> updateText("current mode " + DataUtils.getDataMode(bytes[0]))
                    DataType.TDataCan -> {
                    }
                    DataType.TDataOBD -> {
                    }
                    DataType.TDataJ1939 -> handleJ1939(bytes)
                    DataType.TFilter -> updateText("ID[0x" + filterStr + "] filter " + if (bytes[0].toInt() > 0) " success [${J1939Utils.byte2int(bytes[0])}]" else " failed") //bytes[0] 表示过滤的个数
                    DataType.TCancelFilter -> {
                        mFilterList.removeAll { true }
                        SPUtils.setSP(this@J1939Activity, "filterList", JSONHelper.toJSON(mFilterList))
                        mFilterList.map { updateText(it) }
                        updateText("can id cancel filter " + if (bytes[0].toInt() == 0x01) "success" else " failed")
                    }
                    DataType.TUnknow -> {
                    }
                    DataType.TGPIO -> {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 01 C7 F6 E0 06
     * 第一个字节表示通道
     * 后四个字节表示id
     * id解析方式就是右移3位
     *
     * J1939 都是接收扩展帧
     * 1.判断最后一个字节是否是0x04,0x06   0x04 数据扩展帧,包含数据 0x06远程扩展帧,不包含数据
     * 2.右移3位
     * 3.判断是不是PGN应答 如果是PGN应答输出
     */
    private fun handleJ1939(bytes: ByteArray) {
        updateText("rev:" + J1939Utils.saveHex2String(bytes))
        /*   val id = ByteArray(4)
          System.arraycopy(bytes, 1, id, 0, id.size)
          val last = J1939Utils.byte2int(bytes[id.size])
          if ((last == 0x04) or (last == 0x06)) {
              val byte0 = J1939Utils.byte2int(id[0])
              val byte1 = J1939Utils.byte2int(id[1])
              val byte2 = J1939Utils.byte2int(id[2])
              val byte3 = J1939Utils.byte2int(id[3])
              val count = ((byte0 and 0xff) shl 24) or ((byte1 and 0xff) shl 16) or ((byte2 and 0xff) shl 8) or (byte3 and 0xff)
              var ushrInt = count ushr 3
              val zeroString = "00000000000000000000000000000000"//4个字节 总共32位
              var ushrBinary = Integer.toBinaryString(ushrInt)
              if (ushrBinary.length < 32) {
                  ushrBinary = zeroString.substring(0, 32 - ushrBinary.length) + ushrBinary
              }
              for (i in id.indices) {
                  id[i] = J1939Utils.string2byte(J1939Utils.getHex(ushrBinary.substring(i * 8, (i + 1) * 8)))
              }
              updateText("received -> ID[0x${DataUtils.saveHex2StringNoSpace(id)}] ${if (last == 0x04) "data[x|${DataUtils.saveHex2String(DataUtils.cutByteArray(bytes, 5, bytes.size))}]" else ""}")
          }*/
    }

    private fun sendCommand(data: ByteArray) {
        if (mService != null) {
            if (mService!!.isBindSuccess) {
                mService!!.send(data)
            }
        }
    }

    private fun sendJ1939Data(data: ByteArray) {
        if (mService != null) {
            if (mService!!.isBindSuccess) {
                mService!!.sendJ1939(data)
            }
        }
    }

    private fun updateText(string: String) {
        if (mTv != null) {
            runOnUiThread { mTv!!.append(string + "\n") }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_search_channel -> sendCommand(Command.Send.SearchChannel())
            R.id.btn_set_channel1 -> sendCommand(Command.Send.Channel1())
            R.id.btn_set_channel2 -> sendCommand(Command.Send.Channel2())
            R.id.btn_search_mode -> sendCommand(Command.Send.SearchMode())
            R.id.btn_set_j1939_mode -> sendCommand(Command.Send.ModeJ1939())
            R.id.btn_set_baud -> sendCommand(Command.Send.Switch250K())
            R.id.btn_clean -> mTv!!.text = ""
            R.id.btn_send_data -> sendTestJ1939Data()
            R.id.btn_filter -> filter()
            R.id.btn_filter_cancel -> sendCommand(Command.Send.cancelFilterCan())
        }
    }

    private fun filter() {
        //判断历史记录
        val pgnString = mEtFilterPgn!!.text.toString()
        if (mFilterList.any { pgnString == it }) {
            updateText("had filter")
        } else {
            if (mFilterList.size >= 9) {
                updateText("max 9")
                return
            }
            val priString = resources.getStringArray(R.array.priority)[mSpPriority!!.selectedItemPosition]
            val dstString = mEtDst!!.text.toString()
            val pri = J1939Utils.string2byte(priString).toInt() shl 18
            val pgn = J1939Utils.hexString2Int(pgnString)
            val dst = J1939Utils.string2byte(dstString).toInt()
            val filter = pri or pgn shl 8 or dst
            filterStr = J1939Utils.int2HexString(filter)
            val pgnByteArray = filterStr.toByteArray()
            val filterByteArray = ByteArray(pgnByteArray.size + 1)
            filterByteArray[0] = 0x08//0x08表示过滤J1939协议 pgn
            System.arraycopy(pgnByteArray, 0, filterByteArray, 1, pgnByteArray.size)
            sendCommand(Command.Send.filterCan(filterByteArray))
            mFilterList.add(pgnString)
            SPUtils.setSP(this@J1939Activity, "filterList", JSONHelper.toJSON(mFilterList))
            mFilterList.map { updateText(it) }
        }
    }


    var sendPf = 0
    private fun sendTestJ1939Data() {
        val dstString = mEtDst!!.text.toString()
        val srcString = mEtSrc!!.text.toString()
        val pfString = mEtPf!!.text.toString()
        val psString = mEtPs!!.text.toString()
        val dlcString = resources.getStringArray(R.array.dlc)[mSpDlc!!.selectedItemPosition]
        val dataString = mEtData!!.text.toString()
        val priString = resources.getStringArray(R.array.priority)[mSpPriority!!.selectedItemPosition]
        if (dstString.isEmpty()) {
            mEtDst!!.error = "请输入目标地址"
            mEtDst!!.requestFocus()
        } else {
            if (srcString.isEmpty()) {
                mEtSrc!!.error = "请输入源地址"
                mEtSrc!!.requestFocus()
            } else {
                if (dataString.isEmpty()) {
                    mEtData!!.error = "请输入Data"
                    mEtData!!.requestFocus()
                } else {
                    val intPf = J1939Utils.byte2int(J1939Utils.string2byte(pfString))
                    val intPs = if (intPf < 240) J1939Utils.string2byte(dstString) else J1939Utils.string2byte(psString)
                    val j1939MessagePGN = j1939MessagePGN(srcString, priString, pfString, intPs, dataString)
                    val idExt = J1939Utils.int2HexString(KtUtils().J1939TranslateIdExt(srcString, priString, psString))
                    //pf==0xEA 表示PGN请求
                    sendPf = intPf
                    updateText("send ${if (intPf == 0xEA) "PGN request" else ""} -> ID[0x" + idExt + "] Data[0x" + dataString + "] " /*+ J1939Utils.saveHex2String(j1939MessagePGN)*/)
                    sendJ1939Data(j1939MessagePGN)
                }
            }
        }
    }

    /**
     * 过滤
     * 优先级
     * PF
     * PS
     * SRC
     * DATA
     */
    private fun j1939MessagePGN(src: String, priString: String, pf: String, ps: Byte, dataString: String): ByteArray {
        val list = mutableListOf<Byte>()
        list.add(J1939Activity.mFilter)
        list.add(J1939Utils.string2byte(priString))
        list.add(J1939Utils.string2byte(pf))
        list.add(ps)
        list.add(J1939Utils.string2byte(src))
        J1939Utils.int2bytes2(dataString).forEach {
            list.add(it)
        }
        return list.toByteArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            try {
                mService!!.unbind()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        private val TAG = "unistrong.gh0st"
        var mFilter: Byte = 0x00 //0x01 过滤  0x00 不过滤
    }

    //过滤PGN 交换字节位数   FilterPGN  -> FEDC  == Data->DCFE
    override fun afterTextChanged(editable: Editable?) {
        val inputS = editable.toString()
        if (inputS.length == 4) {
            updateData("${inputS.subSequence(2, 4)}${inputS.subSequence(0, 2)}")
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun updateData(data: String) {
        if (mEtData != null) {
            mEtData!!.setText(data)
        }
    }
}
