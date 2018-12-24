package com.idx.inshowapp.baidu.unit.listener;

import com.idx.inshowapp.baidu.unit.model.CommunicateResponse;

/**
 * Created by Sunny on 18-9-7.
 */

public interface IVoiceActionListener {
    interface IActionCallback {
        void onResult(boolean sayBye);
    }
    boolean onAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema, IActionCallback actionCallback);
}
