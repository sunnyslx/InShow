package com.idx.inshowapp.utils;

import android.content.Context;

import com.idx.inshowapp.beauty.BeautyGroup;
import com.idx.inshowapp.beauty.BeautyTypeFilter;
import com.idx.inshowapp.camera.InShowCameraV2;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.renderer.CameraBaseRenderer;
import com.idx.inshowapp.renderer.CameraRenderView;

/**
 * Created by sunny on 18-8-16.
 */

public class InShowEngine {

    private static InShowEngine magicEngine;

    public static InShowEngine getInstance(){
        if(magicEngine == null)
            throw new NullPointerException("");
        else
            return magicEngine;
    }

    private InShowEngine(Context context, Builder builder){

    }
    public void setBeautyLevel(int level){
        if(InShowParams.cameraBaseRenderer instanceof CameraRenderView && InShowParams.beautyLevel != level) {
            InShowParams.beautyLevel = level;
            ((CameraRenderView) InShowParams.cameraBaseRenderer).onBeautyLevelChanged();
        }
    }

    public static class Builder{
        public InShowEngine build(CameraBaseRenderer cameraBaseRenderer) {
            InShowParams.context = cameraBaseRenderer.getContext();
            InShowParams.cameraBaseRenderer = cameraBaseRenderer;
            InShowParams.cameraRenderView=(CameraRenderView)cameraBaseRenderer;
            InShowParams.cameraV2=new InShowCameraV2(cameraBaseRenderer.getContext());
            InShowParams.beautyTypeFilter = new BeautyTypeFilter();
            InShowParams.beautyGroup = new BeautyGroup();
            return new InShowEngine(cameraBaseRenderer.getContext(),this);
        }
    }
}
