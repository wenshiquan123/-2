package com.hlzx.wenutil.ui.pulltorefresh;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.hlzx.wenutil.R;
import com.hlzx.wenutil.utils.Logger;

/**
 * Created by alan on 2016/3/17.
 */
public class FlipLodingLayout extends LoadingLayout {

    static final int ROTATION_ANIMATION_DURATION = 150;

    private Animation mRotateAnimation, mResetRotateAnimation;
    private Matrix mHeaderImageMatrix;

    private float mRotationPivotX, mRotationPivotY;

    //private final boolean mRotateDrawableWhilePulling;

    public FlipLodingLayout(Context context) {
        this(context, null);
    }

    public FlipLodingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHeaderImage.setScaleType(ImageView.ScaleType.MATRIX);
        mHeaderImageMatrix = new Matrix();
        mHeaderImage.setImageMatrix(mHeaderImageMatrix);

        mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        mRotateAnimation.setDuration(ROTATION_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(-180f, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        mResetRotateAnimation.setDuration(ROTATION_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);
    }

    @Override
    protected int getDefaultDrawableResId() {
        return R.drawable.default_ptr_flip;
    }

    @Override
    protected void onPullImpl(float scaleOfLayout) {
        float angle;
        //angle = scaleOfLayout * 90f;
        angle = Math.max(0f, Math.min(180f, scaleOfLayout * 360f - 180f));
        mHeaderImageMatrix.setRotate(angle, mRotationPivotX, mRotationPivotY);
        mHeaderImage.setImageMatrix(mHeaderImageMatrix);
    }

    @Override
    protected void onLoadingDrawableSet(Drawable imageDrawable) {
    }

    @Override
    protected void pullToRefreshImpl() {
        if (mRotateAnimation == mHeaderImage.getAnimation()) {
            mHeaderImage.startAnimation(mResetRotateAnimation);
        }

    }

    //正在刷新
    @Override
    protected void refreshingImpl() {
        mHeaderImage.clearAnimation();
        mHeaderProgress.setVisibility(View.VISIBLE);
        mHeaderImage.setVisibility(View.GONE);
    }

    @Override
    protected void releaseToRefreshImpl() {
        if (mHeaderImage.getAnimation() == null || mResetRotateAnimation == mHeaderImage.getAnimation()) {
            mHeaderImage.startAnimation(mRotateAnimation);
        }


    }

    @Override
    protected void resetImpl() {
        mHeaderImage.clearAnimation();
        mHeaderProgress.setVisibility(View.GONE);
        mHeaderImage.setVisibility(View.VISIBLE);
    }
}
