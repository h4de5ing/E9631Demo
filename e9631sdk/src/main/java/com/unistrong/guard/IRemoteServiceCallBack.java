/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\java_code\\elm\\e9631service\\src\\main\\aidl\\com\\unistrong\\guard\\IRemoteServiceCallBack.aidl
 */
package com.unistrong.guard;
// Declare any non-default types here with import statements

public interface IRemoteServiceCallBack extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements IRemoteServiceCallBack
{
private static final String DESCRIPTOR = "com.unistrong.guard.IRemoteServiceCallBack";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.unistrong.guard.IRemoteServiceCallBack interface,
 * generating a proxy if needed.
 */
public static IRemoteServiceCallBack asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof IRemoteServiceCallBack))) {
return ((IRemoteServiceCallBack)iin);
}
return new Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_valueChanged:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.valueChanged(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements IRemoteServiceCallBack
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void valueChanged(byte[] data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(data);
mRemote.transact(Stub.TRANSACTION_valueChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_valueChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void valueChanged(byte[] data) throws android.os.RemoteException;
}
