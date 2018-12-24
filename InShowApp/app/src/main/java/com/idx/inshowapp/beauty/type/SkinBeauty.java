package com.idx.inshowapp.beauty.type;

import android.opengl.GLES20;
import android.util.Log;

import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.utils.OpenGlUtils;


/**
 * Created by sunny on 2018/8/2.
 * 美白滤镜
 */

public class SkinBeauty extends GPUImageFilter {
    private static final String TAG=SkinBeauty.class.getSimpleName();
    private int gHaaCoef;
    private int gHmixCoef;
    private int gHiternum;
    private int gHWidth;
    private int gHHeight;

    private float aaCoef;
    private float mixCoef;
    private int iternum;

    public SkinBeauty(){
        super(OpenGlUtils.readShaderFromRawResource(R.raw.beauty_vert) ,
                OpenGlUtils.readShaderFromRawResource(R.raw.beauty_frag));
        setFlag(0);
    }

    //创建着色器程序textureProgram以及获取相应参数的句柄
    protected void onInit() {
        super.onInit();
        gHaaCoef=GLES20.glGetUniformLocation(mGLProgId,"aaCoef");
        gHmixCoef=GLES20.glGetUniformLocation(mGLProgId,"mixCoef");
        gHiternum=GLES20.glGetUniformLocation(mGLProgId,"iternum");
        gHWidth=GLES20.glGetUniformLocation(mGLProgId,"mWidth");
        gHHeight=GLES20.glGetUniformLocation(mGLProgId,"mHeight");
    }

    //设置磨皮等级
    public void setFlag(int flag) {
        switch (flag/20+1){
            case 1:
                setCoef(1,0.19f,0.54f);
                break;
            case 2:
                setCoef(2,0.29f,0.54f);
                break;
            case 3:
                setCoef(3,0.17f,0.39f);
                break;
            case 4:
                setCoef(3,0.25f,0.54f);
                break;
            case 5:
                setCoef(4,0.13f,0.54f);
                break;
            case 6:
                setCoef(4,0.19f,0.69f);
                break;
            default:
                setCoef(0,0f,0f);
                break;
        }
    }

    private void setCoef(int a, float b, float c){
        this.iternum=a;
        this.aaCoef=b;
        this.mixCoef=c;
    }


    //将要改变的参数传给着色器
    protected void onDrawArraysPre() {
        GLES20.glUniform1f(gHWidth,mOutputWidth);
        GLES20.glUniform1f(gHHeight,mOutputHeight);
        GLES20.glUniform1f(gHaaCoef,aaCoef);
        GLES20.glUniform1f(gHmixCoef,mixCoef);
        GLES20.glUniform1i(gHiternum,iternum);
    }
}
