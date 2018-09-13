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
                  switch (dataType) {
                      //detail received data
                  }
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
        case TAccStatus://Deprecated
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


## Method description

### CommunicationService Communication with MCU

```
getInstance();//get a communication instance
setShutdownCountTime(12);//shut down the countdown [10~30Second]
bind();//unbind communiction instance
getData();//a interface for get data,return data and data type.
send(Command.Send.Version());//send command to MCU
sendJ1939(data);//send j1939 data
sendCan(id,data);//send can data,4 Byte id，8 Byte data;
sendOBD(data);//send obd data,ISO15765 11bit/29bit
unbind();//unbind communiction

```

### DataType received data type

```
TAccOn    //when got acc on
TAccOff   //when got acc off
TMcuVersion //return version information,when we send Command.Send.Version();
TMcuVoltage //return voltage information,when we sned Command.Send.Version();
TCan250 // got that,mean setting can 250K succcess;
TCan500 //  got that,mean setting can 500k success;
TDataMode // got that,when search data mode or setting [can | obd |j1939] success;
TDataCan //got can data;
TDataOBD //got obd data;
TDataJ1939 //got j1939 data;
TChannel  //got that,when search can channe or setting [channel 1|channel 2] success;
TUnknow //don't support data or error data;
TGPIO  //GPIO 0x12 radar/0x22 mileage
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

## Uart SDK

### Add the dependency

Maximum support baud rate is 115200:
supoport:2400 4800 9600 19200 57600 115200

uart sdk source in module uartsdk


```
dependencies {
     implementation project(':uartsdk')
}
```

### open Uart

```
    @Override
protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      vanManager = new VanManager();
      boolean uart4opend = vanManager.openUart4(115200);
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

## Method description

### VanManager manager all uart
```
rs232
openUart4(115200);//return true,when serial port /dev/ttyS4 open success
sendData2Uart4(data);//send data to serial port /dev/ttyS4
closeUart4();//close serial port /dev/ttyS4

openUart6(115200);//return true,when serial port /dev/ttyS6 open success
sendData2Uart6(data);//send data to serial port /dev/ttyS6
closeUart6();//close serial port /dev/ttyS6


rs485
openUart7(115200);//return true,when serial port /dev/ttyS7 open success
sendData2Uart7(data);//send data to serial port /dev/ttyS7
closeUart7();//close serial port /dev/ttyS7
```

# Issues
[issues](https://github.com/h4de5ing/E9631Demo/issues)
email:moxi1992@gmail.com

```
update log
e9631_can_sdk_v1.2.jar
1.add can id filter/cancel filter
2.obd filter/j1939filter

e9631_can_sdk_v1.3.jar
1. replace activity context in Context

e9631_can_sdk_v1.4.jar
1.add can baud 125k(just can mode)

 e9631_can_sdk_v1.5.jar
1.optimization  received j1939 id ushr 3

 e9631_can_sdk_v1.6.jar
1.optimization can id   -> last MCU version 2.17

e9631_can_sdk_v1.7.jar
1.optimization postData2()   -> last MCU version 2.17

app log
1.7 ->  last MCU version 2.12
 can id filter

1.8 -> last MCU version 2.13
 filter can id
 filter j1939 pgn
 multiple filter max 10

 1.9 -> last MCU version 2.16
 e9631_can_sdk_v1.4.jar

 2.0 -> last MCU version 2.17

 ```