package com.idx.inshowapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.idx.inshowapp.adapter.BeautyAdapter;
import com.idx.inshowapp.adapter.FilterAdapter;
import com.idx.inshowapp.adapter.StickerIconAdapter;
import com.idx.inshowapp.adapter.StickerViewPageAdapter;
import com.idx.inshowapp.baidu.control.UnitManager;
import com.idx.inshowapp.baidu.unit.listener.IFilterSelectListener;
import com.idx.inshowapp.baidu.unit.listener.ITakePhotoListener;
import com.idx.inshowapp.beauty.BeautyTypeFilter;
import com.idx.inshowapp.camera.InShowCamera;
import com.idx.inshowapp.data.InShowFilter;
import com.idx.inshowapp.filter.InShowFilterType;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.renderer.CameraRenderView;
import com.idx.inshowapp.renderer.FrameDataCallback;
import com.idx.inshowapp.service.InShowService;
import com.idx.inshowapp.sticker.Icon;
import com.idx.inshowapp.utils.FilterUtils;
import com.idx.inshowapp.utils.InShowEngine;
import com.idx.inshowapp.utils.InShowParams;
import com.idx.inshowapp.utils.RecyclerViewData;
import com.orhanobut.logger.Logger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static com.idx.inshowapp.utils.ImageUtils.saveBitmap;


