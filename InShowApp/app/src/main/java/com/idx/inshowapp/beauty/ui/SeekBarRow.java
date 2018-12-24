package com.idx.inshowapp.beauty.ui;

import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Sunny on 18-10-9.
 */

public class SeekBarRow {
    private int id;
    private int initValue;
    private SeekBar seekBar;
    private TextView values;

    public SeekBarRow(int id, int initValue, SeekBar seekBar, TextView values) {
        this.id = id;
        this.initValue = initValue;
        this.seekBar = seekBar;
        this.values = values;
    }
}
