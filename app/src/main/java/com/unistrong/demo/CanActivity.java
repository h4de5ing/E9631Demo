package com.unistrong.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;

public class CanActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "unistrong";
    private CommunicationService mService;
    private TextView mTv;
    private EditText etId;
    private EditText etData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can);
        etId = (EditText) findViewById(R.id.et_id);
        etData = (EditText) findViewById(R.id.et_data);
        findViewById(R.id.btn_search_channel).setOnClickListener(this);
        findViewById(R.id.btn_set_channel1).setOnClickListener(this);
        findViewById(R.id.btn_set_channel2).setOnClickListener(this);
        findViewById(R.id.btn_search_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_can_mode).setOnClickListener(this);
        findViewById(R.id.btn_set_baud).setOnClickListener(this);
        findViewById(R.id.btn_send_data).setOnClickListener(this);
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
                        case TDataOBD:
                            break;
                        case TDataJ1939:
                            break;
                        case TChannel:
                            updateText("current channel " + bytes[0]);
                            break;
                        case TDataMode:
                            updateText("current mode " + DataUtils.getDataMode(bytes[0]));
                            break;
                        case TDataCan:
                            updateText("we got can data:" + DataUtils.saveHex2String(bytes));
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

    private void sendCanData(byte[] id, byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.sendCan(id, data);
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
            case R.id.btn_set_can_mode:
                sendCommand(Command.Send.ModeCan());
                break;
            case R.id.btn_set_baud:
                //sendCommand(Command.Send.Switch250K());
                sendCommand(Command.Send.Switch500K());
                break;
            case R.id.btn_send_data:
                   /*byte[] id = new byte[]{0x00, 0x00, 0x00, 0x00};
                byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
                sendCanData(id, data);*/
                String strID = etId.getText().toString();
                String strData = etData.getText().toString();
                if (strID.length() % 2 == 0 || strID.length() != 8) {
                    if (strData.length() % 2 == 0 || strData.length() != 16) {
                        sendCanData(int2byte(strID, 4), int2byte(strData, 8));
                    } else {
                        Toast.makeText(CanActivity.this, "error data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CanActivity.this, "error id", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public byte[] int2byte(String string, int byteLength) {
        byte[] data = new byte[byteLength];
        for (int j = 0; j < data.length; j++) {
            data[j] = (byte) (Integer.valueOf(string.substring(j * 2, j * 2 + 2), 16) & 0xff);
        }
        return data;
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
