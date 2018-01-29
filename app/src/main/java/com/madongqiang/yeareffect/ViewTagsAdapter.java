package com.madongqiang.yeareffect;

import android.app.Activity;
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
    private int width;
    private int height;
    private static final double VIEW_WIDTH_SCREEN_HEIGHT = 100D / 750D;
    private static final double VIEW_HEIGHT_WIDTH = 469D / 212D;

    public ViewTagsAdapter(Activity activity) {
        width = (int) (activity.getResources().getDisplayMetrics().widthPixels * VIEW_WIDTH_SCREEN_HEIGHT);
        height = (int) (width * VIEW_HEIGHT_WIDTH);
    }

    @Override
    public int getCount() {
        return 15;
    }

    @Override
    public View getView(final Context context, final int position, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.tag_item_view, parent, false);
        final ImageView ivSmall = view.findViewById(R.id.iv_small);
        ViewGroup.LayoutParams params = ivSmall.getLayoutParams();
        params.width = width;
        params.height = height;
        ivSmall.setLayoutParams(params);
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
