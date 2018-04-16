package com.unistrong.uartsdk;

import android.os.Handler;

import com.van.uart.LastError;
import com.van.uart.UartManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by John on 2018/3/9.
 */

public class VanManager {

    private static final String ttyS4 = "ttyS4";
    private static final String ttyS6 = "ttyS6";
    private static final String ttyS7 = "ttyS7";
    private static final String rs485 = "/sys/class/misc/sunxi-gps/rf-ctrl/max485_state";
    private Handler mainHandler;
    private ProcessData mProcessDataUart4;
    private ProcessData mProcessDataUart6;
    private ProcessData mProcessDataUart7;
    private UartManager manager4;
    private UartManager manager6;
    private UartManager manager7;

    public VanManager() {
        mainHandler = new Handler();
    }


    public boolean openUart4(int baud) {
        try {
            manager4 = new UartManager();
            manager4.open(ttyS4, getBaudRate(baud));
            ReadThread4 readThread = new ReadThread4();
            readThread.startMonitor();
            return true;
        } catch (LastError lastError) {
            lastError.printStackTrace();
            return false;
        }
    }

    public boolean closeUart4() {
        if (manager4 != null) {
            manager4.close();
            return true;
        }
        return false;
    }

    public boolean isOpenUart4() {
        return manager4 != null && manager4.isOpen();
    }

    public void sendData2Uart4(byte[] data) {
        try {
            if (manager4 != null) manager4.write(data, data.length);
        } catch (LastError lastError) {
            lastError.printStackTrace();
        }
    }

    public void uartData4(ProcessData processData) {
        mProcessDataUart4 = processData;
    }

    private class ReadThread4 implements Runnable {
        private Thread thread;

        public void startMonitor() {
            stopMonitor();
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void stopMonitor() {
            if (thread != null && thread.isAlive()) try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

        @Override
        public void run() {
            byte[] recv = new byte[2048];
            while (manager4.isOpen()) {
                int length = 0;
                try {
                    length = manager4.read(recv, recv.length, 50, 1);
                    if (length > 1) {
                        byte[] data = new byte[length];
                        System.arraycopy(recv, 0, data, 0, length);
                        postToMainThread(data, length, 4);
                    }
                } catch (LastError e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean openUart6(int baud) {
        try {
            manager6 = new UartManager();
            manager6.open(ttyS6, getBaudRate(baud));
            ReadThread6 readThread = new ReadThread6();
            readThread.startMonitor();
            return true;
        } catch (LastError lastError) {
            lastError.printStackTrace();
            return false;
        }
    }

    public boolean closeUart6() {
        if (manager6 != null) {
            manager6.close();
            return true;
        }
        return false;
    }

    public boolean isOpenUart6() {
        return manager6 != null && manager6.isOpen();
    }

    public void sendData2Uart6(byte[] data) {
        try {
            if (manager6 != null) manager6.write(data, data.length);
        } catch (LastError lastError) {
            lastError.printStackTrace();
        }
    }

    public void uartData6(ProcessData processData) {
        mProcessDataUart6 = processData;
    }

    private class ReadThread6 implements Runnable {
        private Thread thread;

        public void startMonitor() {
            stopMonitor();
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void stopMonitor() {
            if (thread != null && thread.isAlive()) try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

        @Override
        public void run() {
            byte[] recv = new byte[2048];
            while (manager6.isOpen()) {
                int length = 0;
                try {
                    length = manager6.read(recv, recv.length, 50, 1);
                    if (length > 1) {
                        byte[] data = new byte[length];
                        System.arraycopy(recv, 0, data, 0, length);
                        postToMainThread(data, length, 6);
                    }
                } catch (LastError e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean openUart7(int baud) {
        try {
            manager7 = new UartManager();
            manager7.open(ttyS7, getBaudRate(baud));
            ReadThread7 readThread = new ReadThread7();
            readThread.startMonitor();
            return true;
        } catch (LastError lastError) {
            lastError.printStackTrace();
            return false;
        }
    }

    public boolean closeUart7() {
        if (manager7 != null) {
            manager7.close();
            return true;
        }
        return false;
    }

    public boolean isOpenUart7() {
        return manager7 != null && manager7.isOpen();
    }

    public void sendData2Uart7(final byte[] data) {
        if (manager7 != null) {
            writeFile("1");
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        manager7.write(data, data.length);
                    } catch (LastError lastError) {
                        lastError.printStackTrace();
                    }
                }
            }, 100);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeFile("0");
                }
            }, 300);
        }
    }

    public void uartData7(ProcessData processData) {
        mProcessDataUart7 = processData;
    }

    private class ReadThread7 implements Runnable {
        private Thread thread;

        public void startMonitor() {
            stopMonitor();
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void stopMonitor() {
            if (thread != null && thread.isAlive()) try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

        @Override
        public void run() {
            byte[] recv = new byte[2048];
            while (manager7.isOpen()) {
                int length = 0;
                try {
                    length = manager7.read(recv, recv.length, 50, 1);
                    if (length > 1) {
                        byte[] data = new byte[length];
                        System.arraycopy(recv, 0, data, 0, length);
                        postToMainThread(data, length, 7);
                    }
                } catch (LastError e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void postToMainThread(byte[] receive, int length, int type) {
        switch (type) {
            case 4:
                mProcessDataUart4.process(receive, length);
                break;
            case 6:
                mProcessDataUart6.process(receive, length);
                break;
            case 7:
                mProcessDataUart7.process(receive, length);
                break;
        }
    }

    private void writeFile(String string) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(rs485)));
            writer.write(string);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UartManager.BaudRate getBaudRate(int baudrate) {
        UartManager.BaudRate value = null;
        switch (baudrate) {
            case 2400:
                value = UartManager.BaudRate.B2400;
                break;
            case 4800:
                value = UartManager.BaudRate.B4800;
                break;
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
            default:
                value = UartManager.BaudRate.B115200;
                break;
        }
        return value;
    }
}