package com.example.pc2.carmapproject.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;

/**
 * 给recyclerView的item设置间距的类
 */
public class MyItemDecoration extends RecyclerView.ItemDecoration{


    public static final String LINEAR_HORIZONTAL = "LINEAR_HORIZONTAL";// 线性布局水平滑动
    public static final String LINEAR_VERTICAL = "LINEAR_VERTICAL";// 线性布局垂直滑动
    public static final String GRIDLAYOUT = "GRIDLAYOUT";// 网格布局
    private String type;
    private int space;
    private int spanCount;// 网格布局时布局展示的列数

    public MyItemDecoration(String type, int space) {
        this.type = type;
        this.space = space;
    }

    public MyItemDecoration(String type, int space, int spanCount) {
        this.type = type;
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

//        LogUtil.e("TAG", "MyItemDecoration, top = " + outRect.top);
//        LogUtil.e("TAG", "MyItemDecoration, bottom = " + outRect.bottom);
//        LogUtil.e("TAG", "MyItemDecoration, left = " + outRect.left);
//        LogUtil.e("TAG", "MyItemDecoration, right = " + outRect.right);

        if (type.equals(LINEAR_HORIZONTAL)){// 线性水平排列的item间隔设置

            outRect.right = space;
            if (parent.getChildAdapterPosition(view) == 0){// 表示是第一个item
                outRect.left = space;
            }

        }

        if (type.equals(LINEAR_VERTICAL)){// 线性垂直排列的item间隔设置
            outRect.bottom = space;
            if (parent.getChildAdapterPosition(view) == 0){
                outRect.top = space;
            }
        }

        if (type.equals(GRIDLAYOUT)){// 网格布局的item间隔设置
            int itemPos = parent.getChildAdapterPosition(view);// 获取布局中item的位置
            // 网格布局设置第一行的上边距
            if (itemPos < spanCount){
                outRect.top = space;
            }

            if (((itemPos + 1) % 4) == 1){// 网格布局中的第一列
                outRect.left = space;
            }

            outRect.bottom = space;
            outRect.right = space;
        }

    }

}
