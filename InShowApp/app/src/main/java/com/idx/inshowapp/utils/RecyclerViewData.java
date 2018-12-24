package com.idx.inshowapp.utils;

import com.idx.inshowapp.R;
import com.idx.inshowapp.sticker.Icon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 18-10-29.
 */

public class RecyclerViewData {
    List<Icon> animalIconList = new ArrayList<>();
    List<Icon> funnyIconList = new ArrayList<>();
    List<Icon> fruitIconList = new ArrayList<>();
    List<Icon> beautifyIconList = new ArrayList<>();
    List<Icon> boyIconList = new ArrayList<>();
    List<Icon> beautyIconList = new ArrayList<>();


    public List<Icon> getAnimalIconList() {
        return animalIconList;
    }
    public List<Icon> getFunnyIconList() {
        return funnyIconList;
    }
    public List<Icon> getFruitIconList() {
        return fruitIconList;
    }
    public List<Icon> getBeautifyIconList(){
        return beautifyIconList;
    }
    public List<Icon> getBoyIconList(){
        return boyIconList;
    }
    public List<Icon> getBeautyIconList(){
        return beautyIconList;
    }

    public RecyclerViewData(){
        initAnimlaIconList();
        initFunnyIconList();
        initFruitIconList();
        initBeautifyIconList();
        initBoyIconList();
        initBeautyIconList();
    }

    private void initAnimlaIconList(){
        Icon animalIcon;
        animalIcon = new Icon(R.drawable.tags_whitecat,R.drawable.baimao);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_black_cat,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_zhubajie,R.drawable.zhubajie);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_pig,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_fox,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_husky,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_shibainu,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_rabbit1,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_rabbit2,-1);
        animalIconList.add(animalIcon);
        animalIcon = new Icon(R.drawable.tags_bear,-1);
        animalIconList.add(animalIcon);
    }

    private void initFunnyIconList(){
        Icon funnyIcon;
        funnyIcon = new Icon(R.drawable.thicklips,-1);
        funnyIconList.add(funnyIcon);
        funnyIcon = new Icon(R.drawable.ruhua,-1);
        funnyIconList.add(funnyIcon);
        funnyIcon = new Icon(R.drawable.meipo,-1);
        funnyIconList.add(funnyIcon);
        funnyIcon = new Icon(R.drawable.sunglasses,-1);
        funnyIconList.add(funnyIcon);
        funnyIcon = new Icon(R.drawable.aureola,-1);
        funnyIconList.add(funnyIcon);
        funnyIcon = new Icon(R.drawable.gege,-1);
        funnyIconList.add(funnyIcon);
    }

    private void initFruitIconList(){
        Icon fruitIcon;
        fruitIcon = new Icon(R.drawable.apple,-1);
        fruitIconList.add(fruitIcon);
        fruitIcon = new Icon(R.drawable.peach,-1);
        fruitIconList.add(fruitIcon);
        fruitIcon = new Icon(R.drawable.strawberry,-1);
        fruitIconList.add(fruitIcon);
        fruitIcon = new Icon(R.drawable.cherry,-1);
        fruitIconList.add(fruitIcon);
        fruitIcon = new Icon(R.drawable.orange,-1);
        fruitIconList.add(fruitIcon);
        fruitIcon = new Icon(R.drawable.watermelon,-1);
        fruitIconList.add(fruitIcon);
    }

    private void initBeautifyIconList(){
        Icon beautifyIcon;
        beautifyIcon = new Icon(R.drawable.artwork_image,"原图");
        beautifyIconList.add(beautifyIcon);
        beautifyIcon = new Icon(R.drawable.eyebrow_liuye_black,"柳叶眉");
        beautifyIconList.add(beautifyIcon);
        beautifyIcon = new Icon(R.drawable.eyebrow_yizi_black,"一字眉");
        beautifyIconList.add(beautifyIcon);
        beautifyIcon = new Icon(R.drawable.eyebrow_yingqi_black,"英气眉");
        beautifyIconList.add(beautifyIcon);
        beautifyIcon = new Icon(R.drawable.eyebrow_oushi_black,"欧式眉");
        beautifyIconList.add(beautifyIcon);
        beautifyIcon = new Icon(R.drawable.eyebrow_liuxingmei_black,"流星眉");
        beautifyIconList.add(beautifyIcon);
    }

    private void initBoyIconList(){
        Icon boyIcon;
        boyIcon = new Icon(R.drawable.helmet,-1);
        boyIconList.add(boyIcon);
        boyIcon = new Icon(R.drawable.sciencefiction,-1);
        boyIconList.add(boyIcon);
        boyIcon = new Icon(R.drawable.handlebar,-1);
        boyIconList.add(boyIcon);
        boyIcon = new Icon(R.drawable.juanmao,-1);
        boyIconList.add(boyIcon);
        boyIcon = new Icon(R.drawable.beard,-1);
        boyIconList.add(boyIcon);
    }

    //初始化美颜图标List
    private void initBeautyIconList(){
        Icon beautyIcon;
        beautyIcon = new Icon(R.drawable.artwork_icon,"原图");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.face_meifu_icon,"美肤");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.meibai_icon,"美白");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.face_lift_icon,"瘦脸");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.jaw_icon,"下巴");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.forehead_icon,"额头");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.bigeye_icon,"大眼");
        beautyIconList.add(beautyIcon);
        beautyIcon = new Icon(R.drawable.nose_icon,"瘦鼻");
        beautyIconList.add(beautyIcon);
    }
}
