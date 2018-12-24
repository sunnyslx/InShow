package com.idx.inshowapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.idx.inshowapp.face.DetectFace;
import com.idx.inshowapp.utils.InShowParams;
import com.megvii.facepp.sdk.Facepp;

public class WelcomeActivity extends BaseActivity {
    private static final String  TAG=WelcomeActivity.class.getSimpleName();
    private boolean flag=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        InShowParams.detectFace = new DetectFace();
        InShowParams.detectFace.confirmLicense(this);
        initPermission();
        if (flag && requestPermission){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        Log.i(TAG, "run: 跳转到camera界面");
                        Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        flag=false;
                }
            },2000);
        }
    }
}
