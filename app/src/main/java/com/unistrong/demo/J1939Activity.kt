package com.unistrong.demo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import com.unistrong.demo.utils.J1939Utils
import com.unistrong.demo.utils.KtUtils
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
 *
 * 更多关于J1939 协议的说明请阅读J1939相关文档
 */
class J1939Activity : BaseActivity(), View.OnClickListener {
    private var mService: CommunicationService? = null
    private var mTv: TextView? = null

    private var mSpPriority: Spinner? = null
    private var mSpDlc: Spinner? = null
    private var mEtDst: EditText? = null
    private var mEtSrc: EditText? = null
    private var mEtPgn: EditText? = null
    private var mEtPf: EditText? = null
    private var mEtPs: EditText? = null
    private var mEtData: EditText? = null

    private var filterStr = ""
    private var mHandle= Handler()

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
        mEtPgn = findViewById(R.id.et_pgn) as EditText
        mEtPf = findViewById(R.id.et_pf) as EditText
        mEtPs = findViewById(R.id.et_ps) as EditText
        mEtData = findViewById(R.id.et_data) as EditText
        mTv = findViewById(R.id.tv_result) as TextView
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
        updateText(if (intPf < 240) "若PF<240(0xF0),则为PDU1格式;PDU1格式下PS域是目标地址" else "若PF>=240(0xF0),则为PDU2格式;PDU2格式下PS域为组扩展(GE）值")
        mFilter = if (filter.isChecked) 0x01 else 0x00
        mHandle.postDelayed({sendCommand(Command.Send.Channel1())},100)
        mHandle.postDelayed({sendCommand(Command.Send.ModeJ1939())},200)
        mHandle.postDelayed({sendCommand(Command.Send.Switch250K())},300)
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
                    DataType.TDataJ1939 -> {
                        updateText("received ${if (sendPf < 240) "PGN" else ""} response -> ID[0x${DataUtils.saveHex2StringNoSpace(DataUtils.cutByteArray(bytes, 0, 4))}] data[x|${DataUtils.saveHex2String(DataUtils.cutByteArray(bytes, 4, bytes.size))}]")
                        handleJ1939(bytes)
                    }
                    DataType.TFilter -> updateText("ID[0x" + filterStr + "] filter " + if (bytes[0].toInt() == 0x01) " success" else " failed")
                    DataType.TCancelFilter -> updateText("can id cancel filter " + if (bytes[0].toInt() == 0x01) "success" else " failed")
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

    private fun handleJ1939(bytes: ByteArray) {
        //bytes = 18 FE DC 00 55 55 55 55 55 55 11 11
        //0x18 == priority
        //0xFE,0xDC === PGN
        //0x00 === src
        //0x55 0x55 0x55 0x55 0x55 0x55 0x11 0x11  === data
        val valueByte = ByteArray(8)
        System.arraycopy(bytes, 4, valueByte, 0, valueByte.size)
        //TODO 参考文档
        Log.i("gh0st", DataUtils.saveHex2String(valueByte))
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
            Log.e(TAG, string)
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
            R.id.btn_send_data -> {
                ////参考文档
                //int pgn = 61440;
                ////00F000
                //String strPgn = DataUtils.IntToHex(pgn);
                //if (strPgn.length() < 4) {
                //    String newStr = "0000".subSequence(0, 4 - strPgn.length()).toString();
                //    strPgn = newStr + strPgn;
                //}//TODO int pgn to hex
                val data = byteArrayOf(mFilter, 0xF9.toByte(), 0x06, 0x00, 0xEA.toByte(), 0x00, 0xDC.toByte(), 0xFE.toByte(), 0x00)
                //sendJ1939Data(data);
                sendTestJ1939Data()
            }
            R.id.btn_filter -> filter()
            R.id.btn_filter_cancel -> sendCommand(Command.Send.cancelFilterCan())
        }//sendCommand(Command.Send.Switch500K());
    }

    private fun filter() {
        val priString = resources.getStringArray(R.array.priority)[mSpPriority!!.selectedItemPosition]
        val dstString = mEtDst!!.text.toString()
        val pgnString = mEtPgn!!.text.toString()
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
}
