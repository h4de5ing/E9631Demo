package com.unistrong.demo;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;

/**
 * 1. search can channel and setting channel
 * 2. search mode and setting [OBD] mode
 * 3. setting can baud
 * 4. send start command and select ECU
 * 5. search support pid
 * 6. analysis ECU support pid value
 * 7. send pid command
 * 8. analysis pid value
 * <p>
 * <p>
 * 1.查询mcu 设置的can通道 如果can通道不正确,设置对应的can通道
 * 2.查询协议模式，如果不是obd模式，设置obd模式
 * 3.设置can设备的通信波特率
 * 4.发送开始指令，获得汽车的ECU并选择对应的ECU
 * 5.发送PID查询指令到对于的ECU
 * 6.解析对应ECU所支持的PID列表
 * 7.发送支持的PID指令
 * 8.解析PID值
 */
public class OBDIIActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "unistrong";
    private CommunicationService mService;
    private TextView mTv;
    private byte ECUType = (byte) 0xDF;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obdii);
        findViewById(R.id.btn_search_channel).setOnClickListener(this);
        findViewById(R.id.btn_set_channel1).setOnClickListener(this);
        findViewById(R.id.btn_set_channel2).setOnClickListener(this);
        findViewById(R.id.btn_search_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_obd_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_baud).setOnClickListener(this);
        findViewById(R.id.btn_send_data_start).setOnClickListener(this);
        findViewById(R.id.btn_send_data_pidlist).setOnClickListener(this);
        findViewById(R.id.btn_send_data_pid).setOnClickListener(this);
        mTv = (TextView) findViewById(R.id.tv_result);
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
                            updateText("we got obd data:" + DataUtils.saveHex2String(bytes));
                            handleOBD(bytes);
                            break;
                        case TDataJ1939:
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

    private void sendCommand(byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.send(data);
            }
        }
    }

    private void sendOBDData(byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.sendOBD(data);
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
            case R.id.btn_set_obd_mode:
                sendCommand(Command.Send.ModeOBD());
                break;
            case R.id.btn_set_baud:
                //sendCommand(Command.Send.Switch250K());
                sendCommand(Command.Send.Switch500K());
                break;
            case R.id.btn_send_data_start:
                byte[] start11 = new byte[]{0x01, 0x07, (byte) 0xDF, 0x00, 0x00, 0x02, 0x01, 0x00};//ISO15756 500K 11bit
                //byte[] start29 = new byte[]{0x01, 0x18, (byte) 0xDB, 0x33, (byte) 0xF5, 0x02, 0x01, 0x00};//ISO15765 500K 29bit
                sendOBDData(start11);
                isStart = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isStart = false;
                    }
                }, 3000);
                break;
            case R.id.btn_send_data_pidlist://get support pid
                sendOBDData(new byte[]{0x01, 0x07, ECUType, 0x00, 0x00, 0x02, 0x01, 0x00});

                sendOBDData(new byte[]{0x01, 0x07, ECUType, 0x00, 0x00, 0x02, 0x01, 0x20});

                sendOBDData(new byte[]{0x01, 0x07, ECUType, 0x00, 0x00, 0x02, 0x01, 0x40});
                break;
            case R.id.btn_send_data_pid://get  Vehicle speed
                byte pid = 0x0D;//0x0D  Vehicle speed
                sendOBDData(new byte[]{0x01, 0x07, ECUType, 0x00, 0x00, 0x02, 0x01, pid});
                break;
        }
    }


    private boolean isStart = false;

    private void handleOBD(byte[] bytes) {
        if (isStart) ECUType = (byte) (bytes[3] - (byte) 0x08);
        else {
            //00 00 07 E8 03 41 0D EA
            byte pci = bytes[4];
            byte frameType = (byte) (pci & (byte) 0xf0);
            int length = pci & 0x0f;
            switch (frameType) {
                case 0x00://SINGLE_FRAME
                    byte[] valueBytes = new byte[length - 2];
                    System.arraycopy(bytes, bytes.length - length + 2, valueBytes, 0, valueBytes.length);
                    byte pid = bytes[6];
                    switch (pid) {
                        case 0x01:
                            break;
                        case 0x0D:
                            //hex(EA)=des(234)
                            int speed = valueBytes[0] & 0xff;
                            updateText("Vehicle speed:" + speed + " KM/H");
                            break;
                    }
                    break;
                case 0x10://FIRST_FRAME
                    break;
                case 0x20://CONTINUOUS_FRAME
                    break;
                case 0x30://Flow_FRAME
                    break;
            }
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
