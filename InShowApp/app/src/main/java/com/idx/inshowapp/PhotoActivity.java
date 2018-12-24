package com.idx.inshowapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.idx.inshowapp.baidu.control.TTSManager;
import com.idx.inshowapp.baidu.control.UnitManager;
import com.idx.inshowapp.baidu.unit.listener.IPhotoListener;
import com.idx.inshowapp.utils.ImageUtils;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.onekeyshare.OnekeyShare;


public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PhotoActivity.class.getSimpleName();
    private ImageView photo;
    private LinearLayout mPhotoSave;
    private LinearLayout mPhotoCancle;
    private Bitmap bitmap;
    private LinearLayout mPhotoShare;
    private String uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initView();
        Intent intent = getIntent();
        if (intent != null) {
            uri = intent.getData().toString();
            if (uri != null) {
                Bitmap bitmap1 = BitmapFactory.decodeFile(uri);
                bitmap = ImageUtils.rotationBitmap(bitmap1);
                photo.setImageBitmap(bitmap);
            }
        }
        UnitManager.getInstance(this).setPhotoListener(photoListener);
    }

    //初始化界面
    private void initView() {
        photo = findViewById(R.id.igv_photo);
        mPhotoSave = findViewById(R.id.photo_save);
        mPhotoCancle = findViewById(R.id.photo_cancle);
        mPhotoShare = findViewById(R.id.photo_share);
        mPhotoSave.setOnClickListener(this);
        mPhotoCancle.setOnClickListener(this);
        mPhotoShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_save:
                savePhoto();
                break;
            case R.id.photo_cancle:
                cancelSavePhoto();
                break;
            case R.id.photo_share:
                Log.i(TAG, "onClick: 分享功能");
                showShare();
                break;
        }
    }

    private void showShare() {
        OnekeyShare onekeyShare = new OnekeyShare();
        //关闭sso授权
        onekeyShare.disableSSOWhenAuthorize();
        onekeyShare.setTitle("标题");
        // titleUrl是标题的网络链接，QQ和QQ空间等使用
        onekeyShare.setTitleUrl("http://sharesdk.cn");
        onekeyShare.setText(" ");
        onekeyShare.setImageData(bitmap);
        // url仅在微信（包括好友和朋友圈）中使用
        onekeyShare.setUrl("http://sharesdk.cn");
        onekeyShare.show(this);
    }

    private void savePhoto() {
        ImageUtils.saveImage(PhotoActivity.this, bitmap);
        finish();
    }

    private void cancelSavePhoto() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
            finish();
        }
    }

    private IPhotoListener photoListener = new IPhotoListener() {
        @Override
        public void photoSave() {
            Log.i(TAG, "photoSave: 语音控制照片存储");
            savePhoto();
        }

        @Override
        public void photoCancel() {
            cancelSavePhoto();
        }

        @Override
        public void photoShare() {

        }
    };
}
