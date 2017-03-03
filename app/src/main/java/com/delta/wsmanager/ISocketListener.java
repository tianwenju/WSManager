package com.delta.wsmanager;

import okio.ByteString;

/**
 * @description :
 * @autHor :  V.Wenju.Tian
 * @date : 2017/3/3 14:47
 */


interface ISocketListener {

    void startConnect();

    void stopConnect();

    boolean isWsConnected();

    int getCurrentStatus();

    boolean sendMessage(String msg);

    boolean sendMessage(ByteString byteString);


}
