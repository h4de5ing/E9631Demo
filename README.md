# [E9631 SDK document](https://github.com/h4de5ing/E9631Demo)

- English
- 中文

## Can SDK

### Add the dependency

add file libs/e9631_can_sdk_v1.0.jar

```
dependencies {
      implementation files('libs/e9631_can_sdk_v1.0.jar')
}
```

### bindService

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
          mService = CommunicationService.getInstance(this);
          mService.setShutdownCountTime(12);//setting shutdownCountTime
          mService.bind();
          mService.getData(new CommunicationService.IProcessData() {
              @Override
              public void process(byte[] bytes, DataType dataType) {
                    handle(bytes,dataType);//handle data
              }
          });
      } catch (Exception e) {
          e.printStackTrace();
      }
}
```

### sendData

```
mService.send(Command.Send.Version());//send Command
mService.sendJ1939(data);//J1939 data
mService.sendCan(id, data);//can data
mService.sendOBD(data);//obd data
```

### unbindService

```
@Override
protected void onDestroy() {
    super.onDestroy();
    if (mService != null) {
        try {
            mService.unbind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Command

```
Command.Send.SearchAccStatus();// search MCU current acc status

Command.Send.SearchChannel();//search MCU current channel
Command.Send.Channel1();//setting MCU channel 1
Command.Send.Channel2();//setting MCU channel 2

Command.Send.SearchMode(); //search MCU support protocol
Command.Send.ModeJ1939();//setting MCU J1939 protocol
Command.Send.ModeOBD(); //setting MCU OBDII protocol
Command.Send.ModeCan(); //setting MCU can protocol

Command.Send.Switch500K(); //setting MCU 500K
Command.Send.Switch250K(); //setting MCU 250K

Command.Send.Version(); //get MCU firmware version
Command.Send.Voltage(); //get car voltage
```

### handle received data

```
private void handle(byte[] bytes, DataType dataType) {
    switch (dataType) {
        case TAccOn:
            updateText("ACC ON");
            break;
        case TAccOff:
            updateText("ACC OFF");
            break;
        case TMcuVersion:
            updateText("Version:" + new String(bytes));
            break;
        case TMcuVoltage:
            updateText("Voltage:" + new String(bytes));
            break;
        case TCan250:
            updateText("250K setting success");
            break;
        case TCan500:
            updateText("500K setting success");
            break;
        case TDataMode:
            updateText("setting " + DataUtils.getDataMode(bytes[0]) + " success");
            break;
        case TDataCan:
            //we get can data
            //handle can data
            break;
        case TDataOBD:
            //we get obd data
            break;
        case TDataJ1939:
            //we get J1939 data
            break;
        case TChannel:
            updateText("current channel " + bytes[0]);
            break;
        case TUnknow://undefined data type,maybe error data
            break;
        case TGPIO:
            if (bytes[0] == 0x12) {
                updateText("GPIO Radar");
            } else if (bytes[0] == 0x22) {
                updateText("GPIO Mileage");
            }
            break;
    }
}
```

## Uart SDK

### Add the dependency

add file libs/uartsdk_v1.0.jar  
add file jniLibs/armeabi/libVanUart.so

```
dependencies {
      implementation files('libs/uartsdk_v1.0.jar')
}
```

### open Uart

```
    @Override
protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      vanManager = new VanManager();
      boolean uart4opend = vanManager.openUart4();
      vanManager.uartData4(new ProcessData() {
          @Override
          public void process(byte[] bytes, int len) {
              //get uart data and data length
          }
      });
}
```

### send data

```
vanManager.sendData2Uart4(data);
```

### close uart

```
@Override
protected void onDestroy() {
    super.onDestroy();
    vanManager.closeUart4();
}
```

# Issues
[issues](https://github.com/h4de5ing/E9631Demo/issues)

[email](moxi1992@gmail.com)
