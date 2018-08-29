package com.unistrong.e9631sdk;

import java.util.ArrayList;
import java.util.List;


public class Command {
    private static byte[] START = new byte[]{0x55, 0x02};
    private static byte[] ENDOF = new byte[]{0x55, 0x03};
    private static byte[] VERSION = new byte[]{0x20, 0x00, 0x01, 0x33};
    private static byte[] Voltage = new byte[]{0x20, 0x00, 0x01, 0x34};
    private static byte[] Switch250K = new byte[]{0x30, 0x00, 0x01, 0x25};
    private static byte[] Switch500K = new byte[]{0x30, 0x00, 0x01, 0x50};
    private static byte[] UartBaudRate9600 = {0x30, 0x00, 0x01, 0x09};
    private static byte[] UartBaudRate19200 = {0x30, 0x00, 0x01, 0x19};
    private static byte[] UartBaudRate57600 = {0x30, 0x00, 0x01, 0x57};
    private static byte[] UartBaudRate115200 = {0x30, 0x00, 0x01, 0x11};
    private static byte[] UartBaudRate230400 = {0x30, 0x00, 0x01, 0x23};
    private static byte[] CancelShutDown = {0x30, 0x00, 0x01, 0x02};
    private static byte[] ShutDown = {0x30, 0x00, 0x01, 0x03};
    //private static byte[] update = new byte[]{0x30, 0x00, 0x01, 0x04};

    private static byte[] modeJ1939 = {(byte) 0x80, 0x00, 0x01, 0x01};
    private static byte[] modeObd = {(byte) 0x80, 0x00, 0x01, 0x02};
    private static byte[] modeCan = {(byte) 0x80, 0x00, 0x01, 0x03};
    private static byte[] modeSearch = {(byte) 0x80, 0x00, 0x01, 0x00};
    private static byte[] SearchAccStatus = {(byte) 0x90, 0x00, 0x01, 0x30};
    private static byte[] channelSearch = {(byte) 0x82, 0x00, 0x01, 0x00};
    private static byte[] channel1 = {(byte) 0x82, 0x00, 0x01, 0x01};
    private static byte[] channel2 = {(byte) 0x82, 0x00, 0x01, 0x02};

    public static class SendDataType {
        //protected static byte UCarTxRx = 0x10;
        public static byte Can = 0x40;
        //public static byte Uart = 0x51;
        public static byte J1939 = 0x70;
        public static byte OBDII = 0x60;
        protected static byte SearchAccStatus = (byte) 0x91;
    }

    public static class Send {
        public static byte[] SearchAccStatus() {
            return createProtocolPacket(SearchAccStatus);
        }

        public static byte[] SearchChannel() {
            return createProtocolPacket(channelSearch);
        }

        public static List<byte[]> Gpio() {
            List<byte[]> list = new ArrayList<>();
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x07}));//MCU_OUT1(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x04}));//MCU_OUT2(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x12}));//RADAR_IN(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x03}));//MCU_PWM2(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x06}));//MCU_PWM1(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x00}));//MCUIN1(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x01}));//MCUIN2(GPIO)
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x16}));//Mileage_pwr_en(GPIO)    en  ..enable
            list.add(createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x22}));//Mileage_mcuin(GPIO)
            return list;
        }

        public static byte[] GpioRadar() {
            return createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x12});
        }

        public static byte[] GpioMileage() {
            return createProtocolPacket(new byte[]{(byte) 0x90, 0x00, 0x01, 0x22});
        }

        public static List<String> GpioName() {
            List<String> list = new ArrayList<>();
            list.add("MCU_OUT1(GPIO)");
            list.add("MCU_OUT2(GPIO)");
            list.add("RADAR_IN(GPIO) 线接入12V电源电压");
            list.add("MCU_PWM2(GPIO) 线接入地线（GND）");
            list.add("MCU_PWM1(GPIO) 线接入地线（GND）");
            list.add("MCUIN1(GPIO) 线接入12V电源电压");
            list.add("MCUIN2(GPIO) 线接入12V电源电压");
            list.add("Mileage_pwr_en(GPIO)");
            list.add("Mileage_mcuin(GPIO) 线接入12V电源电压");
            return list;
        }

        public static byte[] Channel1() {
            return createProtocolPacket(channel1);
        }

        public static byte[] Channel2() {
            return createProtocolPacket(channel2);
        }

        public static byte[] ModeJ1939() {
            return createProtocolPacket(modeJ1939);
        }

        public static byte[] ModeOBD() {
            return createProtocolPacket(modeObd);
        }

        public static byte[] ModeCan() {
            return createProtocolPacket(modeCan);
        }

        public static byte[] SearchMode() {
            return createProtocolPacket(modeSearch);
        }

        public static byte[] Switch500K() {
            return createProtocolPacket(Switch500K);
        }

        public static byte[] Switch250K() {
            return createProtocolPacket(Switch250K);
        }

        public static byte[] Version() {
            return createProtocolPacket(VERSION);
        }

        //private static byte[] Update() { return createProtocolPacket(update);}

        public static byte[] Voltage() {
            return createProtocolPacket(Voltage);
        }


        @Deprecated
        private static byte[] CancelShutDown() {
            return createProtocolPacket(CancelShutDown);
        }

        private static byte[] ShutDown() {
            return createProtocolPacket(ShutDown);
        }

        public static byte[] sendData(byte[] data, byte type) {
            byte[] protocolData = new byte[data.length + 3];
            protocolData[0] = type;
            protocolData[1] = (byte) (data.length >> 8 & 0xFF);
            protocolData[2] = (byte) (data.length & 0xFF);
            System.arraycopy(data, 0, protocolData, 3, data.length);
            return createProtocolPacket(protocolData);
        }
    }

    private static byte[] byteArrAddByteArr(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    private static byte[] createProtocolPacket(byte[] content) {
        byte bbc[] = escapeEncode(checkbbc(content));
        byte[] bytes = new byte[bbc.length + 4];
        bytes[0] = START[0];
        bytes[1] = START[1];
        bytes[bytes.length - 2] = ENDOF[0];
        bytes[bytes.length - 1] = ENDOF[1];
        System.arraycopy(bbc, 0, bytes, 2, bbc.length);
        return bytes;
    }

    private static byte[] checkbbc(byte[] content) {
        byte checkA = 0, checkB = 0;
        for (byte aContent : content) {
            checkA += aContent;
            checkB += checkA;
        }
        byte[] bytes = new byte[content.length + 2];
        bytes[bytes.length - 2] = checkA;
        bytes[bytes.length - 1] = checkB;
        System.arraycopy(content, 0, bytes, 0, content.length);
        return bytes;
    }

    private final static byte ESCAPE = 0x55;

    private static byte[] escapeEncode(byte[] content) {
        byte[] bytes = new byte[content.length * 2];
        int a = 0, b = 0;
        while (b < content.length) {
            switch (content[b]) {
                case ESCAPE:
                    bytes[a++] = ESCAPE;
                    bytes[a++] = ESCAPE;
                    break;
                default:
                    bytes[a++] = content[b];
                    break;
            }
            b++;
        }
        byte[] bytes1 = new byte[a];
        System.arraycopy(bytes, 0, bytes1, 0, bytes1.length);
        return bytes1;
    }
}
