package com.idx.inshowapp.sticker.base;

import android.opengl.GLES20;
import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.utils.MatrixHelper;
import com.idx.inshowapp.utils.OpenGlUtils;
import com.idx.inshowapp.utils.TextureRotationUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by stefan on 18-10-6.
 * 相机贴纸
 */

public class StickerFilter extends GPUImageFilter {
    private float[] matrix = new float[16];
    private float[] modelMatrix = new float[16];
    private int matrixLocation;
    public StickerFilter(){
        mVertexShader = OpenGlUtils.readShaderFromRawResource(R.raw.sticker_texture_vertex);
        mFragmentShader = NO_FILTER_FRAGMENT_SHADER;
        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
    }

    protected void onInit(){
        super.onInit();
        matrixLocation = GLES20.glGetUniformLocation(mGLProgId,"matrix");
    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, matrix, 0);
    }

    public void setMatrix(){
        MatrixHelper.perspectiveM(matrix,45,
                (float) mOutputWidth /(float) mOutputHeight,1f,10f);

        setIdentityM(modelMatrix,0);
        translateM(modelMatrix,0,0f,0f,-4f);

        final float[] temp = new float[16];
        multiplyMM(temp,0,matrix,0,modelMatrix,0);
        System.arraycopy(temp,0,matrix,0,temp.length);
    }
}
