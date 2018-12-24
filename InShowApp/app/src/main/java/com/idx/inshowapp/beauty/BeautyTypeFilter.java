package com.idx.inshowapp.beauty;

import com.idx.inshowapp.R;
import com.idx.inshowapp.beauty.type.BigEyes;
import com.idx.inshowapp.beauty.type.Jaw;
import com.idx.inshowapp.beauty.type.Forehead;
import com.idx.inshowapp.beauty.type.SkinBeauty;
import com.idx.inshowapp.beauty.type.ThinFace;
import com.idx.inshowapp.beauty.type.ThinNose;
import com.idx.inshowapp.beauty.type.Whitening;
import com.idx.inshowapp.filter.base.GPUImageFilter;

/**
 * Created by stefan on 18-11-13.
 */

public class BeautyTypeFilter {
    public static SkinBeauty getSkinBeauty() {
        return skinBeauty;
    }

    public static Whitening getWhitening() {
        return whitening;
    }

    public static ThinFace getThinFace() {
        return thinFace;
    }

    public static Jaw getJaw() {
        return jaw;
    }

    public static Forehead getForehead() {
        return forehead;
    }

    public static BigEyes getBigeyes() {
        return bigeyes;
    }

    public static ThinNose getThinNose() {
        return thinNose;
    }

    private static SkinBeauty skinBeauty;
    private static Whitening whitening;
    private static ThinFace thinFace;
    private static Jaw jaw;
    private static Forehead forehead;
    private static BigEyes bigeyes;
    private static ThinNose thinNose;

    public BeautyTypeFilter(){
        skinBeauty = new SkinBeauty();
        whitening = new Whitening();
        thinNose = new ThinNose();
        thinFace = new ThinFace();
        jaw = new Jaw();
        forehead = new Forehead();
        bigeyes = new BigEyes();
    }

    public static void onInit(){
        skinBeauty.init();
        whitening.init();
        thinFace.init();
        jaw.init();
        forehead.init();
        bigeyes.init();
        thinFace.init();
    }

    public void onDisplaySizeChanged(int width,int height){
        skinBeauty.onDisplaySizeChanged(width,height);
        whitening.onDisplaySizeChanged(width,height);
        thinFace.onDisplaySizeChanged(width,height);
        thinNose.onDisplaySizeChanged(width,height);
        jaw.onDisplaySizeChanged(width,height);
        forehead.onDisplaySizeChanged(width,height);
        bigeyes.onDisplaySizeChanged(width,height);
    }

    public void initFrameBuffer(int width,int height){
        skinBeauty.initFrameBuffer(width,height);
        whitening.initFrameBuffer(width,height);
        thinFace.initFrameBuffer(width,height);
        jaw.initFrameBuffer(width,height);
        forehead.initFrameBuffer(width,height);
        bigeyes.initFrameBuffer(width,height);
        thinFace.initFrameBuffer(width,height);
    }

    public GPUImageFilter initBeauty(int resourceId){
        switch (resourceId){
            case R.drawable.artwork_icon:
                return null;
            case R.drawable.face_meifu_icon:
                return skinBeauty;
            case R.drawable.meibai_icon:
                return whitening;
            case R.drawable.face_lift_icon:
                return thinFace;
            case R.drawable.jaw_icon:
                return jaw;
            case R.drawable.forehead_icon:
                return forehead;
            case R.drawable.bigeye_icon:
                return bigeyes;
            case R.drawable.nose_icon:
                return thinNose;
            default:
                return null;
        }
    }
}
