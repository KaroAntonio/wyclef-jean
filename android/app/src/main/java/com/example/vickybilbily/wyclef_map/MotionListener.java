package com.example.vickybilbily.wyclef_map;

import android.view.MotionEvent;

import java.util.EventObject;

/**
 * Created by vickybilbily on 2017-01-21.
 */

public interface MotionListener {
    void onDown(MotionEvent e);
    void onMove(MotionEvent e);
    void onUp(MotionEvent e);
    // or void onEvent(); as per your need
}
