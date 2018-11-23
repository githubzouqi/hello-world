package com.example.pc2.carmapproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.entity.MenuEntity;
import com.example.pc2.carmapproject.interfaces.MyItemClickListener;

import java.util.List;

/**
 * 地图主界面选项的适配器
 */
public class MenusAdapter extends RecyclerView.Adapter<MenusAdapter.MyViewHolder>{

    private MyItemClickListener myItemClickListener;
    private List<MenuEntity> list_menu;
    private LayoutInflater inflater;

    // 注册监听器方法，提供给外部开发者调用
    public void setOnItemClick(MyItemClickListener myItemClickListener){
        this.myItemClickListener = myItemClickListener;
    }

    public MenusAdapter(Context context, List<MenuEntity> list_menu) {
        inflater = LayoutInflater.from(context);
        this.list_menu = list_menu;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_item_menu, parent, false);
        MyViewHolder holder = new MyViewHolder(view, myItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.iv_menu.setImageResource(list_menu.get(position).getIconId());
        holder.tv_menu.setText(list_menu.get(position).getIconShows());

    }

    @Override
    public int getItemCount() {
        return list_menu.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView iv_menu;
        TextView tv_menu;

        public MyViewHolder(final View itemView, final MyItemClickListener myItemClickListener) {
            super(itemView);
            iv_menu = itemView.findViewById(R.id.iv_menu);
            tv_menu = itemView.findViewById(R.id.tv_menu);

            // 给item设置点击事件
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myItemClickListener != null){
                        myItemClickListener.onItemClick(itemView, getAdapterPosition());// 点击item，调用接口的方法
                    }
                }
            });
        }
    }

}
