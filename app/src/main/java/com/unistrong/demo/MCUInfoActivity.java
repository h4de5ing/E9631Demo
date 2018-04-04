package com.unistrong.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;

public class MCUInfoActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvResult;
    private CommunicationService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcuinfo);
        findViewById(R.id.btn_version).setOnClickListener(this);
        findViewById(R.id.btn_voltage).setOnClickListener(this);
        findViewById(R.id.btn_gpio_radar).setOnClickListener(this);
        findViewById(R.id.btn_gpio_mileage).setOnClickListener(this);
        findViewById(R.id.btn_acc_status).setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.tv_result);
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
                    Log.e("gh0st", dataType.name() + " " + DataUtils.saveHex2String(bytes));
                    handle(bytes, dataType);
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

    private void updateText(final String string) {
        if (tvResult != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.append(string + "\n");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_version:
                sendCommand(Command.Send.Version());
                break;
            case R.id.btn_voltage:
                sendCommand(Command.Send.Voltage());
                break;
            case R.id.btn_gpio_radar:
                sendCommand(Command.Send.Gpio().get(2));
                break;
            case R.id.btn_gpio_mileage:
                sendCommand(Command.Send.Gpio().get(8));
                break;
            case R.id.btn_acc_status:
                sendCommand(Command.Send.SearchAccStatus());
                break;
        }
    }

    public void allCommand() {
        Command.Send.SearchAccStatus();// search MCU current acc status

        Command.Send.SearchChannel();//search MCU current channel
        Command.Send.Channel1();//setting MCU channel 1
        Command.Send.Channel2();//setting MCU channel 2

        Command.Send.SearchMode(); //search MCU support protocol
        Command.Send.ModeJ1939();//setting MCU J1939 protocol
        Command.Send.ModeOBD(); //setting MCU OBDII protocol
        Command.Send.ModeCan(); //setting MCU can protocol

        Command.Send.Switch500K(); //setting MCU 500K
        Command.Send.Switch250K(); //setting MCU 250K

        Command.Send.Version(); //get MCU firmware version
        Command.Send.Voltage(); //get car voltage
    }

    private void handle(byte[] bytes, DataType dataType) {
        switch (dataType) {
            case TAccOn:
                updateText("ACC ON");
                break;
            case TAccOff:
                updateText("ACC OFF");
                break;
            case TMcuVersion:
                updateText("Version:" + new String(bytes));
                break;
            case TMcuVoltage:
                updateText("Voltage:" + new String(bytes));
                break;
            case TCan250:
                updateText("250K setting success");
                break;
            case TCan500:
                updateText("500K setting success");
                break;
            case TDataMode:
                updateText("setting " + DataUtils.getDataMode(bytes[0]) + " success");
                break;
            case TDataCan:
                //we get can data
                //handle can data
                break;
            case TDataOBD:
                //we get obd data
                break;
            case TDataJ1939:
                //we get J1939 data
                break;
            case TChannel:
                updateText("current channel " + bytes[0]);
                break;
            case TAccStatus://Deprecated
                break;
            case TUnknow://undefined data type,maybe error data
                break;
            case TGPIO:
                if (bytes[0] == 0x12) {
                    updateText("GPIO Radar");
                } else if (bytes[0] == 0x22) {
                    updateText("GPIO Mileage");
                }
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
