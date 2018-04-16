package com.unistrong.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.unistrong.uartsdk.ProcessData;
import com.unistrong.uartsdk.VanManager;
import com.van.uart.UartManager;

public class UartActivity extends BaseActivity implements View.OnClickListener {
    private EditText etSend;
    private VanManager vanManager;
    private String text;
    private TextView tvResult;
    private UartManager.BaudRate baudRate = UartManager.BaudRate.B115200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        etSend = (EditText) findViewById(R.id.et_send);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvResult.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.btn_4).setOnClickListener(this);
        findViewById(R.id.btn_6).setOnClickListener(this);
        findViewById(R.id.btn_7).setOnClickListener(this);
        findViewById(R.id.btn_clean).setOnClickListener(this);
        openUart();
    }

    private void openUart() {
        vanManager = new VanManager();
        boolean uart4opend = vanManager.openUart4(baudRate);
        updateText(uart4opend ? "uart4 opend success" : "uart4 opend failed");
        vanManager.uartData4(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateText("uart4:" + new String(bytes) + ",length:" + len);
            }
        });
        boolean uart6opend = vanManager.openUart6(baudRate);
        updateText(uart6opend ? "uart6 opend success" : "uart6 opend failed");
        vanManager.uartData6(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateText("uart6:" + new String(bytes) + ",length:" + len);
            }
        });
        boolean uart7opend = vanManager.openUart7(baudRate);
        updateText(uart7opend ? "uart7 opend success" : "uart7 opend success");
        vanManager.uartData7(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateText("uart7:" + new String(bytes) + ",length:" + len);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_4:
                text = etSend.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    if (vanManager.isOpenUart4()) {
                        vanManager.sendData2Uart4(text.getBytes());
                    }
                }
                break;
            case R.id.btn_6:
                text = etSend.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    if (vanManager.isOpenUart6()) {
                        vanManager.sendData2Uart6(text.getBytes());
                    }
                }
                break;
            case R.id.btn_7:
                text = etSend.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    if (vanManager.isOpenUart7()) {
                        vanManager.sendData2Uart7(text.getBytes());
                    }
                }
                break;
            case R.id.btn_clean:
                if (tvResult != null) {
                    tvResult.setText("");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeUart();
    }

    private void closeUart() {
        vanManager.closeUart4();
        vanManager.closeUart6();
        vanManager.closeUart7();
    }

    private void updateText(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvResult != null) {
                    tvResult.append(string + "\n");
                }
            }
        });
    }
}
