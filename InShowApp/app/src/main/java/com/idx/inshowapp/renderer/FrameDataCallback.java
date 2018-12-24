package com.idx.inshowapp.renderer;

/**
 * Created by Sunny on 18-10-25.
 */

public interface FrameDataCallback {

    void onFrame(int width,int height,byte[] bytes);
//    void onFrame(byte[] bytes);
}
