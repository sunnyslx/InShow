package com.idx.inshowapp.beauty;

import android.util.Log;

import com.idx.inshowapp.filter.base.GPUImageFilter;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by stefan on 18-11-16.
 * 存储所添加的美颜类型
 */

public class BeautyGroup{
    public BeautyGroup() {
        beautyFilters = new ArrayList<>();
        beautyFilters.add(BeautyTypeFilter.getSkinBeauty());
    }

    public List<GPUImageFilter> getBeautyFilters() {
        return beautyFilters;
    }

    private List<GPUImageFilter> beautyFilters;


    public void onDrawFrame(int textureId, FloatBuffer cubeBuffer,FloatBuffer textureBuffer){
        for (int i = 0;i<beautyFilters.size();i++){
            if (i==beautyFilters.size()-1){
                Log.d(TAG, "onDrawFrame: 11");
                beautyFilters.get(i).onDrawFrame(textureId,cubeBuffer,textureBuffer);
            }
            else {
                textureId = beautyFilters.get(i).drawToTexture(textureId);
            }
        }
    }

    public int drawToTexture(int textureId){
        for (int i = 0;i<beautyFilters.size();i++){
            textureId = beautyFilters.get(i).drawToTexture(textureId);
        }
        return textureId;
    }

    public void clearBeautyGroup(){
        beautyFilters.clear();
    }
}
