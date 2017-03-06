package com.delta.wsmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import okhttp3.Response;
import okio.ByteString;

/**
 * @author :  V.Wenju.Tian
 * @description :
 * @date : 2017/3/3 14:38
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private WsManager wsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wsManager = new WsManager();
        OkHttpWebSocketStrategy build = new OkHttpWebSocketStrategy.Builder(this).wsUrl("ws://172.22.35.176:9090/websocket").wsStatusListener(new WsStatusListener() {
            @Override
            public void onOpen(Response response) {
                Log.e(TAG, "onOpen: "+response.toString());
                super.onOpen(response);
            }

            @Override
            public void onMessage(String text) {
                Log.e(TAG, "onMessage: "+text);
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
        findViewById(R.id.bt_contect).setOnClickListener(this);
        findViewById(R.id.bt_send).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_contect:
                wsManager.startConnect();
                break;
            case R.id.bt_send:
                wsManager.sendMessage("sdfsdf");
                break;
            case R.id.bt_stop:
                wsManager.stopConnect();
                break;
            default:
                break;
        }
    }
}
