package com.example.vickybilbily.wyclef_map;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by vickybilbily on 2017-01-21.
 */

public class CanvasView extends View {

    private MotionListener listener;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMotionListener(MotionListener listener){
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);
        if (this.listener != null) {
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    listener.onDown(event);
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    listener.onMove(event);
                    return true;
                case (MotionEvent.ACTION_UP):
                    listener.onUp(event);
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }
        return false;
    }

}
