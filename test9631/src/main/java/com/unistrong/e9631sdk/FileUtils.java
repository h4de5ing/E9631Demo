package com.unistrong.e9631sdk;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
    public static void saveDataInfo2File(String filename, String string) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        try {
            String time = formatter.format(new Date());
            String fileName = "data-" + filename + "-" + time + ".log";
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Gh0stCrash" + File.separator;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName, true);
            fos.write("----------\n".getBytes());
            fos.write(string.getBytes());
            fos.write("\n----------".getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
