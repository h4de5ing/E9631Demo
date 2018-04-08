package com.unistrong.demo.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.unistrong.demo.BaseActivity;
import com.unistrong.demo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GPSActivity extends BaseActivity {
    boolean initChoiceSets[] = {false, false, false, false, false, false};
    String FileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/gps.txt";
    TextView mInView, mInUse;
    TextView mGPSInView, mBDInView;
    TextView mttff;
    SingleView mGPSView;
    EditText mnmea;

    LocationManager mLM;
    private static final int REQUESTPermissionCode = 10086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_main);
        mLM = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        initViews();
        SharedPreferences sp = getSharedPreferences("test_gps", Context.MODE_PRIVATE);
        initChoiceSets[5] = sp.getBoolean("save_nmea", false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTPermissionCode);
        }
        initTimer();
    }

    int mMS = 0;
    Timer timer;

    private void initTimer() {

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mMS += 100;
            }
        }, 0, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == REQUESTPermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Thanks for you granted permissions!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "The application may crash without permissions!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerLisenter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mInView = (TextView) this.findViewById(R.id.in_view);
        mInUse = (TextView) this.findViewById(R.id.in_use);
        mGPSInView = (TextView) this.findViewById(R.id.gps_in_view_use);
        mBDInView = (TextView) this.findViewById(R.id.bd_in_view_use);
        mttff = (TextView) this.findViewById(R.id.ttff);
        mGPSView = (SingleView) this.findViewById(R.id.gps_bar);
        mnmea = (EditText) this.findViewById(R.id.nmea);
        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLM != null) {
                    if (mLM != null) {
                        if (ActivityCompat.checkSelfPermission(GPSActivity.this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(GPSActivity.this, "没有\"访问额外的位置提供命令\"权限", Toast.LENGTH_LONG).show();
                            return;
                        }
                        showDialog();
                    }
                }
            }
        });
    }

    private void showDialog() {
        final String[] items = {"1.冷启动", "2.nmea打开", "3.Glonass模式", "4.北斗模式", "5.省电模式打开", "保存nmea数据到gps.txt文件中"};
        AlertDialog.Builder multiChoiceDialog = new AlertDialog.Builder(this);
        multiChoiceDialog.setTitle("GPS设置");
        multiChoiceDialog.setMultiChoiceItems(items, initChoiceSets, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which,
                                boolean isChecked) {
                initChoiceSets[which] = isChecked;
            }
        });
        multiChoiceDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("cold_start", initChoiceSets[0]);
                bundle.putBoolean("nmea_open", initChoiceSets[1]);
                bundle.putBoolean("glonass_mode", initChoiceSets[2]);
                bundle.putBoolean("beidou_mode", initChoiceSets[3]);
                bundle.putBoolean("save_power_open", initChoiceSets[4]);
                mLM.sendExtraCommand("gps", "unis_extra_data", bundle); //冷启动
                SharedPreferences sp = getSharedPreferences("test_gps", Context.MODE_PRIVATE);
                Editor e = sp.edit();
                e.putBoolean("save_nmea", initChoiceSets[5]);
                e.commit();
                try {
                    if (fo != null && !initChoiceSets[5]) {
                        fo.close();
                        fo = null;
                    }
                    if (out != null && !initChoiceSets[5]) {
                        out.close();
                        out = null;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //TTFF == 0
                if (mttff != null) {
                    mTTFFTime = "";
                    mttff.setText(getString(R.string.timetofirstfix) + mTTFFTime);
                }
            }
        });
        multiChoiceDialog.setNegativeButton("取消", null);
        multiChoiceDialog.show();
    }

    // 注册GPS 监听事件
    private void registerLisenter() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "没有GPS权限", Toast.LENGTH_LONG).show();
            return;
        }
        mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mLL);
        mLM.addGpsStatusListener(mGL);
        mLM.addNmeaListener(mNL);
    }

    // 撤销
    private void unRegisterListener() {
        mLM.removeUpdates(mLL);
        mLM.removeGpsStatusListener(mGL);
        mLM.removeNmeaListener(mNL);
    }

    // 事件
    private LocationListener mLL = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            long time = location.getTime();
            Date date = new Date(time);
            //Log.e("WGP","DDDDDDDDD="+time+","+date.getHours()+":"+date.getMinutes());
            String hourStr = date.getHours() < 10 ? "0" + date.getHours() : date.getHours() + "";
            String timeStr = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes() + "";
            //locationtime.setText(hourStr + ":" + timeStr);
        }

        @Override
        public void onProviderDisabled(String provider) {
            gpsList.clear();
            mGPSView.updateView(gpsList);
            mInView.setText(getString(R.string.in_view));
            mInUse.setText(getString(R.string.in_use));
            mGPSInView.setText(getString(R.string.gps_in_view_use));
            mBDInView.setText(getString(R.string.bd_in_view_use));
            number = 0;
            mnmea.setText("");
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private int number = 0;
    private NmeaListener mNL = new NmeaListener() {
        @Override
        public void onNmeaReceived(long arg0, String nmea) {
            if (number % 500 == 0) {
                mnmea.setText(nmea);
            } else {
                mnmea.append(nmea);
            }
            number++;

            if (initChoiceSets[5]) {
                //mSetView.saveNmea(nmea);
                saveNmea(nmea);
            }
            int offset = mnmea.getLineCount() * mnmea.getLineHeight() - mnmea.getHeight();
            mnmea.scrollTo(0, offset > 0 ? offset : 0);
        }
    };

    private PrintWriter out = null;
    private FileOutputStream fo = null;

    // 保存NMEA数据
    public void saveNmea(String str) {
        if (str == null)
            return;
        try {
            if (fo == null && out == null) {
                File file = new File(FileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fo = new FileOutputStream(file, true);
                out = new PrintWriter(new OutputStreamWriter(fo));
            }
            out.write(str);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String mTTFFTime = "";
    private List<GpsSatellite> gpsList = new ArrayList<GpsSatellite>();
    //private List<GpsSatellite> bdList  = new ArrayList<GpsSatellite>();
    private GpsStatus.Listener mGL = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            @SuppressLint("MissingPermission") GpsStatus gs = mLM.getGpsStatus(null);
            if (gs != null) {
                mTTFFTime = gs.getTimeToFirstFix() / 1000.0 + "s";
                Log.i("gh0st", "mTTFFTime 0 " + mTTFFTime);
            }
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX://第一次定位
                    if (gs != null) {
                        mTTFFTime = gs.getTimeToFirstFix() / 1000.0 + " S";
                        Log.i("gh0st", "mTTFFTime 1 " + mTTFFTime);
                    }
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS://卫星状态改变
                    gpsList.clear();
                    Iterable<GpsSatellite> sates = gs.getSatellites();
                    if (sates != null) {
                        int inViewNum = 0;
                        int inUseNum = 0;
                        int gpsViewNum = 0;
                        int gpsUseNum = 0;
                        int bdUseNum = 0;
                        int bdViewNum = 0;
                        for (GpsSatellite s : sates) {
                            if (s.getSnr() > 0) {
                                gpsList.add(s);
                                inViewNum++;
                                if (s.usedInFix()) {
                                    inUseNum++;
                                }
                                if (s.getPrn() <= 32) {
                                    gpsViewNum++;
                                    if (s.usedInFix()) {
                                        gpsUseNum++;
                                    }
                                } else {
                                    bdViewNum++;
                                    if (s.usedInFix()) {
                                        bdUseNum++;
                                    }
                                }
                            }
                        }
                        mGPSView.updateView(gpsList);
                        mInView.setText(getString(R.string.in_view) + inViewNum);
                        mInUse.setText(getString(R.string.in_use) + inUseNum);
                        mGPSInView.setText(getString(R.string.gps_in_view_use) + gpsUseNum + "/" + gpsViewNum);
                        mBDInView.setText(getString(R.string.bd_in_view_use) + bdUseNum + "/" + bdViewNum);
/*                        if (inUseNum >= 3) {
                            if (timer != null) {
                                timer.cancel();
                            }
                            mTTFFTime = mMS / 1000.0 + " S";
                        }*/
                        mttff.setText(getString(R.string.timetofirstfix) + mTTFFTime);
                    }
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gps_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                if (mLM != null) {
                    showDialog();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
