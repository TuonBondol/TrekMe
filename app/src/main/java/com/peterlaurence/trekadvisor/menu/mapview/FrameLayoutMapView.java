package com.peterlaurence.trekadvisor.menu.mapview;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.peterlaurence.trekadvisor.R;
import com.peterlaurence.trekadvisor.menu.mapview.components.PositionOrientationMarker;

/**
 * Layout for {@link MapViewFragment}, but can be used with a different {@link View}, as it uses
 * hooks to report events.
 *
 * @author peterLaurence on 19/03/16.
 */
public class FrameLayoutMapView extends FrameLayout implements
        TileViewExtended.SingleTapStaticListener,
        TileViewExtended.ScrollListener {

    private FloatingActionButton mPositionFAB;
    private FloatingActionButton mLockFAB;

    private View mPositionMarker;

    private PositionTouchListener mPositionTouchListener;
    private LockViewListener mLockViewListener;

    private Animation mShowOrientationFAB;
    private Animation mHideOrientationFAB;
    private boolean mIsVisibleOrientationFAB = true;
    private boolean mLockEnabled = false;

    public FrameLayoutMapView(Context context) {
        this(context, null);
    }

    public FrameLayoutMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayoutMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FrameLayoutMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.fragment_map_view, this);
        mPositionFAB = (FloatingActionButton) findViewById(R.id.fab_position);

        mPositionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* How to change the icon color */
                mPositionFAB.getDrawable().mutate().setTint(getResources().getColor(R.color.colorAccent, null));
                if (mPositionTouchListener != null) {
                    mPositionTouchListener.onPositionTouch();
                }
            }
        });

        mLockFAB = (FloatingActionButton) findViewById(R.id.fab_lock);
        mLockFAB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLockViewListener != null) {
                    mLockFAB.getDrawable().mutate().setTint(
                            mLockEnabled ?
                                    getResources().getColor(R.color.colorDarkGrey, null) :
                                    getResources().getColor(R.color.colorAccent, null));
                    mLockEnabled = !mLockEnabled;
                    mLockViewListener.onLockView(mLockEnabled);
                }
            }
        });

        mShowOrientationFAB = AnimationUtils.loadAnimation(getContext(), R.anim.show_orientation_fab);
        mHideOrientationFAB = AnimationUtils.loadAnimation(getContext(), R.anim.hide_orientation_fab);

        /* The first time we show the fragment, we want to hide the orientation FAB */
        post(new Runnable() {
            @Override
            public void run() {
                hideOrientationFAB();
            }
        });

        mPositionMarker = new PositionOrientationMarker(context);
    }

    @Override
    public void onSingleTapStatic() {
        showOrientationFAB();
    }

    @Override
    public void onScroll() {
        hideOrientationFAB();
    }

    private void showOrientationFAB() {
        if (!mIsVisibleOrientationFAB) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mLockFAB.getLayoutParams();
            layoutParams.rightMargin += (int) (mLockFAB.getWidth() * 1.35);
            mLockFAB.setLayoutParams(layoutParams);
            mLockFAB.startAnimation(mShowOrientationFAB);
            mIsVisibleOrientationFAB = true;
        }
    }

    private void hideOrientationFAB() {
        if (mIsVisibleOrientationFAB) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mLockFAB.getLayoutParams();
            layoutParams.rightMargin -= (int) (mLockFAB.getWidth() * 1.35);
            mLockFAB.setLayoutParams(layoutParams);
            mLockFAB.startAnimation(mHideOrientationFAB);
            mIsVisibleOrientationFAB = false;
        }
    }

    public interface PositionTouchListener {
        void onPositionTouch();
    }

    public void setPositionTouchListener(PositionTouchListener listener) {
        mPositionTouchListener = listener;
    }

    public interface LockViewListener {
        void onLockView(boolean lock);
    }

    public void setLockViewListener(LockViewListener listener) {
        mLockViewListener = listener;
    }

    public View getDetachedPositionMarker() {
        try {
            ViewGroup parent = (ViewGroup) mPositionMarker.getParent();
            parent.removeView(mPositionMarker);
        } catch (Exception e) {
            // don't care
        }
        return mPositionMarker;
    }
}