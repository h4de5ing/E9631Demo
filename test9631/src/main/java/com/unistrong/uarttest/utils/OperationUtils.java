package com.unistrong.uarttest.utils;

/**
 * Created by Gh0st on 2017/7/21.
 */

public class OperationUtils {
    public static String Hex2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    //界面显示或者日志
    public static String hexAddUI(String hexStr, int i) {
        String binaryString = addBinary(hexString2binaryString(hexStr), hexString2binaryString(String.valueOf(i)));
        return binaryString2hexString(binaryString);//2进制转换成16进制,用作UI显示使用
    }

    //实际发送的数据
    public static byte[] hexAddByteArray(String hexStr, int i) {
        String binaryString = addBinary(hexString2binaryString(hexStr), hexString2binaryString(String.valueOf(i)));
        String hexString = binaryString2hexString(binaryString);
        byte[] data = new byte[hexString.length() / 2];
        for (int j = 0; j < data.length; j++) {
            Integer integer = Integer.valueOf(hexString.substring(j * 2, j * 2 + 2), 16);
            data[j] = (byte) (integer & 0xff);
        }
        return data;
    }

    private static String binaryString2hexString(String bString) {
        if (bString.length() % 8 != 0) {
            String sbuwei = "00000000";
            bString = sbuwei.substring(0, sbuwei.length() - bString.length() % 8) + bString;
        }
        StringBuilder tmp = new StringBuilder();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }

    private static String hexString2binaryString(String hexString) {
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    private static String addBinary(String a, String b) {
        int carry = 0;
        int sum = 0;
        int opa = 0;
        int opb = 0;
        StringBuilder result = new StringBuilder();
        while (a.length() != b.length()) {
            if (a.length() > b.length()) {
                b = "0" + b;
            } else {
                a = "0" + a;
            }
        }
        for (int i = a.length() - 1; i >= 0; i--) {
            opa = a.charAt(i) - '0';
            opb = b.charAt(i) - '0';
            sum = opa + opb + carry;
            if (sum >= 2) {
                result.append((char) (sum - 2 + '0'));
                carry = 1;
            } else {
                result.append((char) (sum + '0'));
                carry = 0;
            }
        }
        if (carry == 1) {
            result.append("1");
        }
        return result.reverse().toString();
    }
}
