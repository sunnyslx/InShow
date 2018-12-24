package com.idx.inshowapp.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


import com.idx.inshowapp.Actions;
import com.idx.inshowapp.MainActivity;
import com.idx.inshowapp.SlotsTypes;
import com.idx.inshowapp.baidu.control.TTSManager;
import com.idx.inshowapp.baidu.unit.model.CommunicateResponse;
import com.idx.inshowapp.utils.SharePrefrenceUtils;

import java.util.HashMap;

/**
 * Created by Sunny on 18-9-7.
 */

public class VoiceActionAdapter {

    private static final String TAG = VoiceActionAdapter.class.getSimpleName();
    private static final int ASK_BYE_DELAY = 2000;
    private static final int voiceHelpNum = 3;
    private Context mContext;
    private Intent mIntent;

    private SharePrefrenceUtils mSharePrefrenceUtils;

    private ISessionListener mSessionListener;

    private HashMap<String, String> mSlots = new HashMap<>();

    private Handler mHandler = new Handler();

    private IVoiceActionListener.IActionCallback mActionCallback;
    //拍照监听
    private ITakePhotoListener mTakePhotoListener;
    //照片保存监听
    private IPhotoListener mPhotoListener;
    //滤镜选择
    private IFilterSelectListener mFilterSelectListener;

    private TTSManager.SpeakCallback mSpeakCallback = new TTSManager.SpeakCallback() {
        @Override
        public void onSpeakStart() {

        }

        @Override
        public void onSpeakFinish() {
            result(true);
        }

        @Override
        public void onSpeakError() {

        }
    };

    public void setTakePhotoListener(ITakePhotoListener mTakePhotoListener) {
        this.mTakePhotoListener = mTakePhotoListener;
    }

    public void setPhotoListener(IPhotoListener iPhotoListener) {
        this.mPhotoListener = iPhotoListener;
    }

    public void setFilterSelectListener(IFilterSelectListener selectListener){
        this.mFilterSelectListener=selectListener;
    }

    public void setSessionListener(ISessionListener sessionListener) {
        mSessionListener = sessionListener;
    }


    public VoiceActionAdapter(Context context) {
        this.mContext = context;
        mIntent = new Intent(context, MainActivity.class);
        mSharePrefrenceUtils = new SharePrefrenceUtils(context);
    }

    public boolean action(CommunicateResponse.Action action, CommunicateResponse.Schema schema, IVoiceActionListener.IActionCallback actionCallback) {
        Log.i(TAG, "action: 分发事件");
        mActionCallback = actionCallback;
        return handleAction(action, schema);
    }

    /**
     * @param action
     * @param schema
     * @return
     */
    private boolean handleAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {
        mSlots.clear();
        Log.i(TAG, "handleAction: 处理不同业务逻辑");
        if (schema != null) {
            for (int i = 0; i < schema.botMergedSlots.size(); i++) {
                String type = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).type;
                String word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).original_word;
                mSlots.put(type, word);
            }
        }else {
            Log.i(TAG, "handleAction: schema为空");
        }
        switch (action.actionId) {
            case Actions.TAKE_PHOTO:
                Log.i(TAG, "handleAction: 进入takephoto()函数");
                takePhoto();
                return true;
            case Actions.PHOTO_SAVE:
                Log.i(TAG, "handleAction: 进入照片存储函数");
                photoSave();
                return true;
            case Actions.PHOTO_CANCEL:
                photoCancel();
                return true;
            case Actions.VOICE_EXIT:
                if (mSessionListener !=null){
                    mSessionListener.onSessionFinish();
                }
                return  true;
            case Actions.Filter.FILTER_NAME:
                return false;
            case Actions.Filter.FILTER_SELECT:
                Log.i(TAG, "handleAction: 进入滤镜选择函数");
                selectFilter();
                return true;
            default:
                return false;
        }
    }

    private void takePhoto() {
        if (mTakePhotoListener != null) {
            mTakePhotoListener.onTakePhoto();
        }
        result(false);
    }

    private void photoSave() {
        if (mPhotoListener != null) {
            mPhotoListener.photoSave();
        }
        result(false);
    }

    private void photoCancel() {
        if (mPhotoListener != null) {
            mPhotoListener.photoCancel();
        }
        result(false);
    }

    private void selectFilter() {
        String filterName= mSlots.get(SlotsTypes.USER_FILTER_NAME);
        Log.i(TAG, "selectFilter: filterName= "+filterName);
        Log.i(TAG, "selectFilter: mSlots="+mSlots.get("user_filter_name"));
        if (mFilterSelectListener !=null){
            if (filterName !=null && !filterName.equals("")){
                Log.i(TAG, "selectFilter:  filterName= "+filterName);
                mFilterSelectListener.FilterSelect(filterName);
            }
        }
    }

    /**
     * 执行成功后，回调
     *
     * @param sayBye 是否开启结束询问
     */
    private void result(final boolean sayBye) {
        if (mActionCallback != null) {
            if (sayBye) {
                if (mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActionCallback.onResult(sayBye);
                        }
                    }, ASK_BYE_DELAY);
                }
            } else {
                mActionCallback.onResult(sayBye);
            }
        }
    }
}
