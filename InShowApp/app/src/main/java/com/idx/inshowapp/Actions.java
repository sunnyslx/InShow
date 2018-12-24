package com.idx.inshowapp;


/**
 * Created by Sunny on 18-9-4.
 */

public interface Actions {

    String TAKE_PHOTO = "take_photo_satisfy";
    String PHOTO_SAVE = "photo_save_satisfy";
    String VOICE_EXIT = "voice_session_exit_satisfy";
    String PHOTO_CANCEL = "photo_cancle_satisfy";

    interface Filter {

        String FILTER_NAME = "filter_select_user_filter_name_clarify";
        String FILTER_SELECT = "filter_select_satisfy";
    }

    interface Sticker {
        String STICKER_SELECT = "select_sticker_satisfy";
    }
}
