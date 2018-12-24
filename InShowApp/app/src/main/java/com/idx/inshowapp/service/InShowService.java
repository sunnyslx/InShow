package com.idx.inshowapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.TtsMode;
import com.idx.inshowapp.R;
import com.idx.inshowapp.baidu.SpeakDialog;
import com.idx.inshowapp.baidu.control.TTSManager;
import com.idx.inshowapp.baidu.control.UnitManager;
import com.idx.inshowapp.baidu.control.WakeupManager;
import com.idx.inshowapp.baidu.recog.RecogResult;
import com.idx.inshowapp.baidu.control.RecognizerManager;
import com.idx.inshowapp.baidu.recog.listener.StatusRecogListener;
import com.idx.inshowapp.baidu.unit.listener.ISessionListener;
import com.idx.inshowapp.baidu.wakeup.IWakeupListener;
import com.idx.inshowapp.baidu.wakeup.WakeUpResult;
import com.idx.inshowapp.utils.MathTool;
import com.idx.inshowapp.utils.MyLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.idx.inshowapp.baidu.recog.IStatus.STATUS_NONE;
import static com.idx.inshowapp.baidu.recog.IStatus.STATUS_READY;

public class InShowService extends Service implements EventListener {
    private static final String TAG = InShowService.class.getSimpleName();
    //唤醒
    private static final int MSG_WAKE_UP_START = 0x001;
    //唤醒状态
    private static final int MSG_WAKE_UP_STATUE = 0x002;
    //唤醒停止
    private static final int MSG_WAKE_UP_STOP = 0x006;
    //开始识别
    private static final int MSG_RECOGNIZE_START = 0x003;
    //识别结束
    private static final int MSG_RECOGNIZE_FINISH = 0x004;
    //识别错误
    private static final int MSG_RECOGNIZE_ERROR = 0x005;
    //会话结束
    private static final int MSG_SESSION_FINISH=0x007;
    //会话异常
    private static final int MSG_SESSION_ERROR=0x008;
    //超时时长常量
    private static final int CONSTANT_TIME_STEP = 15000; //15s
    //超时消息常量
    private static final int CONSTANT_TIME_TICK = 0x301;
    //相邻唤醒间隔时长
    private static final int CONSTANT_WAKE_UP_SPACE = 5000; //5s
    //唤醒后，识别回溯时长
    private static final int BACK_TRACK = 1000; //1s

