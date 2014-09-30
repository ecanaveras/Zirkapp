package com.ecp.gsy.dcs.zirkapp.app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by elcapi05 on 20/08/2014.
 */
public class PullDownListView extends ListView implements AbsListView.OnScrollListener {

    private ListViewTouchEventListener mTouchEventListener;
    private boolean pulledDown;

    public PullDownListView(Context context) {
        super(context);
        init();
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnScrollListener(this);
    }


    private float lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastY = ev.getRawY();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float newY = ev.getRawY();
            setPulledDown((newY - lastY) > 0);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPulledDown()) {
                        if (mTouchEventListener != null) {
                            mTouchEventListener.onListViewPulledDown();
                            setPulledDown(false);
                        }
                    }
                }
            }, 500);
            lastY = newY;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            lastY = 0;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        setPulledDown(false);
    }

    public interface ListViewTouchEventListener {
        public void onListViewPulledDown();
    }


    //<editor-fold desc="GETTERS">
    public ListViewTouchEventListener getmTouchEventListener() {
        return mTouchEventListener;
    }

    public boolean isPulledDown() {
        return pulledDown;
    }
    //</editor-fold>

    //<editor-fold desc="SETTERS">
    public void setListViewTouchEventListener(ListViewTouchEventListener mTouchEventListener) {
        this.mTouchEventListener = mTouchEventListener;
    }

    public void setPulledDown(boolean pulledDown) {
        this.pulledDown = pulledDown;
    }
    //</editor-fold>
}
