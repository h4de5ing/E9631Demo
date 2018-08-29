package com.unistrong.uarttest.utils;

import com.van.uart.UartManager;

/**
 * Created by Gh0st on 2017/7/17.
 */

public class BaudUtils {
    public static UartManager.BaudRate getBaudRate(int baudrate) {
        UartManager.BaudRate value = null;
        switch (baudrate) {
            case 9600:
                value = UartManager.BaudRate.B9600;
                break;
            case 19200:
                value = UartManager.BaudRate.B19200;
                break;
            case 57600:
                value = UartManager.BaudRate.B57600;
                break;
            case 115200:
                value = UartManager.BaudRate.B115200;
                break;
            case 230400:
                value = UartManager.BaudRate.B230400;
                break;
            case 460800:
                value = UartManager.BaudRate.B460800;
                break;
            case 500000:
                value = UartManager.BaudRate.B500000;
                break;
            case 576000:
                value = UartManager.BaudRate.B576000;
                break;
            case 921600:
                value = UartManager.BaudRate.B921600;
                break;
        }

        return value;
    }


    public static int getBaudRate(UartManager.BaudRate baudRate) {
        int i = 0;
        switch (baudRate) {
            case B9600:
                i = 9600;
                break;
            case B19200:
                i = 19200;
                break;
            case B57600:
                i = 57600;
                break;
            case B115200:
                i = 115200;
                break;
            case B230400:
                i = 230400;
                break;
            case B460800:
                i = 460800;
                break;
//            case B500000:
//                i = 500000;
//                break;
//            case B576000:
//                i = 576000;
//                break;
//            case B921600:
//                i = 921600;
//                break;
        }
        return i;
    }
}
