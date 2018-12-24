package com.idx.inshowapp.camera;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.idx.inshowapp.data.CameraInfo;
import com.idx.inshowapp.utils.InShowParams;
import com.idx.inshowapp.utils.SensorUtil;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by stefan on 18-8-29.
 */

public class InShowCamera {
    private static final String TAG = InShowCamera.class.getSimpleName();
    private static Camera camera;
    private static int cameraID = 0;

    private static SurfaceTexture surfaceTexture;
    private static Camera.Size size;
    private static int width;
    private static int height;
    private static int angle = 270;
    private static SensorUtil sensorUtil;
    private static Context mContext;
    private static float pitch, yaw, roll;

    public static void setContext(Context context){
        mContext = context;
    }

    public static Camera getCamera() {
        return camera;
    }

    public static boolean openCamera() {
        if (sensorUtil == null){
            Log.d(TAG, "openCamera: context "+mContext);
            sensorUtil = new SensorUtil(mContext);
        }
        Log.d(TAG, "openCamera: 11");
        if (camera == null) {
            Log.d(TAG, "openCamera: info");
            try {
                SystemClock.sleep(100);
                camera = Camera.open(cameraID);
                size = getPreviewSize();
                width = size.width;
                height = size.height;
                //设置预览帧回调，重复利用一个预览内存，防止内存抖动
                camera.setPreviewCallbackWithBuffer(previewCallback);
                Log.d(TAG, "openCamera: width "+size.width+" height "+size.height);
                camera.addCallbackBuffer(new byte[((size.width * size.height) *
                        ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
//                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean openCamera(int id) {
        if (sensorUtil == null){
            sensorUtil = new SensorUtil(mContext);
        }
        Log.d(TAG, "openCamera: 12");
        if (camera == null) {

            try {
                camera = Camera.open(id);
                cameraID = id;
                size = getPreviewSize();
                //设置预览帧回调，重复利用一个预览内存，防止内存抖动
                camera.setPreviewCallbackWithBuffer(previewCallback);
                Log.d(TAG, "openCamera: width "+size.width+" height "+size.height);
                camera.addCallbackBuffer(new byte[((size.width * size.height) *
                        ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
//                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }


    //0代表后置，1代表前置
    public static  boolean isFront(){
        if (cameraID==1){
            return true;
        }
        return false;
    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.setAutoFocusMoveCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    public static Camera.Parameters getParameters() {
        if (camera != null) {
            return camera.getParameters();
        } else {
            return null;
        }
    }


    public static void switchCamera() {
        releaseCamera();
        if (cameraID == 0){
            cameraID = 1;
            angle = 270;
        }
        else {
            cameraID = 0;
            angle = 90;
        }
        openCamera(cameraID);
        startPreview(surfaceTexture);
        InShowParams.detectFace.resetTrack();
    }

    private static Camera.Size getPreviewSize() {
        Log.i(TAG, "getPreviewSize: camera="+camera);
        if (camera != null) {
            return camera.getParameters().getPreviewSize();
        } else {
            return null;
        }
    }

    private static Camera.Size getPictureSize() {
        return camera.getParameters().getPictureSize();
    }

    public static void startPreview(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "startPreview: camera " + camera);
        if (camera != null)
            try {
                camera.setPreviewTexture(surfaceTexture);
                InShowCamera.surfaceTexture = surfaceTexture;
                camera.startPreview();
                Camera.Parameters parameters=camera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void startPreview() {
        if (camera != null)
            camera.startPreview();
    }

    public static void setFocus(){
        if (camera !=null){
            camera.autoFocus(focusCallback);
        }
    }

    private static Camera.AutoFocusCallback focusCallback=new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.i(TAG, "onAutoFocus: ");
            if (success){
                Log.i(TAG, "onAutoFocus: 聚焦成功");
                camera.startPreview();
            }else {
                Log.i(TAG, "onAutoFocus: 聚焦不成功");
            }
        }
    };

    public static CameraInfo getCameraInfo() {
        CameraInfo info = new CameraInfo();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, cameraInfo);
        if (size !=null){
            info.previewWidth = size.width;
            info.previewHeight = size.height;
            info.orientation = cameraInfo.orientation;
            size = getPictureSize();
            info.pictureWidth = size.width;
            info.pictureHeight = size.height;
        }
        return info;
    }

    private static Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            InShowCamera.camera.addCallbackBuffer(data);

            int rotation = angle;
            final int orientation = sensorUtil.orientation;
            if (orientation == 0)
                rotation = angle;
            else if (orientation == 1)
                rotation = 0;
            else if (orientation == 2)
                rotation = 180;
            else if (orientation == 3)
                rotation = 360 - angle;

            InShowParams.detectFace.setRotation(rotation);
            InShowParams.detectFace.detectLandmark(data,width,height);
        }
    };

    public static void setPoseAngle(float p,float y,float r){
        pitch = p;
        yaw = y;
        roll = r;
        Log.d(TAG, "setPoseAngle: pitch "+pitch);
        Log.d(TAG, "setPoseAngle: yaw "+yaw);
        Log.d(TAG, "setPoseAngle: roll "+roll);
        Log.d(TAG, "setPoseAngle: ");
    }
}
