package com.idx.inshowapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.idx.inshowapp.R;
import com.idx.inshowapp.filter.FilterTypeFactory;
import com.idx.inshowapp.filter.InShowFilterType;

/**
 * Created by sunny on 18-8-2.
 */

public class FilterAdapter  extends RecyclerView.Adapter<FilterAdapter.InShowHolder>{
    private static final String TAG=FilterAdapter.class.getSimpleName();
    private Context mContext;
    private InShowFilterType[] filterTypes;
    private OnItemClickListener onItemClickListener;
    private int mSelectId=0;
    private boolean isClick=false;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public FilterAdapter(Context context, InShowFilterType[] filterTypes) {
        super();
        this.mContext=context;
        this.filterTypes=filterTypes;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filterTypes.length;
    }

    @Override
    public InShowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_about_filter
        ,parent,false);
        return new InShowHolder(view);
    }

    @Override
    public void onBindViewHolder(InShowHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick=true;
                mSelectId=position;
                notifyItemChanged(position);
                onItemClickListener.onFilterChange(filterTypes[position],position);
            }
        });
        holder.filter_textview.setText(FilterTypeFactory.filterTypeName(filterTypes[position]));
        holder.filter_imageView.setImageResource(FilterTypeFactory.filterTypeImage(filterTypes[position]));
        if (position==mSelectId && isClick){
            holder.filter_imageView.setBackgroundResource(R.drawable.select);
        }
    }

    public class InShowHolder extends RecyclerView.ViewHolder{

        private ImageView filter_imageView;
        private TextView filter_textview;

        private InShowHolder(View itemView) {
            super(itemView);
            filter_imageView =itemView.findViewById(R.id.igv_filter);
            filter_textview =itemView.findViewById(R.id.tv_filter);
        }
    }

    /**
     * recycleView item点击接口
     */
    public interface OnItemClickListener{
        void onFilterChange(InShowFilterType filterType,int position);
    }

}
