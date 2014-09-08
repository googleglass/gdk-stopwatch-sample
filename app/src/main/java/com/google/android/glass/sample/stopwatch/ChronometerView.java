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

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * View used to display draw a running Chronometer.
 *
 * This code is greatly inspired by the Android's Chronometer widget.
 */
public class ChronometerView extends FrameLayout {

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface Listener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    /** About 24 FPS, visible for testing. */
    static final long DELAY_MILLIS = 41;

    private final TextView mMinutesView;
    private final TextView mSecondsView;
    private final TextView mCentiSecondsView;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTextRunnable = new Runnable() {

        @Override
        public void run() {
            if (mRunning) {
                updateText();
                postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    private boolean mStarted;
    private boolean mForceStart;
    private boolean mVisible;
    private boolean mRunning;

    private long mBaseMillis;

    private Listener mChangeListener;

    public ChronometerView(Context context) {
        this(context, null, 0);
    }

    public ChronometerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChronometerView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_chronometer, this);

        mMinutesView = (TextView) findViewById(R.id.minute);
        mSecondsView = (TextView) findViewById(R.id.second);
        mCentiSecondsView = (TextView) findViewById(R.id.centi_second);

        setBaseMillis(getElapsedRealtime());
    }

    /**
     * Sets the base value of the chronometer in milliseconds.
     */
    public void setBaseMillis(long baseMillis) {
        mBaseMillis = baseMillis;
        updateText();
    }

    /**
     * Gets the base value of the chronometer in milliseconds.
     */
    public long getBaseMillis() {
        return mBaseMillis;
    }

    /**
     * Sets a {@link Listener}.
     */
    public void setListener(Listener listener) {
        mChangeListener = listener;
    }

    /**
     * Returns the set {@link Listener}.
     */
    public Listener getListener() {
        return mChangeListener;
    }

    /**
     * Starts the chronometer.
     */
    public void start() {
        if (!mRunning) {
            postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
        }
        mRunning = true;
    }

    /**
     * Stops the chronometer.
     */
    public void stop() {
        if (mRunning) {
            removeCallbacks(mUpdateTextRunnable);
        }
        mRunning = false;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        mHandler.removeCallbacks(action);
        return true;
    }

    /**
     * Returns {@link SystemClock.elapsedRealtime}, overridable for testing.
     */
    protected long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Updates the value of the chronometer, visible for testing.
     */
    void updateText() {
        long millis = getElapsedRealtime() - mBaseMillis;
        // Cap chronometer to one hour.
        millis %= TimeUnit.HOURS.toMillis(1);

        mMinutesView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millis)));
        millis %= TimeUnit.MINUTES.toMillis(1);
        mSecondsView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millis)));
        millis = (millis % TimeUnit.SECONDS.toMillis(1)) / 10;
        mCentiSecondsView.setText(String.format("%02d", millis));
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }
}
