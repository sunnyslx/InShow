package com.idx.inshowapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idx.inshowapp.R;
import com.idx.inshowapp.beauty.BeautyTypeFilter;
import com.idx.inshowapp.filter.base.GPUImageFilter;
import com.idx.inshowapp.sticker.Icon;
import com.idx.inshowapp.utils.InShowParams;

import java.util.List;

/**
 * Created by Sunny on 18-10-10.
 */

public class BeautyAdapter extends IconAdapter {

    private OnBeautyChangeListener beautyChangeListener;

    public void setBeautyChangeListener(OnBeautyChangeListener beautyChangeListener) {
        this.beautyChangeListener = beautyChangeListener;
    }

    public BeautyAdapter(List<Icon> iconList) {
        super(iconList);
    }

    public IconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_icon,parent,false);
        final IconAdapter.ViewHolder holder = new IconAdapter.ViewHolder(view);
        holder.getIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Icon icon = iconList.get(position);
                beautyChangeListener.setBeautyFilter(icon.getResourceId());
            }
        });
        return holder;
    }

    public interface OnBeautyChangeListener {
        void setBeautyFilter(int id);
    }

}
