package com.madongqiang.yeareffect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.madongqiang.yeareffect.event.BackEvent;

import de.greenrobot.event.EventBus;

public class SecondActivity extends AppCompatActivity {
    private ImageView ivBig;
    private boolean firstInit = true;
    private int originWidth;
    private ValueAnimator reverseAnimator;
    private float lastInFraction = 0;
    private float lastOutFraction = 0;
    private boolean animationFinished = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_second);
        ViewGroup layerBig = findViewById(R.id.layer_big);
        ivBig = findViewById(R.id.iv_big);

        layerBig.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (firstInit) {
                    firstInit = false;
                    originWidth = ivBig.getMeasuredWidth();

                    reverseAnimator = ValueAnimator.ofInt(originWidth, 0, originWidth, 0, originWidth, 0, originWidth, 0, originWidth, 0, originWidth, 0, originWidth, 0, originWidth);
                    reverseAnimator.setDuration(2000);
                    reverseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    reverseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int currentValue = (int) animation.getAnimatedValue();
                            float currentFraction = animation.getAnimatedFraction();
                            if (currentFraction > 13f / 14f && lastInFraction < 13f / 14f) {
                                lastInFraction = currentFraction;
                                ivBig.setBackgroundResource(R.drawable.card_negative);
                            }
                            ViewGroup.LayoutParams params = ivBig.getLayoutParams();
                            params.width = currentValue;
                            ivBig.setLayoutParams(params);
                        }
                    });
                    reverseAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationFinished = true;
                        }
                    });
                    reverseAnimator.start();

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!animationFinished) {
            return;
        }
        ValueAnimator exitAnimator = ValueAnimator.ofInt(originWidth, 0, originWidth);
        exitAnimator.setDuration(500);
        exitAnimator.setInterpolator(new LinearInterpolator());
        exitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentFraction = animation.getAnimatedFraction();
                if (currentFraction > 0.5f && lastOutFraction < 0.5f) {
                    lastOutFraction = currentFraction;
                    ivBig.setBackgroundResource(R.drawable.card_positive);
                }
                int currentValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = ivBig.getLayoutParams();
                params.width = currentValue;
                ivBig.setLayoutParams(params);
            }
        });
        exitAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                EventBus.getDefault().post(new BackEvent());
                KShareViewActivityManager.getInstance(SecondActivity.this).finish(SecondActivity.this);
            }
        });
        exitAnimator.start();
    }
}
