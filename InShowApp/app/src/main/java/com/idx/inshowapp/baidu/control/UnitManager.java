package com.idx.inshowapp.baidu.control;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.idx.inshowapp.R;
import com.idx.inshowapp.baidu.unit.APIService;
import com.idx.inshowapp.baidu.unit.exception.UnitError;
import com.idx.inshowapp.baidu.unit.listener.IFilterSelectListener;
import com.idx.inshowapp.baidu.unit.listener.IPhotoListener;
import com.idx.inshowapp.baidu.unit.listener.ISessionListener;
import com.idx.inshowapp.baidu.unit.listener.ITakePhotoListener;
import com.idx.inshowapp.baidu.unit.listener.IVoiceActionListener;
import com.idx.inshowapp.baidu.unit.listener.OnResultListener;
import com.idx.inshowapp.baidu.unit.listener.VoiceActionAdapter;
import com.idx.inshowapp.baidu.unit.model.AccessToken;
import com.idx.inshowapp.baidu.unit.model.CommunicateResponse;
import com.idx.inshowapp.utils.AuthInfo;
import com.idx.inshowapp.utils.MathTool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sunny on 18-9-7.
 */

public class UnitManager {
    private static final String TAG = "UnitManager";
    //unit会话ID
    private String sessionId ;
    //SmartSpeakDock场景ID
    private int sceneId =22822;//15213;
    /**
     * 场景Token
     */
    private String accessToken;
    /**
     * 会话管理标识
     */
    private boolean enableSession = false;
    /**
     * 退出voice标识
     */
    private boolean isOver = false;
    /**
     * 文本转语音管理器
     */
    private TTSManager ttsManager;
    /**
     * Unit API
     */
    private APIService mApiService;
    /**
     * 会话状态监听器
     * 1.Unit Session Error触发，关闭Session（UnitManager中触发）
     * 2.Unit Session Finish，即再见触发，关闭Session（UnitManager中触发）
     * 3.识别超时，触发Recognized错误，关闭Session（SpeakService中RecognizerManager监听器触发）
     * 4.Unit Session未关闭，即以上条件不满足，继续触发识别 onRegContinue
     */
    private ISessionListener mSessionListener;
    /**
     * 重复语音数据
     */
    private String[] mVoiceMore;

    private static UnitManager INSTANCE=null;
    /**
     * 语音响应处理适配
     */
    private VoiceActionAdapter mVoiceAdapter;

    private UnitManager(Context context) {
        init(context);
    }

    public static UnitManager getInstance(Context context){
        if (INSTANCE==null){
            synchronized (UnitManager.class){
                INSTANCE=new UnitManager(context);
            }
        }
        return INSTANCE;
    }


    /**
     * 初始化
     *
     * @param context 上下文
     **/
    public void init(Context context) {
        Map<String, Object> authParams = AuthInfo.getAuthParams(context);
        mApiService = APIService.getInstance();
        mApiService.init(context);
        mApiService.initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                accessToken = result.getAccessToken();
                Log.d(TAG, "get token: " + accessToken);
            }

