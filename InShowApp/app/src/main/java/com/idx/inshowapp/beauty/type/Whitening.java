package com.idx.inshowapp.beauty.type;

import android.opengl.GLES20;
import android.util.Log;

import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.utils.InShowParams;
import com.idx.inshowapp.utils.OpenGlUtils;

import javax.microedition.khronos.opengles.GL;

import static android.content.ContentValues.TAG;

/**
 * Created by stefan on 18-11-14.
 */

public class Whitening extends GPUImageFilter {
    private int maskTexture;
    private int mHMaskImage;
    private int mHIntensity;

    private float intensity;

    public Whitening(){
        super(NO_FILTER_VERTEX_SHADER,OpenGlUtils.readShaderFromRawResource(R.raw.lookup));
    }

    public void setFlag(int flag){
        intensity = flag/100f;
    }

    public void onInit(){
        super.onInit();
        mHMaskImage= GLES20.glGetUniformLocation(mGLProgId,"maskTexture");
        mHIntensity=GLES20.glGetUniformLocation(mGLProgId,"intensity");
        maskTexture = OpenGlUtils.loadTexture(InShowParams.context,"beauty/purity.png");
    }

    protected void onDrawArraysPre(){
        GLES20.glUniform1f(mHIntensity,intensity);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,maskTexture);
        GLES20.glUniform1i(mHMaskImage,1);
    }

    protected void onDrawArraysAfter(){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }
}
