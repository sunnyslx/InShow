package com.idx.inshowapp.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import com.idx.inshowapp.data.CameraInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sunny on 18-8-7.
 */

public class InShowCameraV2 {

    private static final String TAG = InShowCameraV2.class.getSimpleName();
    private Activity mActivity;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mCaptureSize;
//    private Handler mCameraHandler;
    private SurfaceTexture mSurfaceTexture;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageReader mImageReader;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private OnBitmapListener onBitmapListener;

    private  Handler mBackgroundHandler = null;
    static {
        ORIENTATION.append(Surface.ROTATION_0, 0);
        ORIENTATION.append(Surface.ROTATION_90, 90);
        ORIENTATION.append(Surface.ROTATION_180, 180);
        ORIENTATION.append(Surface.ROTATION_270, 270);
    }

    public InShowCameraV2(Context activity) {
        mActivity = (Activity) activity;
//        startCameraThread();
    }

    public void setOnBitmapListener(OnBitmapListener getBitmapListener) {
        this.onBitmapListener = getBitmapListener;
    }


    public  void setBackgroundHandler(Handler mBackgroundHandler) {
        this.mBackgroundHandler = mBackgroundHandler;
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

//    private void startCameraThread() {
//        HandlerThread mCameraThread = new HandlerThread("CameraThread");
//        mCameraThread.start();
//        mCameraHandler = new Handler(mCameraThread.getLooper());
//    }

//    private final OnGetImageListener mOnGetPreviewListener = new OnGetImageListener();

    public void setupCamera(int width, int height) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) (mActivity).getSystemService(Context.CAMERA_SERVICE);
        try {
            //遍历所有摄像头
            assert manager != null;
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == Integer.parseInt(cameraId)) {
                    continue;
                }
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                //获取相机支持的最大拍照尺寸
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });
                //此ImageReader用于拍照所需
                setupImageReader();
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) (mActivity).getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            assert manager != null;
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    public void startPreview() {
        //设置SurfaceTexture的默认尺寸
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //根据mSurfaceTexture创建Surface
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            if (mCameraDevice != null) {
                //创建preview捕获请求
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //将此请求输出目标设为我们创建的Surface对象，这个Surface对象也必须添加给createCaptureSession才行
                mCaptureRequestBuilder.addTarget(previewSurface);
                //创建捕获会话，第一个参数是捕获数据的输出Surface列表，
                //第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，
                //第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    //创建捕获请求
                                    mCaptureRequest = mCaptureRequestBuilder.build();
                                    mCameraCaptureSession = session;
                                    //设置重复捕获数据的请求，之后surface绑定的SurfaceTexture中就会一直有数据到达
                                    //然后就会回调SurfaceTexture.OnFrameAvailableListener接口
                                    mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {

                            }
                        }, mBackgroundHandler);
            }

        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            capture();
        }
    };

    private void capture() {
        try {
            if (mActivity == null || mCameraDevice == null) {
                return;
            }
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            int rotation = (mActivity).getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getPreviewRotateDegree());
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    unLockFocus();
                }
            };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler.post(new imageSaver(reader.acquireNextImage()));
            }
        }, null);
    }


    public class imageSaver implements Runnable {

        private Image mImage;

        public imageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                onBitmapListener.getBitmap(bitmap);
            }
        }
    }

    public interface OnBitmapListener {

        void getBitmap(Bitmap bitmap);
    }


    public void cameraOnPause() {

        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    public void switchCameraId() {
        Log.i(TAG, "switchCameraId: 进入切换镜头");
        switch (mCameraId) {
            case "1":
                Log.i(TAG, "switchCameraId: 切换成后置");
                mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT;
                openCameraAgain();
                break;
            case "0":
                Log.i(TAG, "switchCameraId: 切换成前置");
                mCameraId = "" + CameraCharacteristics.LENS_FACING_BACK;
                openCameraAgain();
                break;
            default:
                break;
        }
    }

    /**
     * 切换摄像头后重新打开相机
     */
    private void openCameraAgain() {
        cameraOnPause();
        openCamera();
        setupImageReader();
        startPreview();
        getPreviewRotateDegree();
    }

    private int getPreviewRotateDegree() {
        int phoneDegree = 0;
        int result;
        //获得手机方向
        int phoneRotate = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        //得到手机的角度
        switch (phoneRotate) {
            case Surface.ROTATION_0:
                phoneDegree = 0;
                break;        //0
            case Surface.ROTATION_90:
                phoneDegree = 90;
                break;        //90
            case Surface.ROTATION_180:
                phoneDegree = 180;
                break;    //180
            case Surface.ROTATION_270:
                phoneDegree = 270;
                break;    //270
        }
        //分别计算前后置摄像头需要旋转的角度
        CameraInfo cameraInfo = new CameraInfo();
        if (mCameraId.equals("1")) {
            //前置摄像头
            result = (cameraInfo.orientation + phoneDegree) % 360;
            result = (360 - result) % 360;
            Log.i(TAG, "getPreviewRotateDegree: 前置摄像头 result=" + result);
        } else {
            //后置摄像头
            result = (cameraInfo.orientation - phoneDegree + 360) % 360;
            Log.i(TAG, "getPreviewRotateDegree: 后置摄像头 result=" + result);
        }
        return result;
    }
}
