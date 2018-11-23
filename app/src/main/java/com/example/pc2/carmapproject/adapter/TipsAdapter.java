package com.example.pc2.carmapproject.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.utils.LogUtil;

import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.MyViewHolder>{

    private LayoutInflater inflater;
    private List<String> tipsList;

    // 构造方法
    public TipsAdapter(Context context, List<String> tipsList) {

        this.tipsList = tipsList;
        this.inflater = LayoutInflater.from(context);

    }

    // 创建ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_item_tips, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }


    // view绑定
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv_item_tip.setText(tipsList.get(position));
        if (tipsList.size() == 1){
            holder.check_box_tip.setVisibility(View.GONE);
        }else if(tipsList.size() > 1){
            holder.check_box_tip.setVisibility(View.VISIBLE);
            holder.check_box_tip.setChecked(false);
        }

    }

    //item数量
    @Override
    public int getItemCount() {
        return ((tipsList == null || tipsList.size() == 0) ? 0 : tipsList.size());
    }

    // 控件缓存类
    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tv_item_tip;
        CheckBox check_box_tip;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_item_tip = itemView.findViewById(R.id.tv_item_tip);
            check_box_tip = itemView.findViewById(R.id.check_box_tip);

        }
    }

}
