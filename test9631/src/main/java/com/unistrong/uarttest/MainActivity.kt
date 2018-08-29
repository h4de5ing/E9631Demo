package com.unistrong.uarttest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.unistrong.uarttest.uart2.Uart2Activity

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        val tvVersion = findViewById<TextView>(R.id.tv_version)
        tvVersion.text = "Version:${packageManager.getPackageInfo(packageName, 0).versionName}"
        //startActivity(Intent(this@Uart2Activity, CanActivity::class.java))
        findViewById<Button>(R.id.btn_can).setOnClickListener {
            startActivity(Intent(this@MainActivity, CanActivity::class.java))
        }
        findViewById<Button>(R.id.btn_uart).setOnClickListener {
            startActivity(Intent(this@MainActivity, Uart2Activity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_setting) {
            startActivity(Intent(this, SettingActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
