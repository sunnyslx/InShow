package com.idx.inshowapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by ryan on 18-1-13.
 * Email: Ryan_chan01212@yeah.net
 */

public class SharePrefrenceUtils {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public SharePrefrenceUtils(Context context){
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("shopping", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void saveFirstAppStart(String firstStart, boolean isFirst){
        mEditor.putBoolean(firstStart,isFirst);
        mEditor.commit();
    }

    public boolean getFirstAppStart(String firstStart){
        return mSharedPreferences.getBoolean(firstStart,true);
    }

    public void insertWebUrl(String webname, String weburl){
        mEditor.putString(webname,weburl);
        mEditor.commit();
    }

    public String getWebUrl(String webname){
        return mSharedPreferences.getString(webname,null);
    }

    public void saveCurrentFragment(String curr_frag_id, String curr_frag_v){
        Log.i("ryan", "saveCurrentFragment: curr_frag_v = "+curr_frag_v);
        mEditor.putString(curr_frag_id,curr_frag_v);
        mEditor.commit();
    }

    public String getCurrentFragment(String curr_frag_id){
        return mSharedPreferences.getString(curr_frag_id,"");
    }

    public void saveChangeFragment(String first_change, boolean isFirstChange){
        Log.i("ryan", "saveChangeFragment: isFirstChange = "+isFirstChange);
        mEditor.putBoolean(first_change,isFirstChange);
        mEditor.commit();
    }

    public boolean getFirstChange(String first_change){
        return mSharedPreferences.getBoolean(first_change,false);
    }

    public void saveBackgroudActivity(String which_background_activity, String back_activity_name){
        mEditor.putString(which_background_activity,back_activity_name);
        mEditor.commit();
    }

    public String getBackgroundActivity(String which_background_activity){
        return mSharedPreferences.getString(which_background_activity,"");
    }
}
