package com.unistrong.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unistrong.demo.dashboard.DashboardActivity;
import com.unistrong.demo.gps.GPSActivity;
import com.unistrong.demo.view.TitleLinearLayout;
import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;
import com.unistrong.uartsdk.ProcessData;
import com.unistrong.uartsdk.VanManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AllActivity2 extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st";
    public static Camera mCamera6;
    public static Camera mCamera7;
    public static final int camera6ID = 6;
    public static final int camera7ID = 7;
    private TextureView video6;
    private TextureView video7;
    private Button btnTakePicture6;
    private Button btnTakePicture7;
    private Button btnRecord6;
    private Button btnRecord7;
    private Chronometer tvRecordingTime6;
    private Chronometer tvRecordingTime7;
    private boolean isRecording6 = false;
    private boolean isRecording7 = false;
    private TextView tvMCUInfo;
    private TextView tvCanInfo;
    private TextView tvUartInfo;
    private TextView tvCurrentVolume;
    private TextView tvGSM;
    private VanManager vanManager;
    private MyPhoneStateListener myPhoneStateListener;
    private LinearLayout ll_video6;
    private TitleLinearLayout tll_video6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all2);
        Log.i("gh0st", "AllActivity2");
        tvMCUInfo = (TextView) findViewById(R.id.tv_mcu_info);
        tvCanInfo = (TextView) findViewById(R.id.tv_can_info);
        tvUartInfo = (TextView) findViewById(R.id.tv_uart_info);
        tvGSM = (TextView) findViewById(R.id.tv_gsm);
        tvCurrentVolume = (TextView) findViewById(R.id.tv_current_volume);
        video6 = (TextureView) findViewById(R.id.video6);
        btnTakePicture6 = (Button) findViewById(R.id.btn_take_picture6);
        btnRecord6 = (Button) findViewById(R.id.btn_record6);
        tvRecordingTime6 = (Chronometer) findViewById(R.id.tv_recording_time6);
        ll_video6 = (LinearLayout) findViewById(R.id.ll_video6);
        tll_video6 = (TitleLinearLayout) findViewById(R.id.tll_video6);

        video7 = (TextureView) findViewById(R.id.video7);
        btnTakePicture7 = (Button) findViewById(R.id.btn_take_picture7);
        btnRecord7 = (Button) findViewById(R.id.btn_record7);
        tvRecordingTime7 = (Chronometer) findViewById(R.id.tv_recording_time7);
        video6.setOnClickListener(this);
        video7.setOnClickListener(this);
        findViewById(R.id.btn_gps).setOnClickListener(this);
        findViewById(R.id.btn_dashboard).setOnClickListener(this);
        findViewById(R.id.btn_volume_plug).setOnClickListener(this);
        findViewById(R.id.btn_volume_sub).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
        findViewById(R.id.btn_6).setOnClickListener(this);
        findViewById(R.id.btn_7).setOnClickListener(this);
        findViewById(R.id.btn_obd_start).setOnClickListener(this);
        findViewById(R.id.btn_obd_start2).setOnClickListener(this);
        btnTakePicture6.setOnClickListener(this);
        btnTakePicture7.setOnClickListener(this);
        btnRecord6.setOnClickListener(this);
        btnRecord7.setOnClickListener(this);
        tvMCUInfo.setMovementMethod(new ScrollingMovementMethod());
        tvCanInfo.setMovementMethod(new ScrollingMovementMethod());
        tvUartInfo.setMovementMethod(new ScrollingMovementMethod());
        openUart();
        initBind();
        initMcuTimer();
        initGSM();
        initVolumes();
        updateMCUText();

        initSurplus();
    }

    private AudioManager mAudioManager;
    private int mMaxVolume = 0;
    private int mCurrentVolume = 0;

    private void initVolumes() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            tvCurrentVolume.setText("" + mCurrentVolume);
        }
    }

    private void updateVolumes() {
        tvCurrentVolume.setText("" + mCurrentVolume);
    }

    private Timer mMcuTimer;
    private boolean isSend = false;

    private void initMcuTimer() {
        mMcuTimer = new Timer();
        mMcuTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String status = readStatus(acStatus);
                if ("1".equals(status)) {// ac charger is plugged
                    if (!isSend) {
                        mHandler.sendEmptyMessageDelayed(0, 100);
                        isSend = true;
                    }
                } else {//ac charger not plugged
                    mAccStatus = false;
                    isSend = false;
                    updateMCUText();
                }
            }
        }, 10000, 2000);
    }

    private static final String acStatus = "/sys/class/power_supply/ac/present";
    private static final String usbStatus = "/sys/class/power_supply/usb/present";

    public String readStatus(String path) {
        String statue = null;
        try {
            BufferedReader read = new BufferedReader(new FileReader(new File(path)));
            statue = read.readLine();
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return statue;
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            Class<?> signalStrengthClass = signalStrength.getClass();
            try {
                Method method = signalStrengthClass.getMethod("getDbm");
                method.setAccessible(true);
                Object object = method.invoke(signalStrength);
                updateGSM(object.toString() + " Dbm");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initGSM() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            if (manager.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
                updateGSM("noSIM");
            } else {
                myPhoneStateListener = new MyPhoneStateListener();
                manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        }
    }

    private void updateGSM(final String s) {
        if (tvGSM != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tvGSM != null) {
                        tvGSM.setText(s);
                    }
                }
            });
        }
    }

    private void openUart() {
        vanManager = new VanManager();
        boolean uart4opend = vanManager.openUart4(115200);
        updateUartText(uart4opend ? "uart4 opend success" : "uart4 opend failed");
        vanManager.uartData4(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateUartText("uart4:[ " + len + " ]" + new String(bytes));
            }
        });
        boolean uart6opend = vanManager.openUart6(115200);
        updateUartText(uart6opend ? "uart6 opend success" : "uart6 opend failed");
        vanManager.uartData6(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateUartText("uart6:[ " + len + " ]" + new String(bytes));
            }
        });
        boolean uart7opend = vanManager.openUart7(115200);
        updateUartText(uart7opend ? "uart7 opend success" : "uart7 opend success");
        vanManager.uartData7(new ProcessData() {
            @Override
            public void process(byte[] bytes, int len) {
                updateUartText("uart7:[ " + len + " ]" + new String(bytes));
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
    }

    //每隔10秒检测剩余空间,如果小于200M就停止录像
    private Timer surplusTimer;

    private void initSurplus() {
        surplusTimer = new Timer();
        surplusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getStorageSpaceBytes() < 200) {
                    pauseRecording();
                    Toast.makeText(AllActivity2.this, "内存不足200M,停止录像", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000, 10000);
    }

    public long getStorageSpaceBytes() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.e(TAG, "Fail to access external storage", e);
            return 0;
        }
    }

    public void pauseRecording() {
        try {
            if (isRecording6) {
                tvRecordingTime6.stop();
                tvRecordingTime6.setVisibility(View.GONE);
                isRecording6 = false;
                btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                stopRecording6();
            }
            if (isRecording7) {
                tvRecordingTime7.stop();
                tvRecordingTime7.setVisibility(View.GONE);
                isRecording7 = false;
                btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                stopRecording7();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //try {
        //    if (isRecording6) {
        //        tvRecordingTime6.stop();
        //        tvRecordingTime6.setVisibility(View.GONE);
        //        isRecording6 = false;
        //        btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        //        stopRecording6();
        //    }
        //    if (isRecording7) {
        //        tvRecordingTime7.stop();
        //        tvRecordingTime7.setVisibility(View.GONE);
        //        isRecording7 = false;
        //        btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        //        stopRecording7();
        //    }
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        stopPreview();
        //closeCamera();
    }

    private void initCamera() {
        try {
            if (mCamera6 == null) {
                mCamera6 = Camera.open(camera6ID);
      /*          mCamera6.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {

                    }
                });*/
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    if (surface != null) {
                        surface.release();
                        surface = null;
                    }
                    return true;
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    if (surface != null) {
                        surface.release();
                        surface = null;
                    }
                    return true;
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
        vanManager.closeUart6();
        vanManager.closeUart7();
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
        if (mMcuTimer != null) {
            mMcuTimer.cancel();
            mMcuTimer.purge();
            mMcuTimer = null;
        }
    }

    private CommunicationService mService;
    private boolean isStart = false;
    //public byte ECUType = (byte) 0xDF;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    sendCommandList();
                    break;
            }
        }
    };

    private void sendCommandList() {
        final List<byte[]> list = new ArrayList<>();
        list.add(Command.Send.Version());
        list.add(Command.Send.Voltage());
        list.add(Command.Send.SearchAccStatus());
        list.add(Command.Send.Gpio().get(2));
        list.add(Command.Send.Gpio().get(8));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (byte[] bytes : list) {
                    sendCommand(bytes);
                    SystemClock.sleep(300);
                }
            }
        }).start();
    }

    private byte[] start11 = new byte[]{0x01, 0x07, (byte) 0xDF, 0x00, 0x00, 0x02, 0x01, 0x00};//ISO15756 500K 11bit
    long mLastTime = 0;
    long mCurTime = 0;
    private boolean isFullScreen6 = false;
    private boolean isFullScreen7 = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_obd_start:
                if (mService != null) {
                    isStart = true;
                    sendCommand(Command.Send.Channel1());
                    sendOBDData(start11);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (App.Companion.getAECUType() == (byte) 0xDF) {
                                updateOBDText("can 1 connect failed");
                            }
                            isStart = false;
                        }
                    }, 3000);
                }
                break;
            case R.id.btn_obd_start2:
                if (mService != null) {
                    isStart = true;
                    sendCommand(Command.Send.Channel2());
                    sendOBDData(start11);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (App.Companion.getAECUType() == (byte) 0xDF) {
                                updateOBDText("can 2 connect failed");
                            }
                            isStart = false;
                        }
                    }, 3000);
                }
                break;
            case R.id.btn_4:
                if (vanManager != null && vanManager.isOpenUart4()) {
                    vanManager.sendData2Uart4("ttyS4".getBytes());
                }
                break;
            case R.id.btn_6:
                if (vanManager != null && vanManager.isOpenUart4()) {
                    vanManager.sendData2Uart6("ttyS6".getBytes());
                }
                break;
            case R.id.video6:
                mLastTime = mCurTime;
                mCurTime = System.currentTimeMillis();
                if (mCurTime - mLastTime < 300) {
                    mCurTime = 0;
                    mLastTime = 0;
                    if (!isFullScreen6) {
                        fullScale6();
                    } else {
                        smallScale6();
                    }
                    isFullScreen6 = !isFullScreen6;
                }
                break;
            case R.id.video7:
                mLastTime = mCurTime;
                mCurTime = System.currentTimeMillis();
                if (mCurTime - mLastTime < 300) {
                    mCurTime = 0;
                    mLastTime = 0;
                    if (!isFullScreen7) {
                        fullScale7();
                    } else {
                        smallScale7();
                    }
                    isFullScreen7 = !isFullScreen7;
                }
                break;
            case R.id.btn_7:
                if (vanManager != null && vanManager.isOpenUart4()) {
                    vanManager.sendData2Uart7("ttyS7".getBytes());
                }
                break;
            case R.id.btn_take_picture6:
                takePicture6();
                break;
            case R.id.btn_record6:
                record6();
                break;
            case R.id.btn_take_picture7:
                takePicture7();
                break;
            case R.id.btn_record7:
                record7();
                break;
            case R.id.btn_volume_plug:
                if (mAudioManager != null) {
                    mCurrentVolume++;
                    if (mCurrentVolume > mMaxVolume) {
                        mCurrentVolume = mMaxVolume;
                    }
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    updateVolumes();
                }
                break;
            case R.id.btn_volume_sub:
                if (mAudioManager != null) {
                    mCurrentVolume--;
                    if (mCurrentVolume <= 0) {
                        mCurrentVolume = 0;
                    }
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    updateVolumes();
                }
                break;
            case R.id.btn_gps:
                startActivity(new Intent(AllActivity2.this, GPSActivity.class));
                break;
            case R.id.btn_dashboard:
                startActivity(new Intent(AllActivity2.this, DashboardActivity.class));
                break;
        }
    }

    private void fullScale6() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(720, 576);
        video6.setLayoutParams(params);
    }

    private void smallScale6() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(180, 144);
        params.topMargin = 53;
        params.leftMargin = 310;
        video6.setLayoutParams(params);
    }

    private void fullScale7() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(720, 576);
        video7.setLayoutParams(params);
        tll_video6.setVisibility(View.GONE);
        ll_video6.setVisibility(View.GONE);
        video6.setVisibility(View.GONE);
    }

    private void smallScale7() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(180, 144);
        params.topMargin = 53;
        params.leftMargin = 560;
        video7.setLayoutParams(params);
        tll_video6.setVisibility(View.VISIBLE);
        ll_video6.setVisibility(View.VISIBLE);
        video6.setVisibility(View.VISIBLE);
    }

    private boolean mAccStatus = false;
    private String mVersion = "NULL";
    private String mVoltage = "NULL";
    private boolean mGpio_rada = false;
    private boolean mGpio_mileage = false;

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
                            mAccStatus = true;
                            updateMCUText();
                            break;
                        case TAccOff:
                            mAccStatus = false;
                            updateMCUText();
                            break;
                        case TMcuVersion:
                            mVersion = new String(bytes);
                            updateMCUText();
                            break;
                        case TMcuVoltage:
                            mVoltage = new String(bytes);
                            updateMCUText();
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
                            handleOBD(bytes);
                            break;
                        case TDataJ1939:
                            break;
                        case TUnknow:
                            break;
                        case TGPIO:
                            if (bytes[0] == 0x12) {
                                mGpio_rada = bytes[1] == 0x01;
                            } else if (bytes[0] == 0x22) {
                                mGpio_mileage = bytes[1] == 0x01;
                            }
                            updateMCUText();
                            break;
                        //case TAccStatus:                            break;
                    }
                }
            });
            mHandler.sendEmptyMessageDelayed(0, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMCUText() {
        if (tvMCUInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMCUInfo.setText("MCUVersion:" + mVersion + "\nVoltage:" + mVoltage + "\nAccStatus:" + mAccStatus + "\nGPIORada:" + mGpio_rada + "\nGPIOMileage:" + mGpio_mileage);
                }
            });
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

    private void handleOBD(byte[] bytes) {
        if (isStart) {
            updateOBDText("connect success");
            App.Companion.setAECUType((byte) (bytes[3] - (byte) 0x08));
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
                sendOBDData(new byte[]{0x01, 0x07, App.Companion.getAECUType(), 0x00, 0x00, 0x02, 0x01, pid});
            }
        }, 5000, 1000);
    }

    private void takePicture6() {
        if (mCamera6 != null) {
            mCamera6.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "done:" + pictureFile.getAbsolutePath());
                    Toast.makeText(AllActivity2.this, "take done 6", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void takePicture7() {
        if (mCamera7 != null) {
            mCamera7.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "done:" + pictureFile.getAbsolutePath());
                    Toast.makeText(AllActivity2.this, "take done 7", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void record6() {
        if (isRecording6) {
            tvRecordingTime6.stop();
            tvRecordingTime6.setVisibility(View.INVISIBLE);
            isRecording6 = false;
            btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            stopRecording6();
        } else {
            if (startRecording6()) {
                tvRecordingTime6.setVisibility(View.VISIBLE);
                tvRecordingTime6.setBase(SystemClock.elapsedRealtime());
                tvRecordingTime6.start();
                isRecording6 = true;
                btnRecord6.setBackgroundResource(R.drawable.ic_stop_black_24dp);
            } else {
                tvRecordingTime6.stop();
                tvRecordingTime6.setVisibility(View.INVISIBLE);
                isRecording6 = false;
                btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            }
        }
    }

    private void record7() {
        if (isRecording7) {
            tvRecordingTime7.stop();
            tvRecordingTime7.setVisibility(View.INVISIBLE);
            isRecording7 = false;
            btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            stopRecording7();
        } else {
            if (startRecording7()) {
                tvRecordingTime7.setVisibility(View.VISIBLE);
                tvRecordingTime7.setBase(SystemClock.elapsedRealtime());
                tvRecordingTime7.start();
                isRecording7 = true;
                btnRecord7.setBackgroundResource(R.drawable.ic_stop_black_24dp);
            } else {
                tvRecordingTime7.stop();
                tvRecordingTime7.setVisibility(View.INVISIBLE);
                isRecording7 = false;
                btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            }
        }
    }

    private MediaRecorder mMediaRecorder6;
    private MediaRecorder mMediaRecorder7;

    public boolean startRecording6() {
        if (prepareVideoRecorder6()) {
            mMediaRecorder6.start();
            return true;
        } else {
            releaseMediaRecorder6();
        }
        return false;
    }


    public void stopRecording6() {
        if (mMediaRecorder6 != null) {
            mMediaRecorder6.stop();
        }
        releaseMediaRecorder6();
    }

    public boolean startRecording7() {
        if (prepareVideoRecorder7()) {
            mMediaRecorder7.start();
            return true;
        } else {
            releaseMediaRecorder7();
        }
        return false;
    }


    public void stopRecording7() {
        if (mMediaRecorder7 != null) {
            mMediaRecorder7.stop();
        }
        releaseMediaRecorder7();
    }

    private boolean prepareVideoRecorder6() {
        try {
            mMediaRecorder6 = new MediaRecorder(1);
            mCamera6.unlock();
            mMediaRecorder6.setCamera(mCamera6);
            mMediaRecorder6.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder6.setOutputFormat(8);
            mMediaRecorder6.setVideoFrameRate(30);
            mMediaRecorder6.setVideoSize(720, 576);
            mMediaRecorder6.setVideoEncodingBitRate(6000000);//6M
            mMediaRecorder6.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder6.setOutputFile(FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO).toString());
            mMediaRecorder6.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
            return false;
        }
        return true;
    }

    private boolean prepareVideoRecorder7() {
        try {
            mMediaRecorder7 = new MediaRecorder(1);
            mCamera7.unlock();
            mMediaRecorder7.setCamera(mCamera7);
            mMediaRecorder7.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder7.setOutputFormat(8);
            mMediaRecorder7.setVideoFrameRate(30);
            mMediaRecorder7.setVideoSize(720, 576);
            mMediaRecorder7.setVideoEncodingBitRate(6000000);//6M
            mMediaRecorder7.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder7.setOutputFile(FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO).toString());
            mMediaRecorder7.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder6() {
        if (mMediaRecorder6 != null) {
            mMediaRecorder6.reset();
            mMediaRecorder6.release();
            mMediaRecorder6 = null;
            mCamera6.lock();
        }
    }

    private void releaseMediaRecorder7() {
        if (mMediaRecorder7 != null) {
            mMediaRecorder7.reset();
            mMediaRecorder7.release();
            mMediaRecorder7 = null;
            mCamera7.lock();
        }
    }
/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            if (isRecording6) {
                tvRecordingTime6.setVisibility(View.INVISIBLE);
                isRecording6 = false;
                btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                tvRecordingTime6.stop();
                stopRecording6();
            }
            if (isRecording7) {
                tvRecordingTime7.setVisibility(View.INVISIBLE);
                isRecording7 = false;
                btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                tvRecordingTime7.stop();
                stopRecording7();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPreview();
        finish();
    }*/
}