package com.unistrong.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;

/**
 * 1. search can channel and setting channel
 * 2. search mode and setting [J1939] mode
 * 3. setting can baud
 * 4. send j1939 command
 * 5. analysis value
 * <p>
 * <p>
 * <p>
 * 1.查询mcu 设置的can通道 如果can通道不正确,设置对应的can通道
 * 2.查询协议模式，如果不是J1939模式，设置J1939模式
 * 3.设置can设备的通信波特率
 * 4.发送对于的J1939指令
 * 5.解析对应值
 *
 * 更多关于J1939 协议的说明请阅读J1939相关文档
 */
public class J1939Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "unistrong";
    private CommunicationService mService;
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_j1939);
        findViewById(R.id.btn_search_channel).setOnClickListener(this);
        findViewById(R.id.btn_set_channel1).setOnClickListener(this);
        findViewById(R.id.btn_set_channel2).setOnClickListener(this);
        findViewById(R.id.btn_search_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_j1939_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_baud).setOnClickListener(this);
        findViewById(R.id.btn_send_data).setOnClickListener(this);
        mTv = findViewById(R.id.tv_result);
        initBind();
    }

    private void initBind() {
        try {
            mService = CommunicationService.getInstance(this);
            mService.setShutdownCountTime(12);
            mService.bind();
            mService.getData(new CommunicationService.IProcessData() {
                @Override
                public void process(byte[] bytes, DataType dataType) {
                    Log.e(TAG, dataType.name() + " " + DataUtils.saveHex2String(bytes));
                    switch (dataType) {
                        case TAccOn:
                            break;
                        case TAccOff:
                            break;
                        case TMcuVersion:
                            break;
                        case TMcuVoltage:
                            break;
                        case TCan250:
                            updateText("can 250K set success");
                            break;
                        case TCan500:
                            updateText("can 500K set success");
                            break;
                        case TChannel:
                            updateText("current channel " + bytes[0]);
                            break;
                        case TDataMode:
                            updateText("current mode " + DataUtils.getDataMode(bytes[0]));
                            break;
                        case TDataCan:
                            break;
                        case TDataOBD:
                            break;
                        case TDataJ1939:
                            updateText("we got j1939 data:" + DataUtils.saveHex2String(bytes));
                            handleJ1939(bytes);
                            break;
                        case TUnknow:
                            break;
                        case TGPIO:
                            break;
                        case TAccStatus:
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleJ1939(byte[] bytes) {
        //bytes = 18 FE DC 00 55 55 55 55 55 55 11 11
        //0x18 == priority
        //0xFE,0xDC === PGN
        //0x00 === src
        //0x55 0x55 0x55 0x55 0x55 0x55 0x11 0x11  === data
        byte[] valueByte = new byte[8];
        System.arraycopy(bytes, 4, valueByte, 0, valueByte.length);
        //TODO 参考文档
        Log.i("gh0st", DataUtils.saveHex2String(valueByte));
    }

    private void sendCommand(byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.send(data);
            }
        }
    }

    private void sendJ1939Data(byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.sendJ1939(data);
            }
        }
    }

    private void updateText(final String string) {
        if (mTv != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTv.append(string + "\n");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search_channel:
                sendCommand(Command.Send.SearchChannel());
                break;
            case R.id.btn_set_channel1:
                sendCommand(Command.Send.Channel1());
                break;
            case R.id.btn_set_channel2:
                sendCommand(Command.Send.Channel2());
                break;
            case R.id.btn_search_mode:
                sendCommand(Command.Send.SearchMode());
                break;
            case R.id.btn_set_j1939_mode:
                sendCommand(Command.Send.ModeJ1939());
                break;
            case R.id.btn_set_baud:
                sendCommand(Command.Send.Switch250K());
                //sendCommand(Command.Send.Switch500K());
                break;
            case R.id.btn_send_data:
                //参考文档
                int pgn = 61440;
                //00F000
                String strPgn = DataUtils.IntToHex(pgn);
                if (strPgn.length() < 4) {
                    String newStr = "0000".subSequence(0, 4 - strPgn.length()).toString();
                    strPgn = newStr + strPgn;
                }//TODO int pgn to hex
                byte[] data = new byte[]{0x01, (byte) 0xF9, 0x06, 0x00, (byte) 0xEA, 0x00, (byte) 0xDC, (byte) 0xFE, 0x00};
                sendJ1939Data(data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            try {
                mService.unbind();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
