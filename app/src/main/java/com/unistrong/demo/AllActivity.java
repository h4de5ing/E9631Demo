package com.unistrong.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;
import com.unistrong.uartsdk.ProcessData;
import com.unistrong.uartsdk.VanManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AllActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st";
    public static Camera mCamera6;
    public static Camera mCamera7;
    public static final int camera6ID = 6;
    public static final int camera7ID = 7;
    private TextureView video6;
    private TextureView video7;
    private TextView tvGPS;
    private TextView tvCPUInfo;
    private TextView tvCanInfo;
    private TextView tvUartInfo;
    private VanManager vanManager;
    private Button btnOBDStart;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        tvGPS = findViewById(R.id.tv_gps);
        tvCPUInfo = findViewById(R.id.tv_cpu_info);
        tvCanInfo = findViewById(R.id.tv_can_info);
        tvUartInfo = findViewById(R.id.tv_uart_info);
        tvCPUInfo.setMovementMethod(new ScrollingMovementMethod());
        tvCanInfo.setMovementMethod(new ScrollingMovementMethod());
        tvUartInfo.setMovementMethod(new ScrollingMovementMethod());
        btnOBDStart = findViewById(R.id.btn_obd_start);
        btnOBDStart.setOnClickListener(this);
        video6 = findViewById(R.id.video6);
        video7 = findViewById(R.id.video7);
        openUart();
        initBind();
        //btnOBDStart.performClick();
        initGPS();
    }

    private void openUart() {
        vanManager = new VanManager();
        boolean uart4opend = vanManager.openUart4();
        updateUartText(uart4opend ? "uart4 opend success" : "uart4 opend failed");
        vanManager.uartData4(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateUartText("uart4:[ " + len + " ]" + new String(bytes));
            }
        });
    }

    private void updateUartText(final String string) {
        if (tvUartInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tvUartInfo != null) {
                        tvUartInfo.append(string + "\n");
                        int offset = tvUartInfo.getLineCount() * tvUartInfo.getLineHeight() - tvUartInfo.getHeight();
                        if (offset >= 500) {
                            tvUartInfo.setText("");
                        }
                        tvUartInfo.scrollTo(0, offset > 0 ? offset : 0);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        initTextureView();
        registerLisenter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
        unRegisterListener();
    }

    private void initCamera() {
        try {
            if (mCamera6 == null) {
                mCamera6 = Camera.open(camera6ID);
            }
            if (mCamera7 == null) {
                mCamera7 = Camera.open(camera7ID);
            }
        } catch (Exception e) {
            Log.e(TAG, "camera is not available check[android.permission.CAMERA]");
        }
    }

    public void initTextureView() {
        if (video6 != null) {
            video6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (mCamera6 != null) {
                        try {
                            mCamera6.startPreview();
                            SystemClock.sleep(200);
                            mCamera6.setPreviewTexture(surface);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
        if (video7 != null) {
            video7.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (mCamera7 != null) {
                        try {
                            mCamera7.startPreview();
                            SystemClock.sleep(200);
                            mCamera7.setPreviewTexture(surface);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    public void stopPreview() {
        if (mCamera6 != null) {
            mCamera6.stopPreview();
            Log.i(TAG, "stopPreview");
        }
        if (mCamera7 != null) {
            mCamera7.stopPreview();
            Log.i(TAG, "stopPreview");
        }
    }

    public void closeCamera() {
        if (mCamera6 != null) {
            mCamera6.release();
            mCamera6 = null;
        }
        if (mCamera7 != null) {
            mCamera7.release();
            mCamera7 = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopPreview();
        closeCamera();
        vanManager.closeUart4();
        if (mService != null) {
            try {
                mService.unbind();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private CommunicationService mService;
    private boolean isStart = false;
    private byte ECUType = (byte) 0xDF;
    private Handler mHandler = new Handler();
    private byte[] start11 = new byte[]{0x01, 0x07, (byte) 0xDF, 0x00, 0x00, 0x02, 0x01, 0x00};//ISO15756 500K 11bit

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_obd_start:
                if (mService != null) {
                    isStart = true;
                    sendOBDData(start11);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ECUType == (byte) 0xDF) {
                                updateOBDText("connect failed");
                            }
                            isStart = false;
                        }
                    }, 3000);
                }
                break;
        }
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
                            updateOBDText("can 250K set success");
                            break;
                        case TCan500:
                            updateOBDText("can 500K set success");
                            break;
                        case TChannel:
                            updateOBDText("current channel " + bytes[0]);
                            break;
                        case TDataMode:
                            updateOBDText("current mode " + DataUtils.getDataMode(bytes[0]));
                            break;
                        case TDataCan:
                            break;
                        case TDataOBD:
                            //updateOBDText("we got obd data:" + DataUtils.saveHex2String(bytes));
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

    private void updateOBDText(final String string) {
        if (tvCanInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tvCanInfo != null) {
                        tvCanInfo.append(string + "\n");
                        int offset = tvCanInfo.getLineCount() * tvCanInfo.getLineHeight() - tvCanInfo.getHeight();
                        if (offset >= 500) {
                            tvCanInfo.setText("");
                        }
                        tvCanInfo.scrollTo(0, offset > 0 ? offset : 0);
                    }
                }
            });
        }
    }


    private void sendOBDData(byte[] data) {
        if (mService != null) {
            if (mService.isBindSuccess()) {
                mService.sendOBD(data);
            }
        }
    }

    private void handleOBD(byte[] bytes) {
        if (isStart) {
            updateOBDText("connect success");
            ECUType = (byte) (bytes[3] - (byte) 0x08);
            initTimer();
        } else {
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
                            updateOBDText("Vehicle speed:" + speed + " KM/H");
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

    private Timer timer;

    private void initTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                byte pid = 0x0D;//0x0D  Vehicle speed
                sendOBDData(new byte[]{0x01, 0x07, ECUType, 0x00, 0x00, 0x02, 0x01, pid});
            }
        }, 5000, 1000);
    }

    LocationManager mLM;

    private void initGPS() {
        mLM = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    // 注册GPS 监听事件
    private void registerLisenter() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "没有GPS权限", Toast.LENGTH_LONG).show();
            return;
        }
        //mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mLL);
        //mLM.addGpsStatusListener(mGL);
        mLM.addNmeaListener(mNL);
    }

    // 撤销
    private void unRegisterListener() {
        //mLM.removeUpdates(mLL);
        //mLM.removeGpsStatusListener(mGL);
        mLM.removeNmeaListener(mNL);
    }

    private GpsStatus.NmeaListener mNL = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long arg0, String nmea) {
            updateGPSText("nmea:" + nmea);

        }
    };
    private GpsStatus.Listener mGL = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            @SuppressLint("MissingPermission") GpsStatus gs = mLM.getGpsStatus(null);

            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX://第一次定位

                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS://卫星状态改变
                    break;
            }
        }
    };

    private void updateGPSText(final String string) {
        if (tvGPS != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tvGPS != null) {
                        tvGPS.append(string + "\n");
                        int offset = tvGPS.getLineCount() * tvGPS.getLineHeight() - tvGPS.getHeight();
                        if (offset >= 5000) {
                            tvGPS.setText("");
                        }
                        tvGPS.scrollTo(0, offset > 0 ? offset : 0);
                    }
                }
            });
        }
    }
}
