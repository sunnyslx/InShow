package com.idx.inshowapp.filter;

import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.filter.type.BlackWhiteFilter;
import com.idx.inshowapp.filter.type.LatteFilter;
import com.idx.inshowapp.filter.type.RetroFilter;
import com.idx.inshowapp.filter.type.RomanceFilter;
import com.idx.inshowapp.filter.type.SakuraFilter;
import com.idx.inshowapp.filter.type.SweetFilter;

/**
 * Created by sunny on 18-8-14.
 */

public class FilterTypeFactory {


    public static GPUImageFilter initFilters(InShowFilterType filterTypes) {
        switch (filterTypes) {
            case SAKURA:
                return new SakuraFilter();
            case ROMANCE:
                return new RomanceFilter();
            case RETRO:
                return new RetroFilter();
            case SWEET:
                return new SweetFilter();
            case BALCKWHITE:
                return new BlackWhiteFilter();
            case LATTE:
                return new LatteFilter();
            default:
                return null;
        }
    }

    /**
     * 返回滤镜名称
     *
     * @param showFilterType 滤镜种类
     * @return 种类资源
     */
    public static String filterTypeName(InShowFilterType showFilterType) {
        switch (showFilterType) {
            case NONE:
                return "原图";
            case SAKURA:
                return "春风";
            case ROMANCE:
                return "酱紫";
            case RETRO:
                return "灰制";
            case SWEET:
                return "蓝调";
            case BALCKWHITE:
                return "黑白";
            case LATTE:
                return "午后";
            default:
                return "原图";

        }
    }

    /**
     * 返回滤镜种类对应的图片
     *
     * @param showFilterType 滤镜种类
     * @return 图片资源
     */
    public static int filterTypeImage(InShowFilterType showFilterType) {
        switch (showFilterType) {
            case SAKURA:
                return R.drawable.chufeng_skinned_image;
            case ROMANCE:
                return R.drawable.jiangzi_skinned_image;
            case RETRO:
                return R.drawable.greymatter_image;
            case SWEET:
                return R.drawable.landiao_skinned_image;
            case BALCKWHITE:
                return R.drawable.blackandwhite_image;
            case LATTE:
                return R.drawable.wuhou_skinned_image;
            default:
                return R.drawable.artwork_image;

        }
    }

}