            @Override
            public void onError(UnitError error) {
                Log.d(TAG, "onError: ");
            }
        }, (String) authParams.get(AuthInfo.META_APP_KEY), (String) authParams.get(AuthInfo.META_APP_SECRET));
        mVoiceAdapter = new VoiceActionAdapter(context);
        ttsManager = TTSManager.getInstance();
        mVoiceMore = context.getResources().getStringArray(R.array.voice_more);
    }

    /**
     * 处理数据，播放语音，调用动作执行函数
     *
     * @param result 解析后的数据
     **/
    private void handleResponse(CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            if (actionList.size() > 1) {
                //意图引导
                List<SpeechSynthesizeBag> bags = new ArrayList<>();
                for (CommunicateResponse.Action action : actionList) {

                    if (!TextUtils.isEmpty(action.say)) {
                        Log.d(TAG, "handleResponse: StringBuilder " + action.say);
                        SpeechSynthesizeBag msg = new SpeechSynthesizeBag();
                        msg.setText(action.say);
                        bags.add(msg);
                        for (String hintText : action.hintList) {
                            SpeechSynthesizeBag hint = new SpeechSynthesizeBag();
                            hint.setText(hintText);
                            bags.add(hint);
                        }

                    }
                }
                ttsManager.batSpeak(bags);

            } else if (actionList.size() == 1) {
                //单一意图，意图引导最后出口亦是单一意图
                final CommunicateResponse.Action action = actionList.get(0);
                final CommunicateResponse.Schema schema = result.schema;
                Log.i(TAG, "handleResponse: 单一意图，拿到schema="+schema);
                if (!TextUtils.isEmpty(action.say)) {
                    //有语音文本信息返回
                    List<SpeechSynthesizeBag> bags = new ArrayList<>();
                    SpeechSynthesizeBag msg = new SpeechSynthesizeBag();
                    msg.setText(action.say);
                    bags.add(msg);
                    for (String hintText : action.hintList) {
                        SpeechSynthesizeBag hint = new SpeechSynthesizeBag();
                        hint.setText(hintText);
                        bags.add(hint);
                    }

                    //播放Unit返回的语音包
                    ttsManager.batSpeak(bags, new TTSManager.SpeakCallback() {
                        @Override
                        public void onSpeakStart() {

                        }

                        @Override
                        public void onSpeakFinish() {
                            //执行自己的业务逻辑，回调执行，即语音播放结束后回调
                            Log.d(TAG, "onSpeakFinish: " + Thread.currentThread().getId());
                            executeTask(action, schema);
                        }

                        @Override
                        public void onSpeakError() {

                        }
                    });

                } else if (!TextUtils.isEmpty(action.mainExe)) {
                    //没有语音信息返回
                    Log.d(TAG, "handleResponse: mainExe, " + action.mainExe);
                    //执行自己的业务逻辑
                    executeTask(action, schema);
                }

            }
        }
    }

    /**
     * 发送数据至语音云端处理接口
     *
     * @param message 语音转化后的文本信息
     */
    public void sendMessage(String message) {
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        Log.i(TAG, "sendMessage文本信息为: message="+message);

        mApiService.communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {
                Log.i(TAG, "onResult: 处理返回结果");
                handleResponse(result);

            }

            @Override
            public void onError(UnitError error) {
                Log.i(TAG, "onError发生错误: error="+error);
                if (mSessionListener != null) {
                    mSessionListener.onSessionError();
                }
            }
        }, sceneId, message, sessionId);

    }


    /**
     * @param action 执行的动作信息
     * @param schema 对应的词槽
     */
    private void executeTask(final CommunicateResponse.Action action, final CommunicateResponse.Schema schema) {
        if (mVoiceAdapter != null) {
            Log.i(TAG, "run: action.id = " + action.actionId);
            isOver = mVoiceAdapter.action(action, schema, new IVoiceActionListener.IActionCallback() {
                @Override
                public void onResult(boolean sayBye) {
                    if (enableSession) {
                        if (sayBye) {
                            //询问是否关闭会话
                            String voice = mVoiceMore[MathTool.randomValue(mVoiceMore.length)];
                            TTSManager.getInstance().speak(voice, new TTSManager.SpeakCallback() {
                                @Override
                                public void onSpeakStart() {
                                }

                                @Override
                                public void onSpeakFinish() {
                                    if (mSessionListener != null) {
                                        if (enableSession) {
                                            mSessionListener.onRegContinue();
                                        }
                                    }
                                }

                                @Override
                                public void onSpeakError() {

                                }
                            });
                        } else {
                            if (mSessionListener != null) {
                                mSessionListener.onSessionFinish();
                            }
                        }

                    } else {
                        if (mSessionListener != null) {
                            mSessionListener.onSessionFinish();
                        }
                    }
                }
            });

            if (!isOver) {
                //如未结束，继续识别
                if (mSessionListener != null) {
                    mSessionListener.onRegContinue();
                }
            } else {
                sessionId = "";
            }
        }
    }

    public void setTakePhotoListener(ITakePhotoListener photoListener){
        mVoiceAdapter.setTakePhotoListener(photoListener);
    }

    public void setPhotoListener(IPhotoListener iPhotoListener){
        mVoiceAdapter.setPhotoListener(iPhotoListener);
    }

    public void setFilterSelectListener(IFilterSelectListener selectListener){
        mVoiceAdapter.setFilterSelectListener(selectListener);
    }

    public void setSessionListener(ISessionListener sessionListener) {
        mSessionListener = sessionListener;
        mVoiceAdapter.setSessionListener(sessionListener);
    }

    public void enableSession(boolean enable) {
        enableSession = enable;
    }

    public void release() {
        if (ttsManager != null) {
            ttsManager.release();
        }

        if (INSTANCE != null) {
            INSTANCE = null;
        }

        if (mApiService != null) {
            mApiService = null;
        }
    }
}
