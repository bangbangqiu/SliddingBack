package com.vcredit.sliddingback;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by qiubangbang on 2017/3/9.
 * 侧滑返回的view
 * 使用 ： 1：嵌套在activity布局的最外层
 * -------2：清单文件中activity 设置TransparentStyle的样式
 * -------3：setBackActivityListenner 设置监听
 * ------------onSliddingOver(boolean canBack) canBack为true时可以销毁
 */

public class SliddingBackLayout extends FrameLayout {
    public boolean isOpen = true; //是否开启滑动
    private static final String TAG = "SliddingBackLayout";
    private int sliddingLength = 30;
    private Point downPoint = new Point(1, 1);
    private Point currentPoint = new Point(0, 0);
    private int widthPixels;
    private int heightPixels;
    private boolean isInterupt = false;
    private BackActivityListenner backActivityListenner;
    private int duration = 260;
    private long speed; //检测速度，快速滑动 px/s
    private long startTime;
    private Paint fillPaint;
    private RectF shaderRectF;
    private int shaderWidth = 40;//阴影的宽度

    public SliddingBackLayout(Context context) {
        this(context, null);
    }

    public SliddingBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliddingBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        heightPixels = getResources().getDisplayMetrics().heightPixels;
        Log.d(TAG, "SliddingBackLayout: widthPixels: " + widthPixels);
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setShader(new LinearGradient(-shaderWidth, 0, 0, 0, Color.argb(0x00, 0xff, 0xff, 0xff),
                Color.argb(0x66, 0x33, 0x33, 0x33), Shader.TileMode.CLAMP));
        shaderRectF = new RectF(dp2px(-shaderWidth), 0, 0, heightPixels);
        setWillNotDraw(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果点击距离屏幕左边界10dp，触发返回销毁activity
        float x = ev.getX();
        Log.d(TAG, "onInterceptTouchEvent: x:" + ev.getX());
        if (!isOpen) return false;
        if (x < dp2px(sliddingLength)) {
            //注意： 即使拦截 ACTION_DOWN 也会执行一次
            isInterupt = true;
        } else {
            isInterupt = false;
        }
        return isInterupt;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: x:" + event.getX() + "y: " + event.getY());
        Log.d(TAG, "onTouchEvent: downX:" + downPoint.x + "downY: " + downPoint.y);
        Log.d(TAG, "onTouchEvent: currentX:" + currentPoint.x + "currentY: " + currentPoint.y);
        //当 isInterrupt==false 或者不在拦截区域 进行拦截
        if (!isInterupt || downPoint.x > dp2px(sliddingLength)) {
            Log.d(TAG, "onTouchEvent: 拦截==================");
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                currentPoint.x = downPoint.x = (int) event.getX();
                currentPoint.y = downPoint.y = (int) event.getY();
                startTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                //滚动操作
                speed = (int) ((event.getX() - currentPoint.x) / (System.currentTimeMillis() - startTime) * 1000);
                Log.d(TAG, "onTouchEvent: speed: " + speed);
                scrollTo(downPoint.x - currentPoint.x, 0);
                currentPoint.x = (int) event.getX();
                currentPoint.y = (int) event.getY();
                startTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent: ACTION_UP_CANCEL");
                int x = (int) event.getX();
                //如果速度达到了,并且是正值，负数代表反方向
                if (speed > 1000) {
                    finish();
                    return true;
                }

                if (x < 0.3 * widthPixels || speed < -1000) {
//                    Toast.makeText(getContext(), "距离不够activity不销毁", Toast.LENGTH_SHORT).show();
                    backActivityListenner.onSliddingOver(false);
                    backAnimation(currentPoint.x, 0);
                } else {
//                    Toast.makeText(getContext(), "距离达到activity销毁", Toast.LENGTH_SHORT).show();
                    finish();
                }
                //初始化
                downPoint.set(1, 0);
                currentPoint.set(0, 0);
                break;
        }
        return true;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制阴影
        Log.d(TAG, "onDraw: width " + getWidth());
        canvas.drawRect(shaderRectF, fillPaint);
        super.onDraw(canvas);
    }

    private void finish() {
        //关闭动画
        backAnimation(currentPoint.x, widthPixels);
        //动画执行完成后通知finish
        postDelayed(new Runnable() {
            @Override
            public void run() {
                backActivityListenner.onSliddingOver(true);
            }
        }, duration - 20);
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public void setBackActivityListenner(BackActivityListenner backActivityListenner) {
        this.backActivityListenner = backActivityListenner;
    }

    public interface BackActivityListenner {
        void onSliddingOver(boolean canBack);
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    //回复动画
    public void backAnimation(final int xStart, final int xEnd) {
        final ValueAnimator animator = ValueAnimator.ofInt(xStart, xEnd);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                scrollTo(-animatedValue, 0);
                Log.d(TAG, "onAnimationUpdate: " + animatedValue);
                if (animatedValue == xEnd) {
                    animator.removeUpdateListener(this);
                }
            }
        });
        animator.setDuration(duration);
        animator.start();
    }
}

