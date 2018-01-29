package com.madongqiang.yeareffect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.madongqiang.yeareffect.event.BackEvent;
import com.madongqiang.yeareffect.view.TagCloudView;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    private TagCloudView tagCloudView;
    private ViewTagsAdapter viewTagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);
        tagCloudView = (TagCloudView) findViewById(R.id.tag_cloud);

        viewTagsAdapter = new ViewTagsAdapter(this);
        viewTagsAdapter.setOnItemClickListener(new ViewTagsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                tagCloudView.setOnTouch(true);
                tagCloudView.animate()
                        .alpha(0)
                        .setDuration(KShareViewActivityManager.getInstance(MainActivity.this).duration)
                        .start();
                KShareViewActivityManager
                        .getInstance(MainActivity.this)
                        .startActivity(MainActivity.this, BigSignCardActivity.class,
                                R.layout.tag_item_view,R.layout.activity_big_sign_card, view);
            }
        });
        tagCloudView.setAdapter(viewTagsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tagCloudView.setOnTouch(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(BackEvent event) {
        tagCloudView.animate()
                .alpha(1f)
                .setDuration(KShareViewActivityManager.getInstance(MainActivity.this).duration)
                .start();
    }
}
