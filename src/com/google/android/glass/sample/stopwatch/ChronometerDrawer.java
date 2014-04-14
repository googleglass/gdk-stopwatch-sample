/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.stopwatch;

import com.google.android.glass.timeline.DirectRenderingCallback;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * {@link DirectRenderingCallback} used to draw the chronometer on the timeline {@link LiveCard}.
 * Rendering requires that:
 * <ol>
 * <li>a {@link SurfaceHolder} has been created through monitoring the
 *     {@link SurfaceHolder.Callback#onSurfaceCreated(SurfaceHolder)} and
 *     {@link SurfaceHolder.Callback#onSurfaceDestroyed(SurfaceHolder)} callbacks.
 * <li>rendering has not been paused (defaults to rendering) through monitoring the
 *     {@link DirecRenderingCallback#renderingPaused(SurfaceHolder, boolean)} callback.
 * </ol>
 * As this class uses an inflated {@link View} to draw on the {@link SurfaceHolder}'s
 * {@link Canvas}, monitoring the
 * {@link SurfaceHolder.Callback#onSurfaceChanged(SurfaceHolder, int, int, int)} callback is also
 * required to properly measure and layout the {@link View}'s dimension.
 */
public class ChronometerDrawer implements DirectRenderingCallback {

    private static final String TAG = ChronometerDrawer.class.getSimpleName();
    private static final int COUNT_DOWN_VALUE = 3;

    private final CountDownView mCountDownView;
    private final ChronometerView mChronometerView;

    private SurfaceHolder mHolder;
    private boolean mCountDownDone;
    private boolean mRenderingPaused;

    private final CountDownView.Listener mCountDownListener = new CountDownView.Listener() {

        @Override
        public void onTick(long millisUntilFinish) {
            if (mHolder != null) {
                draw(mCountDownView);
            }
        }

        @Override
        public void onFinish() {
            mCountDownDone = true;
            mChronometerView.setBaseMillis(SystemClock.elapsedRealtime());
            updateRenderingState();
        }
    };

    private final ChronometerView.Listener mChronometerListener = new ChronometerView.Listener() {

        @Override
        public void onChange() {
            if (mHolder != null) {
                draw(mChronometerView);
            }
        }
    };

    public ChronometerDrawer(Context context) {
        this(new CountDownView(context), new ChronometerView(context));
    }

    public ChronometerDrawer(CountDownView countDownView, ChronometerView chronometerView) {
        mCountDownView = countDownView;
        mCountDownView.setCountDown(COUNT_DOWN_VALUE);
        mCountDownView.setListener(mCountDownListener);

        mChronometerView = chronometerView;
        mChronometerView.setListener(mChronometerListener);
    }

    /**
     * Uses the provided {@code width} and {@code height} to measure and layout the inflated
     * {@link CountDownView} and {@link ChronometerView}.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Measure and layout the view with the canvas dimensions.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mCountDownView.measure(measuredWidth, measuredHeight);
        mCountDownView.layout(
                0, 0, mCountDownView.getMeasuredWidth(), mCountDownView.getMeasuredHeight());

        mChronometerView.measure(measuredWidth, measuredHeight);
        mChronometerView.layout(
                0, 0, mChronometerView.getMeasuredWidth(), mChronometerView.getMeasuredHeight());
    }

    /**
     * Keeps the created {@link SurfaceHolder} and updates this class' rendering state.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The creation of a new Surface implicitly resumes the rendering.
        mRenderingPaused = false;
        mHolder = holder;
        updateRenderingState();
    }

    /**
     * Removes the {@link SurfaceHolder} used for drawing and stops rendering.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRenderingState();
    }

    /**
     * Updates this class' rendering state according to the provided {@code paused} flag.
     */
    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    /**
     * Starts or stops rendering according to the {@link LiveCard}'s state.
     */
    private void updateRenderingState() {
        if (mHolder != null && !mRenderingPaused) {
            if (mCountDownDone) {
                mChronometerView.start();
            } else {
                mCountDownView.start();
            }
        } else {
            mChronometerView.stop();
        }
    }

    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw(View view) {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            Log.e(TAG, "Unable to lock canvas: " + e);
            return;
        }
        if (canvas != null) {
            view.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
