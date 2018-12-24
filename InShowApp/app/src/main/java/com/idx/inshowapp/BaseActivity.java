package com.idx.inshowapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.idx.inshowapp.service.InShowService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunny on 18-7-17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public Intent intent;
    public   boolean requestPermission=true;

    public static final int REQUEST_CAMERA_PERMISSION = 1;
    public String[] mPermission=new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //隐藏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (hasCameraSupport()) {
            getNumberOfCameras();
        }

    }

    //判断是否存在摄像头
    private boolean hasCameraSupport() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    //获取摄像头个数
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    /**
     * 动态请求权限
     */
    public void  initPermission(){
        //用于存储用户拒绝授权的权限
        List<String> stringList=new ArrayList<>();
        for (int i=0;i<mPermission.length;i++){
            if (ContextCompat.checkSelfPermission(this,mPermission[i])!=
                    PackageManager.PERMISSION_GRANTED){
                stringList.add(mPermission[i]);
            }
        }
        //权限全部被允许
        if (stringList.isEmpty()){
            requestPermission=true;
        }else {
            //存在为允许的权限
            String[] permissionsArr = stringList.toArray(new String[stringList.size()]);
            ActivityCompat.requestPermissions(this, permissionsArr, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                Log.i("", "onRequestPermissionsResult: 请求权限");
                for (int i=0;i<grantResults.length;i++){
                    if (grantResults[i] !=PackageManager.PERMISSION_GRANTED){
                        //勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                        if (showRequestPermission) {
                            initPermission();
                            return;
                        } else { // false 被禁止了，不在访问
                            requestPermission = false;//已经禁止了
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
