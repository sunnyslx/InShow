package com.idx.inshowapp.renderer;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import com.idx.inshowapp.beauty.BeautyTypeFilter;
import com.idx.inshowapp.camera.InShowCamera;
import com.idx.inshowapp.data.CameraInfo;
import com.idx.inshowapp.filter.base.CameraInputFilter;
import com.idx.inshowapp.sticker.base.StickerFilter;
import com.idx.inshowapp.utils.InShowParams;
import com.idx.inshowapp.utils.OpenGlUtils;
import com.idx.inshowapp.utils.PointsMatrix;

import java.nio.ByteBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by sunny on 18-8-16.
 */

public class CameraRenderView extends CameraBaseRenderer {
    private static final String TAG = CameraRenderView.class.getSimpleName();
    private CameraInputFilter cameraInputFilter;

    public SurfaceTexture surfaceTexture;
    private CameraInfo info;
    //此类用于绘制贴纸
    private StickerFilter stickerFilter;
    //创建离屏buffer，用于最后导出数据

    private int[] mExportFrame = new int[1];
    private int[] mExportTexture = new int[1];
    private Point mDataSize;                                    //数据的大小


    private boolean isShoot=false;                              //一次拍摄flag
    private ByteBuffer[] outPutBuffer = new ByteBuffer[3];      //用于存储回调数据的buffer
    private FrameDataCallback mFrameDataCallback;
    private int indexOutput=0;                                  //回调数据使用的buffer索引
    private PointsMatrix pointsMatrix;


    public CameraRenderView(Context context) {
        super(context);
    }

    public CameraRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        if (cameraInputFilter == null) {
            cameraInputFilter = new CameraInputFilter();
            cameraInputFilter.init();
        }
        if (textureId == OpenGlUtils.NO_TEXTURE) {
            textureId = OpenGlUtils.getExternalOESTextureID();
            if (textureId != OpenGlUtils.NO_TEXTURE) {
                surfaceTexture = new SurfaceTexture(textureId);
                surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
            }
        }

        mDataSize = new Point(surfaceWidth, surfaceHeight);
        mDataSize.x = surfaceWidth;
        mDataSize.y = surfaceHeight;
        GLES20.glGenFramebuffers(1, mExportFrame, 0);
        EasyGlUtils.genTexturesWithParameter(1, mExportTexture, 0, GLES20.GL_RGBA, mDataSize.x,
                mDataSize.y);
//        Log.d(TAG, "onSurfaceCreated: stickerFilter "+stickerFilter);
//        if (stickerFilter == null){
//            stickerFilter = new StickerFilter();
//            stickerFilter.onInit(context);
//        }


        if (stickerFilter == null){
            stickerFilter = new StickerFilter();
            stickerFilter.init();
        }

        BeautyTypeFilter.onInit();

        if (pointsMatrix == null){
            pointsMatrix = new PointsMatrix();
            InShowParams.detectFace.setmPointsMatrix(pointsMatrix);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        openCamera();
        InShowCamera.setFocus();
        onFilterChanged();
        stickerFilter.setMatrix();
        pointsMatrix.setMatrix();

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if (surfaceTexture == null)
            return;
        surfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        surfaceTexture.getTransformMatrix(mtx);
        cameraInputFilter.setTextureTransformMatrix(mtx);
        int id;
        //没有滤镜，直接将相机数据渲染在屏幕上
        if (filter == null) {
            if (stickerTexture == OpenGlUtils.NO_TEXTURE){
                id = cameraInputFilter.onDrawToTexture(textureId);
                InShowParams.beautyGroup.onDrawFrame(id,gLCubeBuffer,gLTextureBuffer);
            }
            //有贴纸
            else {
                id = cameraInputFilter.onDrawToTexture(textureId);
                InShowParams.beautyGroup.onDrawFrame(id,gLCubeBuffer,gLTextureBuffer);
                stickerFilter.onDrawFrame(stickerTexture);
            }
        }
        //有滤镜，先将相机数据渲染在Framebuffer上，然后与滤镜纹理混合，渲染在屏幕上
        else {
            if (stickerTexture == OpenGlUtils.NO_TEXTURE){
                id = cameraInputFilter.onDrawToTexture(textureId);
                id = InShowParams.beautyGroup.drawToTexture(id);
                filter.onDrawFrame(id, gLCubeBuffer, gLTextureBuffer);
            }
            else {
                id = cameraInputFilter.onDrawToTexture(textureId);
                id = InShowParams.beautyGroup.drawToTexture(id);
                filter.onDrawFrame(id,gLCubeBuffer,gLTextureBuffer);
                stickerFilter.onDrawFrame(stickerTexture);
            }

        }
        pointsMatrix.draw();
        callbackIfNeeded();
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }
    };


    public void openCamera() {
        Log.i(TAG, "openCamera: 打开相机");
        if (InShowCamera.getCamera() == null)
            InShowCamera.openCamera();
        info = InShowCamera.getCameraInfo();
        if (info.orientation == 90 || info.orientation == 270) {
            imageWidth = info.previewHeight;
            imageHeight = info.previewWidth;
        } else {
            imageWidth = info.previewWidth;
            imageHeight = info.previewHeight;
        }
        cameraInputFilter.onInputSizeChanged(imageWidth, imageHeight);
        if (!InShowCamera.isFront()){
            adjustSize(info.orientation, false, true);
        }else {
            adjustSize(info.orientation, true, false);
        }
        if (surfaceTexture != null) {
            InShowCamera.startPreview(surfaceTexture);
        }
    }

    protected void onFilterChanged() {
        super.onFilterChanged();
        cameraInputFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
        stickerFilter.onDisplaySizeChanged(surfaceWidth,surfaceHeight);
        InShowParams.beautyTypeFilter.onDisplaySizeChanged(surfaceWidth,surfaceHeight);
        InShowParams.beautyTypeFilter.initFrameBuffer(surfaceWidth,surfaceHeight);
        cameraInputFilter.initCameraFrameBuffer(imageWidth, imageHeight);
    }

    public void onBeautyLevelChanged() {
        if (filter==null){
            Log.i(TAG, "onBeautyLevelChanged: 进入相机level");
            cameraInputFilter.onBeautyLevelChanged();
        }else {
            Log.i(TAG, "onBeautyLevelChanged: 进入滤镜level");
            filter.onFilterLevelChange();
        }
    }

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private void callbackIfNeeded() {
        if (mFrameDataCallback != null && (isShoot)) {
            indexOutput = indexOutput++ >= 2 ? 0 : indexOutput;
            if (outPutBuffer[indexOutput] == null) {
                outPutBuffer[indexOutput] = ByteBuffer.allocate(surfaceWidth *
                        surfaceHeight * 4);
            }
            frameCallback();
            isShoot = false;
        }
    }

    //读取数据并回调
    private void frameCallback() {
        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer[indexOutput]);
        mFrameDataCallback.onFrame(surfaceWidth,surfaceHeight,outPutBuffer[indexOutput].array());
    }



    public void setFrameCallback(FrameDataCallback frameCallback){
        if(outPutBuffer!=null){
            outPutBuffer=new ByteBuffer[3];

        }
        this.mFrameDataCallback = frameCallback;
    }




    public void takePhoto(){
        if (imageWidth > 0 && imageHeight > 0) {
            isShoot=true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "on:surfaceDestroyed: ");
        super.surfaceDestroyed(holder);

        InShowCamera.releaseCamera();
    }





}
