// IRemoteServiceCallBack.aidl
package com.unistrong.guard;

// Declare any non-default types here with import statements

interface IRemoteServiceCallBack {
    void valueChanged(in byte[] data);
}
