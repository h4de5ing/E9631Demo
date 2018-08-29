// IRemoteService.aidl
package com.unistrong.guard;

// Declare any non-default types here with import statements
import com.unistrong.guard.IRemoteServiceCallBack;

interface IRemoteService {
       void handleData(in byte[]data);
       void registerCallback(IRemoteServiceCallBack cb);
       void unregisterCallback(IRemoteServiceCallBack cb);
}
