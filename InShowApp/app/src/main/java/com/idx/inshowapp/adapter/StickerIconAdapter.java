package com.idx.inshowapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.idx.inshowapp.R;
import com.idx.inshowapp.sticker.Icon;
import com.idx.inshowapp.utils.InShowParams;

import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by stefan on 18-10-27.
 */

public class StickerIconAdapter extends IconAdapter {

    public IconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_icon,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.getIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Icon icon = iconList.get(position);
                InShowParams.cameraBaseRenderer.setSticker(icon.getStickerResourceId());
            }
        });
        return holder;
    }

    public StickerIconAdapter(List<Icon> stickerIconList){
        super(stickerIconList);
    }
}
