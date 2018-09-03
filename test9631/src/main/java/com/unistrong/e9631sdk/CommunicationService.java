package com.unistrong.e9631sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.unistrong.guard.IRemoteService;
import com.unistrong.guard.IRemoteServiceCallBack;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CommunicationService {
    private static CommunicationService sCommunicationService;
    private static Context mActivity;
    private IProcessData mIProcessData;
    private static int mCountTime = 15000;

    public interface IProcessData {
        void process(byte[] data, DataType type);
    }

    public void getData(IProcessData iProcessData) {
        mIProcessData = iProcessData;
    }

    private CommunicationService() {
    }

    public static CommunicationService getInstance(Context activity) {
        if (sCommunicationService == null) {
            sCommunicationService = new CommunicationService();
            mActivity = activity;
        }
        return sCommunicationService;
    }

    private static final String IntentAction = "com.unistrong.intent.action.SHUTDOWN";

    public void setShutdownCountTime(int second) {
        if (second < 10 || second > 30) {
            mCountTime = 10 * 1000;
        } else {
            mCountTime = second * 1000;
        }
        Intent intent = new Intent();
        intent.setAction(IntentAction);
        intent.putExtra("shutdown_value", "shutdown_time");
        intent.putExtra("shutdown_time", mCountTime);
        mActivity.sendBroadcast(intent);
    }


    public void send(byte[] data) {
        if (mService != null) {
            try {
                Log.i("gh0st", "sdk :" + saveHex2String(data));
                mService.handleData(data);
            } catch (RemoteException e) {
                Log.e("gh0st", e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    private byte[] byteArrayAddByteArray(byte[] id, byte[] data) {
        byte[] resultData = new byte[id.length + data.length + 2];
        resultData[0] = 0x00;
        System.arraycopy(id, 0, resultData, 1, id.length);
        resultData[id.length + 1] = (byte) data.length;
        System.arraycopy(data, 0, resultData, id.length + 2, data.length);
        return resultData;
    }

    public void sendCan(byte[] id, byte[] data) {
        send(Command.Send.sendData(byteArrayAddByteArray(id, data), Command.SendDataType.Can));
    }

    public void sendOBD(byte[] data) {
        send(Command.Send.sendData(data, Command.SendDataType.OBDII));
    }

    public void sendJ1939(byte[] data) {
        send(Command.Send.sendData(data, Command.SendDataType.J1939));
    }


    public void bind() throws Exception {
        if (mActivity != null) {
            Intent intent = new Intent();
            intent.setAction("com.unistrong.guard.E9631Service");
            intent.setPackage("com.unistrong.guard");
            mActivity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } else {
            throw new Exception("gh0st -- bind fail , activity is Null");
        }
    }


    public void unbind() throws Exception {
        if (mActivity != null) {
            if (mService != null) {
                try {
                    mService.unregisterCallback(mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mActivity.unbindService(conn);
                mService = null;
            }
        } else {
            throw new Exception("gh0st -- unbind fail , activity is Null");
        }
    }

    public boolean isBindSuccess() {
        return mService != null;
    }

    IRemoteService mService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mService = IRemoteService.Stub.asInterface(service);
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                Log.e("gh0st", e.getMessage(), e);
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private IRemoteServiceCallBack mCallback = new IRemoteServiceCallBack.Stub() {

        @Override
        public void valueChanged(byte[] protocolData) throws RemoteException {
            ReceivePack(protocolData, protocolData.length);
        }
    };


    private int BUF_SIZE = 2048;
    private int m_nBufLen = 0;
    private byte[] m_RecvPack = new byte[BUF_SIZE];
    private byte[] m_pRecvBuf = new byte[BUF_SIZE];

    private void ReceivePack(byte buf[], int size) {
        if (m_nBufLen + size >= BUF_SIZE) {
            m_nBufLen = 0;
        }
        System.arraycopy(buf, 0, m_pRecvBuf, m_nBufLen, size);
        m_nBufLen += size;
        boolean bRet = false;
        do {
            bRet = parseData(m_pRecvBuf, m_nBufLen);
        } while (bRet);
    }

    private boolean parseData(byte[] buf, int size) {
        boolean findHead = false;
        boolean findTail = false;
        int length = 0;
        int i = 0;
        for (i = 0; i < size; ++i) {
            if (buf[i] == 0x55) {
                if (++i >= size) {
                    return false;
                }
                if (buf[i] == 0x02) {
                    findHead = true;
                } else if (buf[i] == 0x03 && findHead) {
                    findTail = true;
                    ++i;
                    break;
                } else if (buf[i] == 0x55) {
                    m_RecvPack[length++] = buf[i];
                }
            } else {
                if (findHead) {
                    m_RecvPack[length++] = buf[i];
                }
            }
        }
        if (findTail) {
            m_nBufLen -= i;
            if (m_nBufLen < 0) {
                m_nBufLen = 0;
            }
            System.arraycopy(m_pRecvBuf, i, m_pRecvBuf, 0, m_nBufLen);
            if (verify(m_RecvPack, length)) {
                postData2(m_pRecvBuf, length);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean verify(byte buf[], int size) {
        try {
            byte CK_A = 0;
            byte CK_B = 0;
            for (int i = 0; i < size - 2; ++i) {
                CK_A += buf[i];
                CK_B += CK_A;
            }
            byte A = (byte) (buf[size - 2] & 0xFF);
            byte B = (byte) (buf[size - 1] & 0xFF);
            return (CK_A == A) && (CK_B == B);
        } catch (Exception e) {
            return false;
        }
    }

    private void postData2(byte[] tempData, int size) {
        byte[] protocolData = new byte[size + 4];
        System.arraycopy(tempData, 0, protocolData, 0, protocolData.length);
        byte type = protocolData[2];
        int length = ((protocolData[3] & 0xFF) << 8 | ((protocolData[4] & 0xFF)));
        byte dataType = protocolData[5];
        byte[] data = new byte[length];
        System.arraycopy(protocolData, 5, data, 0, data.length);
        if (type == 0x21) {
            mIProcessData.process(data, DataType.TMcuVersion);
        } else if (type == 0x30 && dataType == 0x01) {//acc on
            mIProcessData.process(data, DataType.TAccOn);
        } else if (type == 0x30 && dataType == 0x00) {//acc off
            FileUtils.saveDataInfo2File("accoff", new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(SystemClock.currentThreadTimeMillis()) + "" + saveHex2String(data));
            mIProcessData.process(data, DataType.TAccOff);
        } else if (type == 0x31 && dataType == 0x12) {//can 125
            mIProcessData.process(data, DataType.TCan125);
        } else if (type == 0x31 && dataType == 0x25) {//can 250
            mIProcessData.process(data, DataType.TCan250);
        } else if (type == 0x31 && dataType == 0x50) {//can 500
            mIProcessData.process(data, DataType.TCan500);
        } else if (type == 0x33) {
            mIProcessData.process(data, DataType.TMcuVoltage);
        } else if (type == 0x41) {
            mIProcessData.process(data, DataType.TDataCan);
        } else if (type == 0x61) {
            mIProcessData.process(data, DataType.TDataOBD);
        } else if (type == 0x71) {
            mIProcessData.process(data, DataType.TDataJ1939);
        } else if (type == (byte) 0x81) {
            mIProcessData.process(data, DataType.TDataMode);
        } else if (type == (byte) 0x83) {
            mIProcessData.process(data, DataType.TChannel);
        } else if (type == (byte) (0x91)) {
            mIProcessData.process(data, DataType.TGPIO);
        } else {
            mIProcessData.process(data, DataType.TUnknow);
            Log.d("gh0st", "we don't check:" + saveHex2String(data));
        }
    }

    public static String saveHex2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (byte aData : data) {
            int value = aData & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }
}
