package com.idx.inshowapp.utils;

import android.content.Context;


import com.idx.inshowapp.beauty.BeautyGroup;
import com.idx.inshowapp.beauty.BeautyTypeFilter;
import com.idx.inshowapp.camera.InShowCameraV2;
import com.idx.inshowapp.face.DetectFace;

import com.idx.inshowapp.renderer.CameraBaseRenderer;
import com.idx.inshowapp.renderer.CameraRenderView;
import com.megvii.facepp.sdk.Facepp;

/**
 * Created by sunny on 18-8-16.
 */

public class InShowParams {

    public static Context context;
    public static CameraBaseRenderer cameraBaseRenderer;
    public static CameraRenderView cameraRenderView;
    public static InShowCameraV2 cameraV2;
    public static int beautyLevel=0 ;
    public static BeautyGroup beautyGroup;
    public static BeautyTypeFilter beautyTypeFilter;
    public static DetectFace detectFace;
}
