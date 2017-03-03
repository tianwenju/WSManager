package com.delta.wsmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import okhttp3.Response;
import okio.ByteString;

/**
 * @author :  V.Wenju.Tian
 * @description :
 * @date : 2017/3/3 14:38
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WsManager wsManager = new WsManager();
        OkHttpWebSocketStrategy build = new OkHttpWebSocketStrategy.Builder(this).wsUrl("").wsStatusListener(new WsStatusListener() {
            @Override
            public void onOpen(Response response) {
                super.onOpen(response);
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                super.onFailure(t, response);
            }
        }).build();
        wsManager.setBaseWebSocketStrategy(build);
        wsManager.startConnect();
        wsManager.sendMessage("sdf");

    }
}
