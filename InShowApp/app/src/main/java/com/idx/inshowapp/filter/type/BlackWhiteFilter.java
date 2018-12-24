package com.idx.inshowapp.filter.type;

import android.opengl.GLES20;
import android.util.Log;

import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.utils.InShowParams;
import com.idx.inshowapp.utils.OpenGlUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by Sunny on 18-9-1.
 */

public class BlackWhiteFilter extends GPUImageFilter {
    private int[] inputTextureHandles = {-1};
    private int[] inputTextureUniformLocations = {-1};
    private int mGLStrengthLocation;

    public BlackWhiteFilter(){
        super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(R.raw.blackwhite));
    }

    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, inputTextureHandles, 0);
        for(int i = 0; i < inputTextureHandles.length; i++)
            inputTextureHandles[i] = -1;
    }

    protected void onDrawArraysAfter(){
        for(int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }

    protected void onDrawArraysPre(){
        for(int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3) );
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
            GLES20.glUniform1i(inputTextureUniformLocations[i], (i+3));
        }
    }

    protected void onInit(){
        super.onInit();
        for(int i=0; i < inputTextureUniformLocations.length; i++)
            inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture"+(2+i));
        mGLStrengthLocation = GLES20.glGetUniformLocation(mGLProgId,
                "strength");
    }

    protected void onInitialized(){
        super.onInitialized();
        setFloat(mGLStrengthLocation, 1.0f);
        runOnDraw(new Runnable(){
            public void run(){
                inputTextureHandles[0] = OpenGlUtils.loadTexture(InShowParams.context, "filter/blackwhite.png");
            }
        });
    }
}
