package com.madongqiang.yeareffect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kot32 on 16/1/21. FirstActivity 中的View 暂不支持 WRAP_CONTENT ,SecondActivity 中的View 可以
 */
public class KShareViewActivityManager {
    /**
     * 当前要启动其他 activity 的 activity
     */
    private Activity one;
    /**
     * 被启动的 activity
     */
    private Class two;
    private Handler replaceViewHandler;
    /**
     * 共享的元素在当前 activity 的view
     */
    private HashMap<Object, View> shareViews = new HashMap<>();
    /**
     * 共享的元素在目标 activity 的view
     */
    private HashMap<View, ShareViewInfo> shareViewPairs = new HashMap<>();
    private KShareViewActivityAction kShareViewActivityAction;
    /**
     * 启动的 activity 跟布局
     */
    private ViewGroup secondActivityLayout;
    /** 启动的 activity 布局文件 ID **/
    private int targetResourceId;
    private ViewGroup copyOfFirstActivityLayout;
    private ViewGroup baseFrameLayout;

    public long duration = 300;
    private boolean isMatchedFirst;
    private boolean isMatchedSecond;
    private List<View> copyViews = new ArrayList<>(2);
    private int mRequestCode = -255;

    private Intent mIntent;

    private static KShareViewActivityManager INSTANCE;
    private static final double VIEW_WIDTH_SCREEN_WIDTH = 527D / 750D;
    private static final double VIEW_HEIGHT_WIDTH = 1164D / 527D;

