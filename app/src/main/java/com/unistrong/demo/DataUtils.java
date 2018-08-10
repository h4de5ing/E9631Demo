package com.unistrong.demo;

/**
 * Created by John on 2018/3/1.
 */

public class DataUtils {
    public static String saveHex2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (byte aData : data) {
            int value = aData & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    public static String saveHex2StringNoSpace(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (byte aData : data) {
            int value = aData & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]);
        }
        return sb.toString();
    }

    public static String byte2String(byte data) {
        StringBuilder sb = new StringBuilder();
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        int value = data & 0xff;
        sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        return sb.toString().trim();
    }

    public static byte string2byte(String byteString) {
        byte b = 0;
        if (byteString.length() == 2) {
            b = (byte) (Integer.valueOf(byteString.substring(0, 2), 16) & 0xff);
        }
        return b;
    }

    public static byte[] cutByteArray(byte[] data, int start, int stop) {
        byte[] newData = null;
        if (data != null && data.length > 0 && stop <= data.length && stop - start > 0) {
            newData = new byte[stop - start];
            System.arraycopy(data, start, newData, 0, stop - start);
        }
        return newData;
    }

    public static int byte2int(byte b) {
        return b & 0xff;
    }

    public static String getDataMode(byte mode) {
        String modeTip;
        switch (mode) {
            case 0x00:
                modeTip = "Command mode";
                break;
            case 0x01:
                modeTip = "J1939 mode";
                break;
            case 0x02:
                modeTip = "OBD mode";
                break;
            case 0x03:
                modeTip = "Can mode";
                break;
            default:
                modeTip = "unknow";
                break;
        }
        return modeTip;
    }

    public static byte[] int2byteArray(String s) {
        byte[] data;
        data = new byte[s.length() / 2];
        for (int j = 0; j < data.length; j++) {
            data[j] = (byte) (Integer.valueOf(s.substring(j * 2, j * 2 + 2), 16) & 0xff);
        }
        return data;
    }

    //dec(170)=hex(AA)
    protected static String IntToHex(int n) {
        char[] ch = new char[20];
        int nIndex = 0;
        while (true) {
            int m = n / 16;
            int k = n % 16;
            if (k == 15)
                ch[nIndex] = 'F';
            else if (k == 14)
                ch[nIndex] = 'E';
            else if (k == 13)
                ch[nIndex] = 'D';
            else if (k == 12)
                ch[nIndex] = 'C';
            else if (k == 11)
                ch[nIndex] = 'B';
            else if (k == 10)
                ch[nIndex] = 'A';
            else
                ch[nIndex] = (char) ('0' + k);
            nIndex++;
            if (m == 0)
                break;
            n = m;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(ch, 0, nIndex);
        sb.reverse();
        return sb.toString();
    }
}
