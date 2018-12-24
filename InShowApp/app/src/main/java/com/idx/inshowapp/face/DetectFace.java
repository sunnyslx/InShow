package com.idx.inshowapp.face;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.idx.inshowapp.R;
import com.idx.inshowapp.camera.InShowCamera;
import com.idx.inshowapp.utils.PointsMatrix;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by stefan on 18-8-9.
 */

public class DetectFace {
    private static final String TAG = "MainActivity";

    public boolean isTakeLicenseSuccess() {
        return takeLicenseSuccess;
    }

    private boolean takeLicenseSuccess;

    private Facepp facepp = new Facepp();
    private PointsMatrix mPointsMatrix = new PointsMatrix();

    public void confirmLicense(final Context context){
        LicenseManager licenseManager = new LicenseManager(context);
        licenseManager.setAuthTimeBufferMillis(0);
        licenseManager.takeLicenseFromNetwork(
                FaceUtil.CN_LICENSE_URL,
                "123",
                FaceUtil.API_KEY,
                FaceUtil.API_SECRET,
                Facepp.getApiName(),
                "1",
                new LicenseManager.TakeLicenseCallback() {
                    //联网授权成功
                    @Override
                    public void onSuccess() {
                        initSdk(context);
                        Log.i(TAG, "onSuccess: ");
                    }
                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        takeLicenseSuccess = false;
                        Toast.makeText(context,"网络开小差,无法使用贴图功能",Toast.LENGTH_LONG).show();
                    }
                });
    }

    //检测人脸坐标点
    public void detectLandmark(byte[] bytes,int width,int height){
        Facepp.Face[] faces = facepp.detect(bytes, width, height, Facepp.IMAGEMODE_NV21);
        Log.d(TAG, "detectLandmark: faces.length "+faces.length);
        Log.d(TAG, "detectLandmark: width "+width+" height "+height);
        ArrayList<ArrayList> pointsOpengl = new ArrayList<>();
        if (faces.length>0){
            for (int c = 0; c < faces.length; c++) {
                //获取人脸坐标点
                facepp.getLandmarkRaw(faces[c], Facepp.FPP_GET_LANDMARK81);

                ArrayList<FloatBuffer> triangleVBList = new ArrayList<>();
                for (int i = 0; i < faces[c].points.length; i++) {
                    float x = (faces[c].points[i].x / width) * 2 - 1;
                    if (!InShowCamera.isFront()){
                        x = -x;
                    }
                    float y = (faces[c].points[i].y / height) * 2-1;
                    Log.d(TAG, "detectFacePoints: x "+x);
                    Log.d(TAG, "detectFacePoints: y "+y);
                    float[] pointf = new float[]{y, x, 0.0f};

                    // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
                    ByteBuffer qbb = ByteBuffer.allocateDirect(pointf.length * 4);
                    // 数组排列用nativeOrder
                    qbb.order(ByteOrder.nativeOrder());
                    FloatBuffer fb = qbb.asFloatBuffer();
                    fb.put(pointf);
                    fb.position(0);
                    triangleVBList.add(fb);
                }
                pointsOpengl.add(triangleVBList);

                if (mPointsMatrix != null){
                    synchronized (mPointsMatrix) {
                        mPointsMatrix.points = pointsOpengl;
                    }
                }


                //将屏幕坐标转换成opengl坐标
//                float centerX = (faces[c].points[34].x / width) * 2 - 1;
//                float centerY = (faces[c].points[34].y / height) * 2-1;
//                Log.d(TAG, "detectLandmarkc: x "+centerX);
//                Log.d(TAG, "detectLandmarkc: y "+centerY);

                InShowCamera.setPoseAngle(faces[c].pitch,faces[c].yaw,faces[c].roll);
            }
        }

    }

    private void initSdk(Context context){
        String errorCode = facepp.init(context, ConUtil.getFileContent(context,
                R.raw.megviifacepp_0_5_2_model),  0);
        if (errorCode != null){
            Toast.makeText(context,errorCode,Toast.LENGTH_SHORT).show();
        }
        else {
            takeLicenseSuccess = true;
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    public void releaseFacepp(){
        facepp.release();
    }

    public void resetTrack(){
        facepp.resetTrack();
    }

    public void setRotation(int rotation){
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        faceppConfig.rotation = rotation;
        facepp.setFaceppConfig(faceppConfig);
    }

    public void setmPointsMatrix(PointsMatrix pointsMatrix){
        mPointsMatrix = pointsMatrix;
    }
}
