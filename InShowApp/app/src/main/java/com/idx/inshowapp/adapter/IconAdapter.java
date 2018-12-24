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

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by stefan on 18-11-7.
 */

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {
    protected List<Icon> iconList;
    @Override
    public IconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Icon icon = iconList.get(position);
        holder.icon.setImageResource(icon.getResourceId());
        Log.d(TAG, "onBindViewHolder: "+icon.getName());
        if (icon.getName() != null){
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setPadding(0,10,0,0);
            holder.text.setText(icon.getName());
        }
    }
    
    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView getIcon() {
            return icon;
        }

        private ImageView icon;

        public TextView getText() {
            return text;
        }

        private TextView text;
        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            text = itemView.findViewById(R.id.icon_text);
        }
    }

    public IconAdapter(List<Icon> iconList){
        this.iconList = iconList;
    }

}
