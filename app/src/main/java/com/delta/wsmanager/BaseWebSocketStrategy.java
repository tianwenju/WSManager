package com.delta.wsmanager;

import okio.ByteString;

/**
 * @description :
 * @autHor :  V.Wenju.Tian
 * @date : 2017/3/2 16:47
 */


public interface BaseWebSocketStrategy {

    void startConnect();

    void stopConnect();

    boolean isWsConnected();

    int getCurrentStatus();

    boolean sendMessage(String msg);

    boolean sendMessage(ByteString byteString);

    void setWsStatusListener(WsStatusListener wsStatusListener);
}