public class MainActivity extends BaseActivity implements View.OnClickListener
        , FrameDataCallback,SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private List<ImageView> imgList;
    private TextView mTvDismiss;
    private ImageView mTackphoto;
    private ImageView mSwitchCameraId;
    private ImageView mIgbFilter;
    private ImageView mIgbSticker;
    private ImageView mIgbBeauty;
    private ImageView mReturn;
    private SeekBar mFilterSeekBar;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mFilterlayout;
    private RecyclerView mFiltercyview;
    private LinearLayout mBeautylayout;
    private LinearLayout mStickerlayout;
    private InShowFilter inShowFilter;
    private InShowFilterType[] inShowFilterTypes = new InShowFilterType[]{
            InShowFilterType.NONE,
            InShowFilterType.SAKURA,
            InShowFilterType.ROMANCE,
            InShowFilterType.RETRO,
            InShowFilterType.SWEET,
            InShowFilterType.BALCKWHITE,
            InShowFilterType.LATTE
    };
    private InShowEngine mShowEngine;
    private FilterAdapter filterAdapter;

    private ViewPager viewPager;
    private List<View> viewList;
    private List<Integer> defualtImageTabIds;
    private List<Integer> selectImageTabIds;
    private ImageView returnTab;
    private ImageView unSelectTab;
    private int pageIndex = 0;
    private int lastPageIndex = 0;
    private boolean tabClicked = false;

    private CameraRenderView cameraRenderView;

    private SeekBar mSeekBar;
    private TextView tvFilterSeekBar;

    //美颜类型
    private GPUImageFilter beautyFilter;
    //上一个美颜的类型
    private GPUImageFilter lastBeautyFilter;
    //美颜SeekBar的进程值
    private int skinBeautyProgress;
    private int whiteningProgress;
    private int thinFaceProgress;
    private int jawProgress;
    private int foreHeadProgress;
    private int bigEyesProgress;
    private int thinNoseProgress;
    //当前美颜SeekBar的进程值
    private int currentProgress;
    //SharePreferences
    private SharedPreferences preferences;
    private int beautyResourceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.d("onCreate");
        intent = new Intent(this, InShowService.class);
        startService(intent);
        InShowCamera.setContext(this);
        initView();
        if (runFirst()) {
            Logger.d("onCreate: 首次启动");
            mTvDismiss.setText(getResources().getString(R.string.tv_dismiss));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvDismiss.setVisibility(View.GONE);
                        }
                    });
                }
            }, 5000);
        }
        setDataToRecycleView();
       
        UnitManager.getInstance(this).setTakePhotoListener(iTakePhotoListener);
        UnitManager.getInstance(this).setFilterSelectListener(selectListener);
        initViewPage();
        initRecyclerView();
        initBeautyFilter();
    }

    //判断是否为首次启动
    public boolean runFirst() {
        preferences = getSharedPreferences("inShowDate", MODE_PRIVATE);
        Boolean first_run = preferences.getBoolean("first", true);
        if (first_run) {
            preferences.edit().putBoolean("first", false).apply();
            return true;
        } else {
            return false;
        }
    }

    private void initView() {
        mTackphoto = findViewById(R.id.igb_tackPhoto);
        InShowEngine.Builder builder = new InShowEngine.Builder();
        mShowEngine = builder.build((CameraRenderView) findViewById(R.id.camera_surface));
        cameraRenderView = findViewById(R.id.camera_surface);
        cameraRenderView.setFrameCallback(this);
        mSwitchCameraId = findViewById(R.id.switch_camera);
        mRelativeLayout = findViewById(R.id.select);
        mIgbFilter = findViewById(R.id.igb_filter);
        mIgbSticker = findViewById(R.id.igb_sticker);
        mIgbBeauty = findViewById(R.id.igb_beauty);
        mFilterlayout = findViewById(R.id.filter_layout);
        mFiltercyview = findViewById(R.id.recyclerView_filter);
        mReturn = findViewById(R.id.filter_return_image);
        mBeautylayout = findViewById(R.id.beauty_layout);
        mStickerlayout = findViewById(R.id.sticker_layout);
        mTvDismiss = findViewById(R.id.tv_dismiss);
        mSeekBar = findViewById(R.id.seekbar_beauty);
        initStickerTab();
        mFilterSeekBar=findViewById(R.id.filter_SeekBar);
        tvFilterSeekBar=findViewById(R.id.tv_filterSeekBar);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        cameraRenderView.setOnClickListener(this);
        mTackphoto.setOnClickListener(this);
        mSwitchCameraId.setOnClickListener(this);
        mIgbFilter.setOnClickListener(this);
        mIgbSticker.setOnClickListener(this);
        mIgbBeauty.setOnClickListener(this);
        mBeautylayout.setOnClickListener(this);
        mStickerlayout.setOnClickListener(this);
        mReturn.setOnClickListener(this);
        returnTab.setOnClickListener(this);
        unSelectTab.setOnClickListener(this);
        mFilterSeekBar.setOnSeekBarChangeListener(this);
        for (int i = 0;i<5;i++){
            imgList.get(i).setOnClickListener(this);
        }
    }

    private void initViewPage(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view0 = inflater.inflate(R.layout.sticker_animal,null);
        View view1 = inflater.inflate(R.layout.sticker_funny,null);
        View view2 = inflater.inflate(R.layout.sticker_fruit,null);
        View view3 = inflater.inflate(R.layout.sticker_beautify,null);
        View view4 = inflater.inflate(R.layout.sticker_boy,null);

        viewList = new ArrayList<>();
        viewList.add(view0);
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        viewList.add(view4);

        final StickerViewPageAdapter pageAdapter = new StickerViewPageAdapter(viewList);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 0){
                    if (!tabClicked&&pageIndex != lastPageIndex){
                        imgList.get(pageIndex).setImageResource(selectImageTabIds.get(pageIndex));
                        imgList.get(lastPageIndex).setImageResource(defualtImageTabIds.get(lastPageIndex));
                        lastPageIndex = pageIndex;
                    }
                    tabClicked = false;
                }
            }
        });
    }

    private void initStickerTab(){
        returnTab = findViewById(R.id.return_tab_img);
        unSelectTab = findViewById(R.id.unselect_tab_img);
        ImageView animalTab = findViewById(R.id.animal_tab_img);
        ImageView beautifyTab = findViewById(R.id.beautify_tab_img);
        ImageView boyTab = findViewById(R.id.boy_tab_img);
        ImageView fruitTab = findViewById(R.id.fruit_tab_img);
        ImageView funnyTab = findViewById(R.id.funny_tab_img);

        imgList = new ArrayList<>();
        imgList.add(animalTab);
        imgList.add(funnyTab);
        imgList.add(fruitTab);
        imgList.add(beautifyTab);
        imgList.add(boyTab);

        defualtImageTabIds = new ArrayList<>();
        defualtImageTabIds.add(R.drawable.animal_icon_default);
        defualtImageTabIds.add(R.drawable.funny_icon_default);
        defualtImageTabIds.add(R.drawable.fruit_icon_default);
        defualtImageTabIds.add(R.drawable.decorate_icon_default);
        defualtImageTabIds.add(R.drawable.boy_icon_default);

        selectImageTabIds = new ArrayList<>();
        selectImageTabIds.add(R.drawable.animal_icon_select);
        selectImageTabIds.add(R.drawable.funny_icon_select);
        selectImageTabIds.add(R.drawable.fruit_icon_select);
        selectImageTabIds.add(R.drawable.decorate_icon_select);
        selectImageTabIds.add(R.drawable.boy_icon_select);
    }


    //填充贴纸的recyclerView
    private void initRecyclerView() {
        RecyclerViewData recyclerViewData = new RecyclerViewData();
        initStickerSignRecyclerView(viewList.get(0),R.id.animal_recycler,
                recyclerViewData.getAnimalIconList(),5);
        initStickerSignRecyclerView(viewList.get(1),R.id.funny_recycler,
                recyclerViewData.getFunnyIconList(),5);
        initStickerSignRecyclerView(viewList.get(2),R.id.fruit_recycler,
                recyclerViewData.getFruitIconList(),5);
        initStickerSignRecyclerView(viewList.get(3),R.id.beautify_recycler,
                recyclerViewData.getBeautifyIconList(),6);
        initStickerSignRecyclerView(viewList.get(4),R.id.boy_recycler,
                recyclerViewData.getBoyIconList(),5);
        //填充美颜的recyclerView
        initBeautyRecyclerView(recyclerViewData);
    }

    private void initStickerSignRecyclerView(View view, int resourceId,
                                             List<Icon> iconList, int spanCount){
        RecyclerView recyclerView = view.findViewById(resourceId);
        GridLayoutManager layoutManager = new GridLayoutManager(this,spanCount);
        recyclerView.setLayoutManager(layoutManager);
        StickerIconAdapter adapter = new StickerIconAdapter(iconList);
        recyclerView.setAdapter(adapter);
    }

    private void initBeautyRecyclerView(RecyclerViewData recyclerViewData){
        RecyclerView recyclerView = mBeautylayout.findViewById(R.id.beauty_recycler);
        final GridLayoutManager layoutManager = new GridLayoutManager(this,8);
        recyclerView.setLayoutManager(layoutManager);
        BeautyAdapter adapter = new BeautyAdapter(recyclerViewData.getBeautyIconList());
        adapter.setBeautyChangeListener(new BeautyAdapter.OnBeautyChangeListener() {
            @Override
            public void setBeautyFilter(int id) {
                beautyResourceId = id;
                beautyFilter = InShowParams.beautyTypeFilter.initBeauty(id);
                if (currentProgress==0&&beautyFilter!=lastBeautyFilter&&lastBeautyFilter!=
                        BeautyTypeFilter.getSkinBeauty())
                    InShowParams.beautyGroup.getBeautyFilters().remove(lastBeautyFilter);
                Log.d(TAG, "setBeautyFilter: "+beautyFilter.getClass()+currentProgress);
                if (!InShowParams.beautyGroup.getBeautyFilters().contains(beautyFilter))
                    InShowParams.beautyGroup.getBeautyFilters().add(beautyFilter);
                switch(id){
                    case R.drawable.face_meifu_icon:
                            mSeekBar.setProgress(skinBeautyProgress);
                        break;
                    case R.drawable.meibai_icon:
                            mSeekBar.setProgress(whiteningProgress);
                        break;
                    case R.drawable.face_lift_icon:
                            mSeekBar.setProgress(thinFaceProgress);
                        break;
                    case R.drawable.jaw_icon:
                            mSeekBar.setProgress(jawProgress);
                        break;
                    case R.drawable.forehead_icon:
                            mSeekBar.setProgress(foreHeadProgress);
                        break;
                    case R.drawable.bigeye_icon:
                            mSeekBar.setProgress(bigEyesProgress);
                        break;
                    case R.drawable.nose_icon:
                            mSeekBar.setProgress(thinNoseProgress);
                        break;
                    default:
                        break;
                }
                lastBeautyFilter = beautyFilter;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void initBeautyFilter(){
        beautyFilter = InShowParams.beautyTypeFilter.initBeauty(R.drawable.face_meifu_icon);
        Log.d(TAG, "initBeautyFilter: beautyFilter "+beautyFilter);
        beautyResourceId = preferences.getInt("resourceId",0);
        skinBeautyProgress = preferences.getInt("skinBeautyProgress",0);
        whiteningProgress = preferences.getInt("whiteningProgress",0);
        if (whiteningProgress>0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getWhitening());
        thinFaceProgress = preferences.getInt("thinFaceProgress",0);
        if (thinFaceProgress>0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getThinFace());
        jawProgress = preferences.getInt("jawProgress",0);
        if (jawProgress >0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getJaw());
        foreHeadProgress = preferences.getInt("foreHeadProgress",0);
        if (foreHeadProgress>0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getForehead());
        bigEyesProgress = preferences.getInt("bigEyesProgress",0);
        if (bigEyesProgress>0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getBigeyes());
        thinNoseProgress = preferences.getInt("thinNoseProgress",0);
        if (thinNoseProgress>0)
            InShowParams.beautyGroup.getBeautyFilters().add(BeautyTypeFilter.getThinNose());

        switch(beautyResourceId){
            case R.drawable.face_meifu_icon:
                Log.d(TAG, "selectAndSetBeauty: "+skinBeautyProgress);
                mSeekBar.setProgress(skinBeautyProgress);
                beautyFilter.setFlag(skinBeautyProgress);
                break;
            case R.drawable.meibai_icon:
                mSeekBar.setProgress(whiteningProgress);
                beautyFilter.setFlag(whiteningProgress);
                break;
            case R.drawable.face_lift_icon:
                mSeekBar.setProgress(thinFaceProgress);
                break;
            case R.drawable.jaw_icon:
                mSeekBar.setProgress(jawProgress);
                break;
            case R.drawable.forehead_icon:
                mSeekBar.setProgress(foreHeadProgress);
                break;
            case R.drawable.bigeye_icon:
                mSeekBar.setProgress(bigEyesProgress);
                break;
            case R.drawable.nose_icon:
                mSeekBar.setProgress(thinNoseProgress);
                break;
            default:
                break;
        }
    }

    /**
     * 填充滤镜的recycleView
     */
    private void setDataToRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFiltercyview.setLayoutManager(layoutManager);
        filterAdapter = new FilterAdapter(MainActivity.this,
                inShowFilterTypes);
        mFiltercyview.setAdapter(filterAdapter);
        filterAdapter.setOnItemClickListener(onItemClickListener);
    }

    private void showRelativeLayout(){
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFilterlayout.setVisibility(View.GONE);
        mBeautylayout.setVisibility(View.GONE);
        mStickerlayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_surface:
                showRelativeLayout();
                break;
            case R.id.igb_tackPhoto:
                takePhoto();
                break;
            case R.id.switch_camera:
                switchCameraId();
                break;
            case R.id.igb_filter:
                mRelativeLayout.setVisibility(View.GONE);
                mFilterlayout.setVisibility(View.VISIBLE);
                break;
            case R.id.igb_beauty:
                mRelativeLayout.setVisibility(View.GONE);
                mBeautylayout.setVisibility(View.VISIBLE);
                break;
            case R.id.igb_sticker:
                mRelativeLayout.setVisibility(View.GONE);
                mStickerlayout.setVisibility(View.VISIBLE);
                break;
            case R.id.filter_return_image:
                showRelativeLayout();
                break;
            case R.id.return_tab_img:
                showRelativeLayout();
                break;
            case R.id.unselect_tab_img:

                break;
            case R.id.animal_tab_img:
                if (pageIndex != 0){
                    tabClick(0);
                }
                break;
            case R.id.funny_tab_img:
                if (pageIndex != 1){
                    tabClick(1);
                }
                break;
            case R.id.fruit_tab_img:
                if (pageIndex != 2){
                    tabClick(2);
                }
                break;
            case R.id.beautify_tab_img:
                if (pageIndex != 3){
                    tabClick(3);
                }
                break;
            case R.id.boy_tab_img:
                if (pageIndex != 4){
                    tabClick(4);
                }
                break;
            default:
                break;
        }
    }

    private void tabClick(int index){
        tabClicked = true;
        imgList.get(index).setImageResource(selectImageTabIds.get(index));
        imgList.get(lastPageIndex).setImageResource(defualtImageTabIds.get(lastPageIndex));
        viewPager.setCurrentItem(index);
        lastPageIndex = index;
    }

    private FilterAdapter.OnItemClickListener onItemClickListener = new FilterAdapter.OnItemClickListener() {
        @Override
        public void onFilterChange(InShowFilterType filterType, int position) {
            cameraRenderView.setFilter(filterType);
            filterAdapter.notifyDataSetChanged();
        }
    };



    //语音拍照接口
    private ITakePhotoListener iTakePhotoListener=new ITakePhotoListener() {
        @Override
        public void onTakePhoto() {
            Log.i(TAG, "onTakePhoto: 语音控制拍照");
            takePhoto();
        }
    };

    //语音控制滤镜选择接口实现
    private IFilterSelectListener selectListener=new IFilterSelectListener() {
        @Override
        public void FilterSelect(String filterName) {
            //显示滤镜选择界面
            mRelativeLayout.setVisibility(View.GONE);
            mFilterlayout.setVisibility(View.VISIBLE);
            //拿到滤镜名称
            String name=filterName.substring(0,2);
            Log.i(TAG, "FilterSelect: name="+name);
            inShowFilter=FilterUtils.getFilter().get(name);
            cameraRenderView.setFilter(inShowFilter.getInShowFilterType());
            filterAdapter.notifyDataSetChanged();
        }
    };

    //进度条拖动事件监听
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: beautyFilter "+beautyFilter);
            beautyFilter.setFlag(progress);
            currentProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            switch(beautyResourceId){
                case R.drawable.face_meifu_icon:
                    skinBeautyProgress = currentProgress;
                    break;
                case R.drawable.meibai_icon:
                    whiteningProgress = currentProgress;
                    break;
                case R.drawable.face_lift_icon:
                    thinFaceProgress = currentProgress;
                    break;
                case R.drawable.jaw_icon:
                    jawProgress = currentProgress;
                    break;
                case R.drawable.forehead_icon:
                    foreHeadProgress = currentProgress;
                    break;
                case R.drawable.bigeye_icon:
                    bigEyesProgress = currentProgress;
                    break;
                case R.drawable.nose_icon:
                    thinNoseProgress = currentProgress;
                    break;
                default:
                    break;
            }
        }
    };






    //选择摄像头
    private void switchCameraId() {
        Log.i(TAG, "switchCameraId: ");
        if (getNumberOfCameras() > 1) {
            InShowCamera.switchCamera();
        } else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.cameraId_one),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {

        cameraRenderView.takePhoto();
    }

    @Override
    public void onFrame(final int width, final int height, final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                ByteBuffer b=ByteBuffer.wrap(bytes);
                bitmap.copyPixelsFromBuffer(b);
                Log.i(TAG, "run: bitmap="+bitmap);
                if (requestPermission){
                    Uri uri=Uri.parse(saveBitmap(bitmap));
                    Intent intent=new Intent(MainActivity.this,PhotoActivity.class);
                    intent.setData(uri);
                    startActivity(intent);
                    bitmap.recycle();
                }
            }
        }).start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i(TAG, "onProgressChanged: " + progress);
        tvFilterSeekBar.setText(" "+seekBar.getProgress());
        mShowEngine.setBeautyLevel(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InShowCamera.releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("skinBeautyProgress", skinBeautyProgress);
        editor.putInt("resourceId",beautyResourceId);
        editor.putInt("whiteningProgress",whiteningProgress);
        editor.putInt("thinFaceProgress",thinFaceProgress);
        editor.putInt("jawProgress", jawProgress);
        editor.putInt("foreHeadProgress",foreHeadProgress);
        editor.putInt("bigEyesProgress",bigEyesProgress);
        editor.putInt("thinNoseProgress",thinNoseProgress);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InShowCamera.releaseCamera();
        InShowParams.detectFace.releaseFacepp();
        stopService(intent);
    }
}
