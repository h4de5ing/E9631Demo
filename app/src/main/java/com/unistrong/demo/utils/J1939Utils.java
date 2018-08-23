package com.unistrong.demo.utils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Gh0st on 2017/7/17.
 * J1939协议处理相关的工具类
 */

public class J1939Utils {
    public static String DebugHexByte2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        int CurrentType = 2;
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xff;
            if (i == CurrentType) {
                sb.append("type:");
            } else if (i == CurrentType + 1) {
                sb.append("currentLength:");
            } else if (i == CurrentType + 3) {
                sb.append("data:");
            } else if (i == data.length - 4) {
                sb.append("check:");
            }
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    public static String saveHex2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    /**
     * @param s input string like : 000102030405060708
     * @return byte[] b={0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08}
     */
    public static byte[] int2bytes2(String s) {
        byte[] data;
        try {
            s = s.replace(" ", "");
            if (s.length() % 2 != 0) {
                s = s.substring(0, s.length() - 1) + "0" + s.substring(s.length() - 1, s.length());
            }
            data = new byte[s.length() / 2];
            for (int j = 0; j < data.length; j++) {
                data[j] = (byte) (Integer.valueOf(s.substring(j * 2, j * 2 + 2), 16) & 0xff);
            }
        } catch (Exception e) {
            e.printStackTrace();//NumberFormatException
            data = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04};
        }
        return data;
    }

    /**
     * @param data 0xf0
     * @return f0
     */
    public static String byte2String(byte data) {
        StringBuilder sb = new StringBuilder();
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        int value = data & 0xff;
        sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        return sb.toString().trim();
    }

    /**
     * @param byteString f0
     * @return 0xf0
     */
    public static byte string2byte(String byteString) {
        byte b = 0;
        if (byteString.length() == 2) {
            b = (byte) (Integer.valueOf(byteString.substring(0, 2), 16) & 0xff);
        }
        return b;
    }

    public static int byte2int(byte b) {
        return b & 0xff;
    }

    public static int moveRight(byte data, int length) {
        return data >> length;
    }

    public static String getCurrentDateTimeString() {
        DateFormat format = new SimpleDateFormat("[HH:mm:ss.SSS]", Locale.CHINA);
        return format.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 245 -> 100
     * 61440 -> F000
     *
     * @param n 进制
     * @return 16进制字符串
     */
    public static String int2HexString(int n) {
        return RadixUtils.IntToHex(n);
    }

    /**
     * input:100 -> 0x10 0x00 -> 245
     * input:F000 -> 0xF0 0x00 -> 61440
     *
     * @param hexString F000
     * @return 61440
     */
    public static int hexString2Int(String hexString) {
        return RadixUtils.HexToInt(hexString);
    }

    /**
     * @param data  0x00 0x01 0x02 0x03
     * @param start 0
     * @param stop  2
     * @return 0x00 0x01
     */
    public static byte[] cutByteArray(byte[] data, int start, int stop) {
        byte[] newData = null;
        if (data != null && data.length > 0 && stop <= data.length && stop - start > 0) {
            newData = new byte[stop - start];
            System.arraycopy(data, start, newData, 0, stop - start);
        }
        return newData;
    }

    public static byte[] byteArrayAddByteArray(byte[] id, byte[] data) {
        byte[] resultData = new byte[id.length + 1 + data.length];
        System.arraycopy(id, 0, resultData, 0, id.length);
        resultData[id.length] = (byte) data.length;
        System.arraycopy(data, 0, resultData, id.length + 1, data.length);
        return resultData;
    }

    /**
     * 帧类型 标准数据帧 标准远程帧 扩展数据帧 扩展远程帧
     * 判断远程帧 数据帧
     *
     * @param id
     * @return
     */
    public static int getFrameType(byte[] id) {
        int intId = 0;
        for (int i = 0; i < id.length; i++) {
            intId = intId | id[i] << (8 * (id.length - 1 - i));
        }
        return intId & 0x0002;
    }

    /**
     * id类型
     * 判断标准帧  扩展帧
     *
     * @param id
     * @return
     */
    public static int getFrameFormat(byte[] id) {
        int intId = 0;
        for (int i = 0; i < id.length; i++) {
            intId = intId | id[i] << (8 * (id.length - 1 - i));
        }
        return intId & 0x0004;
    }
}
