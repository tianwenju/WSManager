package com.delta.wsmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.ContentValues.TAG;

/**
 * @description :
 * @autHor :  V.Wenju.Tian
 * @date : 2017/3/3 13:53
 */
public class OkHttpWebSocketStrategy implements BaseWebSocketStrategy {

    //重连相关
    private final static int RECONNECT_INTERVAL = 10 * 1000;
    private final static long RECONNECT_MAX_TIME = 120 * 1000;
    private Context mContext;
    private String wsUrl;
    private WebSocket mWebSocket;
    private OkHttpClient mOkHttpClient;
    private Request mRequest;
    private int mCurrentStatus = WsStatus.DISCONNECTED;
    private boolean isNeedReconnect = true;
    private WsStatusListener wsStatusListener;
    private Lock mLock;
    private Handler wsHandler = new Handler(Looper.getMainLooper());
    private int reconnectCount = 0;   //重连次数
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (wsStatusListener != null) wsStatusListener.onReconnect();
            buildConnect();
        }
    };
    private WebSocketListener mWebSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.e(TAG, "onOpen: "+"sdf");
            mWebSocket = webSocket;
            mCurrentStatus = WsStatus.CONNECTED;
            connected();
            if (wsStatusListener != null) wsStatusListener.onOpen(response);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (wsStatusListener != null) wsStatusListener.onMessage(bytes);
        }

        @Override
        public void onMessage(final WebSocket webSocket, String text) {
            Log.e(TAG, "onMessage: "+text);
            if (wsStatusListener != null) wsStatusListener.onMessage(text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (wsStatusListener != null) wsStatusListener.onClosing(code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (wsStatusListener != null) wsStatusListener.onClosed(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            tryReconnect();
            if (wsStatusListener != null) wsStatusListener.onFailure(t, response);
        }
    };

    public OkHttpWebSocketStrategy(Builder builder) {
        mContext = builder.mContext;
        wsUrl = builder.wsUrl;
        mOkHttpClient = builder.mOkHttpClient;
        this.wsStatusListener=builder.wsStatusListener;
        this.mLock = new ReentrantLock();
    }

    private void initWebSocket() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }
        if (mRequest == null) {
            mRequest = new Request.Builder()
                    .url(wsUrl)
                    .build();
        }
        mOkHttpClient.dispatcher().cancelAll();
        try {
            mLock.lockInterruptibly();
            try {
                mOkHttpClient.newWebSocket(mRequest, mWebSocketListener);
            } finally {
                mLock.unlock();
            }
        } catch (InterruptedException e) {
        }
    }

    public void setWsStatusListener(WsStatusListener wsStatusListener) {
        this.wsStatusListener = wsStatusListener;
    }

    @Override
    public boolean isWsConnected() {
        return mCurrentStatus == WsStatus.CONNECTED;
    }

    @Override
    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    @Override
    public void startConnect() {
        isNeedReconnect = true;
        buildConnect();
    }

    @Override
    public void stopConnect() {
        isNeedReconnect = false;
        disconnect();
    }

    private void tryReconnect() {
        if (!isNeedReconnect) return;
        mCurrentStatus = WsStatus.RECONNECT;

        if (!isNetworkConnected(mContext)) return;

        long delay = reconnectCount * RECONNECT_INTERVAL;
        wsHandler.postDelayed(reconnectRunnable, delay > RECONNECT_MAX_TIME ? RECONNECT_MAX_TIME : delay);
        reconnectCount++;
    }

    private void cancelReconnect() {
        wsHandler.removeCallbacks(reconnectRunnable);
        reconnectCount = 0;
    }

    private void connected() {
        cancelReconnect();
    }

    private void disconnect() {
        if (mCurrentStatus == WsStatus.DISCONNECTED) return;
        cancelReconnect();
        if (mOkHttpClient != null) mOkHttpClient.dispatcher().cancelAll();
        if (mWebSocket != null) {
            boolean isClosed = mWebSocket.close(WsStatus.CODE.NORMAL_CLOSE, WsStatus.TIP.NORMAL_CLOSE);
            //非正常关闭连接
            if (!isClosed) {
                if (wsStatusListener != null)
                    wsStatusListener.onClosed(WsStatus.CODE.ABNORMAL_CLOSE, WsStatus.TIP.ABNORMAL_CLOSE);
            }
        }
        mCurrentStatus = WsStatus.DISCONNECTED;
    }

    private void buildConnect() {
        if (mCurrentStatus == WsStatus.CONNECTED | mCurrentStatus == WsStatus.CONNECTING | !isNetworkConnected(mContext))
            return;
        mCurrentStatus = WsStatus.CONNECTING;
        initWebSocket();
    }

    //发送消息
    @Override
    public boolean sendMessage(String msg) {
        return send(msg);
    }

    @Override
    public boolean sendMessage(ByteString byteString) {
        return send(byteString);
    }

    private boolean send(Object msg) {
        boolean isSend = false;
        if (mWebSocket != null && mCurrentStatus == WsStatus.CONNECTED) {
            if (msg instanceof String) {
                isSend = mWebSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = mWebSocket.send((ByteString) msg);
            }
            //发送消息失败，尝试重连
            if (!isSend) {
                tryReconnect();
            }
        }
        return isSend;
    }

    //检查网络是否连接
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static final class Builder {
        private Context mContext;
        private String wsUrl;
        private OkHttpClient mOkHttpClient;
        private WsStatusListener wsStatusListener;

        public Builder(Context val) {
            mContext = val;
        }

        public Builder wsUrl(String val) {
            wsUrl = val;
            return this;
        }

        public Builder client(OkHttpClient val) {
            mOkHttpClient = val;
            return this;
        }

        public Builder wsStatusListener(WsStatusListener wsStatusListener) {
            this.wsStatusListener = wsStatusListener;
            return this;
        }

        public OkHttpWebSocketStrategy build() {
            return new OkHttpWebSocketStrategy(this);
        }
    }
}
