package com.unistrong.demo.cvbsVideo;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Constants {
    public static int zhi = 1;// 0 NTSC  1 PAL
    public static String path6 = "/sys/devices/soc.0/1c33000.tvd2/tvd2_attr/tvd_system";
    public static String path7 = "/sys/devices/soc.0/1c34000.tvd3/tvd3_attr/tvd_system";
    public static final String STANDARD_KEY = "standard";

    public static void setStandard(String path, int value) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            Log.i("gh0st", "path:" + path + ",write:" + value);
            writer.write("#" + value);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