    private boolean logTime = true;
    private VoiceHandler voiceHandler;
    /**
     * 数据部分为本地化，若迁移至伺服器，可联网获取，动态变更
     * 再见语音数据
     */
    private String[] mVoiceArrayBye;
    /**
     * 欢迎语音数据
     */
    private String[] mVoiceArrayWel;
    /**
     * 抱歉语音数据
     */
    private String[] mVoiceArraySorry;
    /**
     * 重复语音数据
     */
    private String[] mVoiceRepeat;
    /**
     * 语音交互声波纹
     */
    private SpeakDialog mSpeakDialog = null;
    //唤醒
    private WakeupManager mWakeupManager;
    //识别
    private RecognizerManager mRecognizerManager;
    //唤醒后，识别回溯时长
    private int backTrackInMs = 1500;
    //唤醒状态
    private boolean isWaked = false;
    //消息接收状态，定时置false，接收到消息置为true
    private boolean isReceived = false;
    //唤醒服务状态标识
    private int wakeUpStatus = STATUS_NONE;

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (voiceHandler != null) {
                voiceHandler.postDelayed(timeRunnable, CONSTANT_TIME_STEP);
                if (!isReceived) {
                    voiceHandler.sendEmptyMessage(CONSTANT_TIME_TICK);
                }
                isReceived = false;
            }
        }
    };

    private class VoiceHandler extends Handler {
        private long startTime;
        @Override
        public void handleMessage(Message msg) {
            isReceived=true;
            switch (msg.what) {
                //唤醒成功的状态下，对话框显示，tts发音
                case MSG_WAKE_UP_STATUE:  //唤醒成功状态
                    Log.i(TAG, "handleMessage: 开启会话功能");
                    if (isWaked) {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - startTime < CONSTANT_WAKE_UP_SPACE) {
                            return;
                        }
                    }
                    isWaked = true;
                    startTime = System.currentTimeMillis();
                    voiceHandler.removeCallbacks(timeRunnable);
                    voiceHandler.post(timeRunnable);
                    if (mSpeakDialog == null) {
                        mSpeakDialog = new SpeakDialog(getBaseContext());
                        mSpeakDialog.showReady();
                    }
                    String voiceWel = mVoiceArrayWel[MathTool.randomValue(mVoiceArrayWel.length)];
                    Log.i(TAG, "handleMessage: voiceWel=" + voiceWel);

                    TTSManager.getInstance().speak(voiceWel, new TTSManager.SpeakCallback() {
                        @Override
                        public void onSpeakStart() {
                            Log.d(TAG, "onSpeakStart: ");
                        }

                        @Override
                        public void onSpeakFinish() {
                            Log.d(TAG, "onSpeakFinish: ");
                            voiceHandler.sendEmptyMessageDelayed(MSG_RECOGNIZE_START, BACK_TRACK);
                        }

                        @Override
                        public void onSpeakError() {
                            Log.d(TAG, "onSpeakError: ");

                        }
                    });
                    break;
                case MSG_RECOGNIZE_START:  //开始识别
                    if (mSpeakDialog != null) {
                        mSpeakDialog.showSpeaking();
                        startRecognize();
                    }
                    break;

                case MSG_RECOGNIZE_FINISH: //识别结束
                    if (mSpeakDialog != null) {
                        mSpeakDialog.showReady();
                        stopRecognize();
                    }
                    break;
                case MSG_SESSION_ERROR:  //会话异常
                case MSG_SESSION_FINISH: //会话结束
                case MSG_RECOGNIZE_ERROR://识别错误
                    UnitManager.getInstance(getBaseContext()).enableSession(false);
                    if (mSpeakDialog != null) {
                        mSpeakDialog.dismiss();
                        stopRecognize();
                    }
                    isWaked = false;
                    voiceHandler.removeCallbacks(timeRunnable);
                    break;
                case MSG_WAKE_UP_START:   //开始唤醒
                    Log.i(TAG, "handleMessage:启动唤醒 ");
                    wakeUpStart();
                    break;
                case MSG_WAKE_UP_STOP:  //唤醒停止
                    wakeupStop();
                    stopRecognize();
                    break;
                case CONSTANT_TIME_TICK:  //查询超时
                    String sorry = mVoiceArraySorry[MathTool.randomValue(mVoiceArraySorry.length)];
                    TTSManager.getInstance().speak(sorry);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
        String logTxt = "name: " + s;
        if (s1 != null && !s1.isEmpty()) {
            logTxt += " ;params :" + s1;
        } else if (bytes != null) {
            logTxt += " ;data length=" + bytes.length;
        }
        printLog(logTxt);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        voiceHandler = new VoiceHandler();
        initData();
        initVoice();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //通知handler唤醒
        sendEmptyMsg(MSG_WAKE_UP_START);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initData() {
        mVoiceArrayBye = getResources().getStringArray(R.array.voice_bye);
        mVoiceArrayWel = getResources().getStringArray(R.array.voice_welcome);
        mVoiceArraySorry = getResources().getStringArray(R.array.voice_sorry);
        mVoiceRepeat = getResources().getStringArray(R.array.voice_repeat);
    }

    private void initVoice() {
        mWakeupManager = new WakeupManager(getBaseContext(), new SimpleWakeupListener());
        mRecognizerManager = new RecognizerManager(getBaseContext(), new MessageStatusRecogListener());
        //初始化语音合成
        TTSManager.getInstance().init(getBaseContext(), TtsMode.ONLINE);
        UnitManager.getInstance(getBaseContext()).setSessionListener(new SessionListener());
    }

    private Map<String, Object> initParam() {
        Map<String, Object> params = new TreeMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        return params;
    }

    //开始唤醒
    private void wakeUpStart(){
        if (wakeUpStatus==STATUS_NONE){
            mWakeupManager.start(initParam());
            wakeUpStatus = STATUS_READY;
        }
    }

    //停止唤醒
    private void wakeupStop(){
        if (wakeUpStatus==STATUS_READY){
            mWakeupManager.stop();
            mWakeupManager.release();
            mWakeupManager = null;
            wakeUpStatus = STATUS_NONE;
        }
    }

    //开始识别
    private void startRecognize() {
        Log.i(TAG, "startRecognize: 执行识别");
        // 此处 开始正常识别流程
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
        params.put(SpeechConstant.PID, 1536);
        if (backTrackInMs > 0) {
            // 方案1  唤醒词说完后，直接接句子，中间没有停顿。开启回溯，连同唤醒词一起整句识别。
            // System.currentTimeMillis() - backTrackInMs ,  表示识别从backTrackInMs毫秒前开始
            params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);
        }
        mRecognizerManager.cancel();
        mRecognizerManager.start(params);
    }

    //识别结束
    private void stopRecognize() {
        mRecognizerManager.stop();
    }

    /**
     * 唤醒事件监听器
     */
    public class SimpleWakeupListener implements IWakeupListener {

        private static final String TAG = "SimpleWakeupListener";

        @Override
        public void onSuccess(String word, WakeUpResult result) {
            Log.i(TAG, "onSuccess: 唤醒成功 ，唤醒词为:  " + word);
            //唤醒成功，启动tts
            sendEmptyMsg(MSG_WAKE_UP_STATUE);
        }

        @Override
        public void onStop() {
            MyLogger.info(TAG, "唤醒词识别结束：");
        }

        @Override
        public void onError(int errorCode, String errorMessge, WakeUpResult result) {
            MyLogger.info(TAG, "唤醒错误：" + errorCode + ";错误消息：" + errorMessge + "; 原始返回" + result.getOrigalJson());
        }

        @Override
        public void onASrAudio(byte[] data, int offset, int length) {
            MyLogger.error(TAG, "audio data： " + data.length);
        }
    }

    /**
     * 识别事件监听器
     */
    public class MessageStatusRecogListener extends StatusRecogListener {


        private long speechEndTime = 0;

        private boolean needTime = true;

        private static final String TAG = "MesStatusRecogListener";

        public MessageStatusRecogListener() {
        }

        @Override
        public void onAsrReady() {
            super.onAsrReady();
            speechEndTime = 0;
            Log.i(TAG, "onAsrReady: 准备说话");
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_WAKEUP_READY, "引擎就绪，可以开始说话。");
        }

        @Override
        public void onAsrBegin() {
            super.onAsrBegin();
            Log.i(TAG, "onAsrBegin: 开始说话");
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN, "检测到用户说话");
        }

        @Override
        public void onAsrEnd() {
            super.onAsrEnd();
            Log.i(TAG, "onAsrEnd: 说话结束");
            speechEndTime = System.currentTimeMillis();
        }

        @Override
        public void onAsrPartialResult(String[] results, RecogResult recogResult) {
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                    "临时识别结果，结果是“" + results[0] + "”；原始json：" + recogResult.getOrigalJson());
            super.onAsrPartialResult(results, recogResult);
        }

        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {
            super.onAsrFinalResult(results, recogResult);
//            int length = results[0].length() - 1;
            String msg = results[0];
            if (msg.equals("")) {
                TTSManager.getInstance().speak(mVoiceRepeat[MathTool.randomValue(mVoiceRepeat.length)], new TTSManager.SpeakCallback() {
                    @Override
                    public void onSpeakStart() {

                    }

                    @Override
                    public void onSpeakFinish() {
                        sendEmptyMsg(MSG_RECOGNIZE_START, BACK_TRACK);
                    }

                    @Override
                    public void onSpeakError() {

                    }
                });
                return;
            } else {
                //识别结束
                sendEmptyMsg(MSG_RECOGNIZE_FINISH);
                //将识别后的语句交由Unit处理
                Log.i(TAG, "onAsrFinalResult: 将识别结果交给unit处理");
                UnitManager.getInstance(getBaseContext()).sendMessage(msg);
            }

            String message = "识别结束，结果是”" + results[0] + "”";
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                    message + "；原始json：" + recogResult.getOrigalJson());

            if (speechEndTime > 0) {
                long currentTime = System.currentTimeMillis();
                long diffTime = currentTime - speechEndTime;
                message += "；说话结束到识别结束耗时【" + diffTime + "ms】" + currentTime;

            }
            speechEndTime = 0;
            sendMessage(message, status, true);
        }

        @Override
        public void onAsrFinishError(int errorCode, int subErrorCode, String descMessage,
                                     RecogResult recogResult) {
            super.onAsrFinishError(errorCode, subErrorCode, descMessage, recogResult);
            Log.i(TAG, "onAsrFinishError: startRecognize，后说再见");
            String bye = mVoiceArrayBye[MathTool.randomValue(mVoiceArrayBye.length)];
            TTSManager.getInstance().speak(bye, new TTSManager.SpeakCallback() {
                @Override
                public void onSpeakStart() {

                }

                @Override
                public void onSpeakFinish() {
                    //超时交互，自动结束会话
                    sendEmptyMsg(MSG_RECOGNIZE_ERROR);
                }

                @Override
                public void onSpeakError() {

                }
            });
            String message = "【asr.finish事件】识别错误, 错误码：" + errorCode + " ," + subErrorCode + " ; " + descMessage;
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL, message);
            if (speechEndTime > 0) {
                long diffTime = System.currentTimeMillis() - speechEndTime;
                message += "。说话结束到识别结束耗时【" + diffTime + "ms】";
            }
            speechEndTime = 0;
            sendMessage(message, status, true);
            speechEndTime = 0;
        }

        @Override
        public void onAsrOnlineNluResult(String nluResult) {
            super.onAsrOnlineNluResult(nluResult);
            if (!nluResult.isEmpty()) {
                sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL, "原始语义识别结果json：" + nluResult);
            }
        }

        @Override
        public void onAsrFinish(RecogResult recogResult) {
            super.onAsrFinish(recogResult);
            Log.i(TAG, "onAsrFinish: ");
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_FINISH, "识别一段话结束。如果是长语音的情况会继续识别下段话。");

        }

        /**
         * 长语音识别结束
         */
        @Override
        public void onAsrLongFinish() {
            super.onAsrLongFinish();
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH, "长语音识别结束。");
        }


        /**
         * 使用离线命令词时，有该回调说明离线语法资源加载成功
         */
        @Override
        public void onOfflineLoaded() {
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_LOADED, "离线资源加载成功。没有此回调可能离线语法功能不能使用。");
        }

        /**
         * 使用离线命令词时，有该回调说明离线语法资源加载成功
         */
        @Override
        public void onOfflineUnLoaded() {
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED, "离线资源卸载成功。");
        }

        @Override
        public void onAsrExit() {
            super.onAsrExit();
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_EXIT, "识别引擎结束并空闲中");
        }

        private void sendStatusMessage(String eventName, String message) {
            message = "[" + eventName + "]" + message;
            sendMessage(message, status);
        }

        private void sendMessage(String message) {
            sendMessage(message, WHAT_MESSAGE_STATUS);
        }

        private void sendMessage(String message, int what) {
            sendMessage(message, what, false);
        }


        private void sendMessage(String message, int what, boolean highlight) {

            if (needTime && what != STATUS_FINISHED) {
                message += "  ;time=" + System.currentTimeMillis();
            }
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = status;
            if (highlight) {
                msg.arg2 = 1;
            }
            msg.obj = message + "\n";

        }
    }

    /**
     * 会话状态监听器
     */
    private  class SessionListener implements ISessionListener{

        @Override
        public void onSessionFinish() {
            Log.i(TAG, "onSessionFinish: ");
            sendEmptyMsg(MSG_SESSION_FINISH);
        }

        @Override
        public void onRegContinue() {
            Log.i(TAG, "onRegContinue: ");
            sendEmptyMsg(MSG_RECOGNIZE_START, BACK_TRACK);
        }

        @Override
        public void onSessionError() {
            //Unit Error，自动结束会话
            Log.i(TAG, "onSessionError: ");
            sendEmptyMsg(MSG_SESSION_ERROR);
        }
    }

    private void printLog(String text) {
        if (logTime) {
            text += "  ;time=" + System.currentTimeMillis();
        }
        text += "\n";
        Log.i(getClass().getName(), text);
    }

    private void sendEmptyMsg(int what, int timeDelay) {
        if (voiceHandler != null) {
            voiceHandler.sendEmptyMessageDelayed(what, timeDelay);
        }
    }

    private void sendEmptyMsg(int what) {
        if (voiceHandler != null) {
            voiceHandler.sendEmptyMessage(what);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeupManager != null) {
            mWakeupManager.stop();
            mWakeupManager.release();
            mWakeupManager = null;
        }

        if (mRecognizerManager != null) {
            mRecognizerManager.stop();
            mRecognizerManager.release();
            mRecognizerManager = null;
        }

        if (mSpeakDialog != null) {
            mSpeakDialog.dismiss();
            mSpeakDialog = null;
        }

        if (voiceHandler != null) {
            voiceHandler.removeCallbacks(timeRunnable);
            voiceHandler = null;
        }

        TTSManager.getInstance().release();
        UnitManager.getInstance(getBaseContext()).release();
    }

}
