package com.madongqiang.yeareffect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.madongqiang.yeareffect.view.TagsAdapter;

/**
 * Created by moxun on 16/3/4.
 */
public class ViewTagsAdapter extends TagsAdapter {
    private OnItemClickListener onItemClickListener;

    @Override
    public int getCount() {
        return 15;
    }

    @Override
    public View getView(final Context context, final int position, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.tag_item_view, parent, false);
        final ImageView ivSmall = view.findViewById(R.id.iv_small);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(ivSmall, position);
                }
//                Toast.makeText(context, "clicked:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getPopularity(int position) {
        return position % 5;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {
//        view.findViewById(R.id.android_eye).setBackgroundColor(themeColor);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