    {
        replaceViewHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                afterAnimation();
            }
        };

        kShareViewActivityAction = new KShareViewActivityAction() {

            @Override
            public void onAnimatorStart() {

            }

            @Override
            public void onAnimatorEnd() {

            }

            @Override
            public void changeViewProperty(View view) {

            }

        };
    }

    /**
     * 每两个Activity 对应一个Manager
     *
     * @return
     */
    public static KShareViewActivityManager getInstance(Activity activity) {

        if (INSTANCE != null && INSTANCE.one != null && INSTANCE.two != null) {

            INSTANCE.isMatchedFirst = activity.equals(INSTANCE.one);
            INSTANCE.isMatchedSecond = (activity.getClass().getSimpleName().equals(INSTANCE.two.getSimpleName()));

            if (INSTANCE.isMatchedFirst || INSTANCE.isMatchedSecond) {
                return INSTANCE;
            }
        }
        INSTANCE = new KShareViewActivityManager();
        return INSTANCE;

    }

    /**
     * 根据两个Activity 的布局中的View Tag 是否相同来判断是否属于一个元素
     */
    public void startActivity(Activity one, Class two, int oringinActivityLayoutResourceId,
                              int targetActivityLayoutResourceId, View... shareViews) {
        this.shareViews.clear();
        this.shareViewPairs.clear();

        this.one = one;
        this.two = two;
        for (View v : shareViews) {
            this.shareViews.put(v.getTag(), v);
        }

        baseFrameLayout = (ViewGroup) one.findViewById(Window.ID_ANDROID_CONTENT);

        targetResourceId = targetActivityLayoutResourceId;
        secondActivityLayout = (ViewGroup) LayoutInflater.from(one).inflate(targetActivityLayoutResourceId, null);

        copyOfFirstActivityLayout = (ViewGroup) LayoutInflater.from(one).inflate(oringinActivityLayoutResourceId, null);

        beforeAnimation();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void finish(Activity finishActivity) {
        if (isMatchedFirst || (one != null && one.isDestroyed())) {
            Log.e("警告", "不能在这个页面调用finish 动画");
            finishActivity.finish();
            return;
        }
        if (isMatchedSecond) {
            kShareViewActivityAction.onAnimatorStart();
            finishActivityAnimation(finishActivity);
            one = null;
            two = null;
        }
    }

    private void startIntent() {
        if (mIntent == null) {
            mIntent = new Intent(one, two);
        }
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (mRequestCode != -255) {
            one.startActivityForResult(mIntent,mRequestCode);
        } else {
            one.startActivity(mIntent);
        }
        one.overridePendingTransition(0, 0);
        mIntent = null;
        mRequestCode = -255;
    }

    /**
     * 位移前测量各种数据
     */
    private void beforeAnimation() {

        findAllTargetViews(secondActivityLayout);
        final int[] currentIndex = { 0 };

        for (final ShareViewInfo viewInfo : shareViewPairs.values()) {

            if (secondActivityLayout.getParent() != null) {
                baseFrameLayout.removeView(secondActivityLayout);
            }
            secondActivityLayout.setAlpha(0);
            baseFrameLayout.addView(secondActivityLayout,
                                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                 ViewGroup.LayoutParams.MATCH_PARENT));

            viewInfo.view.post(new Runnable() {

                @Override
                public void run() {
                    viewInfo.width = viewInfo.view.getWidth();
                    viewInfo.height = viewInfo.view.getHeight();
                    viewInfo.locationOnScreen = new Point(getViewLocationOnScreen(viewInfo.view)[0],
                                                          getViewLocationOnScreen(viewInfo.view)[1]);

                    synchronized (currentIndex) {
                        if (currentIndex[0] == shareViewPairs.values().size() - 1) {
                            startActivityAnimation();
                        }
                        currentIndex[0]++;
                    }

                }
            });

        }

        kShareViewActivityAction.onAnimatorStart();

    }

    /**
     * 因为有的View 在ViewGroup 当中，因此需要将它们放到最外层
     */
    private void startActivityAnimation() {

        final int[] currentIndex = { 0 };
        for (final View v : shareViewPairs.keySet()) {

            final View copyOfView = copyOfFirstActivityLayout.findViewWithTag(v.getTag());
            if (copyOfView == null) {
                Log.e("警告", "传入的源View 所在xml id 错误，如果该View 在ListView 中，请传入ListView 的Item 布局xml");
                startIntent();
                return;
            }
            if (copyOfView.getParent() != null) {
                if (!copyOfView.getParent().getClass().getSimpleName().equals("ViewRootImpl")) {
                    ((ViewGroup) copyOfView.getParent()).removeView(copyOfView);
                }
            }

            FrameLayout.LayoutParams copyParams = new FrameLayout.LayoutParams(v.getWidth(), v.getHeight());
            copyParams.setMargins(getViewLocationOnScreen(v)[0], getViewLocationOnScreen(v)[1] - getTitleHeight(one),
                                  0, 0);
            fillViewProperty(v, copyOfView);
            kShareViewActivityAction.changeViewProperty(copyOfView);
            baseFrameLayout.addView(copyOfView, copyParams);
            copyViews.add(copyOfView);
            copyOfView.postInvalidate();
            v.setAlpha(0);

            final ShareViewInfo pair = shareViewPairs.get(v);
            copyOfView.post(new Runnable() {

                @Override
                public void run() {
                    startViewAnimation(copyOfView, pair, currentIndex, new AnimationEnd() {

                        @Override
                        public void onEnd() {
                            kShareViewActivityAction.onAnimatorEnd();
                            startIntent();
                            replaceViewHandler.sendEmptyMessageDelayed(1, 500);
                        }
                    }, duration);
                }
            });

        }
    }

    private void finishActivityAnimation(final Activity target) {
        final int[] currentIndex = { 0 };

        for (View v : shareViewPairs.keySet()) {

            ShareViewInfo pair = shareViewPairs.get(v);

            View sourceView = target.getWindow().getDecorView().findViewWithTag(pair.view.getTag());

            startViewAnimation(sourceView, new ShareViewInfo(v, new Point(getViewLocationOnScreen(v)[0],
                                                                          getViewLocationOnScreen(v)[1]), v.getWidth(),
                                                             v.getHeight()), currentIndex, new AnimationEnd() {

                @Override
                public void onEnd() {
                    kShareViewActivityAction.onAnimatorEnd();
                    target.finish();
                    target.overridePendingTransition(0, 0);
                    replaceViewHandler.sendEmptyMessageDelayed(1, 500);
                }
            }, duration - 100);

        }
    }

    private void startViewAnimation(View origin, ShareViewInfo target, final int[] lock, final AnimationEnd animationEnd, long duration) {

        float w2 = target.width;
        float w1 = origin.getWidth();
        float h2 = target.height;
        float h1 = origin.getHeight();

        int x2 = target.locationOnScreen.x;
        int x1 = getViewLocationOnScreen(origin)[0];

        int y2 = target.locationOnScreen.y;
        int y1 = getViewLocationOnScreen(origin)[1];

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);

        // 放大
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(origin, "scaleX", w2 / w1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(origin, "scaleY", h2 / h1);
        // 位移
        ObjectAnimator translationX = ObjectAnimator.ofFloat(origin, "translationX", x2 - x1 - w1 * (1 - (w2 / w1)) / 2);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(origin, "translationY", y2 - y1 - h1 * (1 - (h2 / h1)) / 2);

        animatorSet.playTogether(scaleX, scaleY, translationX, translationY);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                synchronized (lock) {
                    if (lock[0] == shareViewPairs.keySet().size() - 1) {
                        animationEnd.onEnd();
                    }
                    lock[0]++;
                }
            }
        });

        animatorSet.start();
    }

    private void afterAnimation() {
        baseFrameLayout.removeView(secondActivityLayout);
        for (View v : shareViews.values()) {
            v.setTranslationX(0);
            v.setTranslationY(0);
            v.setScaleX(1);
            v.setScaleY(1);
            v.setAlpha(1);
        }
        for (View v : copyViews) {
            baseFrameLayout.removeView(v);
        }

    }

    private void findAllTargetViews(ViewGroup viewGroup) {
        // TODO: 2018/1/29 layout
        for (Object keyTag : shareViews.keySet()) {
            if (targetResourceId == R.layout.activity_big_sign_card && "img".equals(keyTag)) {
                View view = secondActivityLayout.findViewWithTag(keyTag);

                int width = (int) (one.getResources().getDisplayMetrics().widthPixels * VIEW_WIDTH_SCREEN_WIDTH);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.width = width;
                params.height = (int) (width * VIEW_HEIGHT_WIDTH);
                view.setLayoutParams(params);

                shareViewPairs.put(shareViews.get(keyTag), new ShareViewInfo(view,
                        new Point()));
            } else {
                shareViewPairs.put(shareViews.get(keyTag), new ShareViewInfo(secondActivityLayout.findViewWithTag(keyTag),
                        new Point()));
            }
        }
    }

    private int[] getViewLocationOnScreen(View view) {
        int[] location = { 0, 0 };
        view.getLocationOnScreen(location);
        return location;
    }

    private int getTitleHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentTop - statusBarHeight;

        return statusBarHeight * 2 + titleBarHeight;
    }

    private static class ShareViewInfo {

        public View view;
        private Point locationOnScreen;
        private float width;
        public float height;

        private ShareViewInfo(View view, Point locationOnScreen) {
            this.view = view;
            this.locationOnScreen = locationOnScreen;
        }

        private ShareViewInfo(View view, Point locationOnScreen, float width, float height) {
            this.view = view;
            this.locationOnScreen = locationOnScreen;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "ShareViewInfo{" + "view=" + view + ", locationOnScreen=" + locationOnScreen + ", width=" + width
                   + ", height=" + height + '}';
        }
    }

    private void fillViewProperty(View origin, View target) {
        if ((origin instanceof TextView) && (target instanceof TextView)) {
            ((TextView) target).setText(((TextView) origin).getText());
            ((TextView) target).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) ((TextView) origin).getTextSize());
            ((TextView) target).setTextColor(((TextView) origin).getCurrentTextColor());
            return;
        }
        if ((origin instanceof ImageView) && (target instanceof ImageView)) {
            ((ImageView) target).setImageDrawable(((ImageView) origin).getDrawable());
        }
    }

    public KShareViewActivityManager withAction(KShareViewActivityAction action) {
        this.kShareViewActivityAction = action;
        return this;
    }

    public KShareViewActivityManager setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public KShareViewActivityManager withIntent(Intent intent) {
        this.mIntent = intent;
        return this;
    }

    public KShareViewActivityManager withIntentAndRequestCode(Intent intent, int requestCode) {
        this.mIntent = intent;
        this.mRequestCode = requestCode;
        return this;
    }

    public interface KShareViewActivityAction {

        void onAnimatorStart();

        void onAnimatorEnd();

        void changeViewProperty(View view);
    }

    private interface AnimationEnd {

        void onEnd();
    }

}
