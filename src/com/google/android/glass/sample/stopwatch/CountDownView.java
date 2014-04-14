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
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Animated countdown going from {@code mTimeSeconds} to 0.
 *
 * The current animation for each second is as follow:
 *   1. From 0 to 500ms, move the TextView from {@code MAX_TRANSLATION_Y} to 0 and its alpha from
 *      {@code 0} to {@code ALPHA_DELIMITER}.
 *   2. From 500ms to 1000ms, update the TextView's alpha from {@code ALPHA_DELIMITER} to {@code 1}.
 * At each second change, update the TextView text.
 */
public class CountDownView extends FrameLayout {

    /**
     * Interface to listen for changes in the countdown.
     */
    public interface Listener {
        /**
         * Notified of a tick, indicating a layout change.
         */
        public void onTick(long millisUntilFinish);

        /**
         * Notified when the countdown is finished.
         */
        public void onFinish();
    }

    /** Time delimiter specifying when the second component is fully shown. */
    public static final float ANIMATION_DURATION_IN_MILLIS = 850.0f;
    private static final long DELAY_MILLIS = 40;

    private static final int SOUND_PRIORITY = 1;
    private static final int MAX_STREAMS = 1;

    // Constants visible for testing.
    static final int MAX_TRANSLATION_Y = 30;
    static final float ALPHA_DELIMITER = 0.95f;
    static final long SEC_TO_MILLIS = TimeUnit.SECONDS.toMillis(1);

    // Sounds ID visible for testing.
    final int mFinishSoundId;
    final int mCountDownSoundId;

    private final TextView mSecondsView;

    private final SoundPool mSoundPool;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateViewRunnable = new Runnable() {

        @Override
        public void run() {
            if (!updateView()) {
                postDelayed(mUpdateViewRunnable, DELAY_MILLIS);
            }
        }
    };

    private long mTimeSeconds;
    private long mCurrentTimeSeconds;
    private long mStopTimeInFuture;
    private Listener mListener;
    private boolean mStarted;

    public CountDownView(Context context) {
        this(context, null, 0);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_countdown, this);
        mSecondsView = (TextView) findViewById(R.id.seconds);

        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        mFinishSoundId = mSoundPool.load(context, R.raw.start, SOUND_PRIORITY);
        mCountDownSoundId = mSoundPool.load(context, R.raw.countdown_bip, SOUND_PRIORITY);
    }

    public void setCountDown(long timeSeconds) {
        mTimeSeconds = timeSeconds;
    }

    public long getCountDown() {
        return mTimeSeconds;
    }

    /**
     * Sets a {@link Listener}.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Returns the set {@link Listener}.
     */
    public Listener getListener() {
        return mListener;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    /**
     * Starts the countdown animation if not yet started.
     */
    public void start() {
        if (!mStarted) {
            mCurrentTimeSeconds = 0;
            mStopTimeInFuture = TimeUnit.SECONDS.toMillis(mTimeSeconds) + getElapsedRealtime();
            mStarted = true;
            postDelayed(mUpdateViewRunnable, 0);
        }
    }

    /**
     * Returns {@link SystemClock.elapsedRealtime}, overridable for testing.
     */
    protected long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Plays the provided {@code soundId}, overridable for testing.
     */
    protected void playSound(int soundId) {
        mSoundPool.play(soundId,
                        1 /* leftVolume */,
                        1 /* rightVolume */,
                        SOUND_PRIORITY,
                        0 /* loop */,
                        1 /* rate */);
    }

    /**
     * Updates the view to reflect the current state of animation, visible for testing.
     *
     * @return whether or not the count down is finished.
     */
    boolean updateView() {
        long millisLeft = mStopTimeInFuture - getElapsedRealtime();
        long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft);
        boolean countDownDone = millisLeft <= 0;

        if (countDownDone) {
            mStarted = false;
            if (mListener != null) {
                mListener.onFinish();
            }
            playSound(mFinishSoundId);
        } else {
            updateView(millisLeft);
            if (mListener != null) {
                mListener.onTick(millisLeft);
            }
            if (mCurrentTimeSeconds != currentTimeSeconds) {
                playSound(mCountDownSoundId);
                mCurrentTimeSeconds = currentTimeSeconds;
            }
        }
        return countDownDone;
    }

    /**
     * Updates the view to reflect the current state of animation, visible for testing.
     *
     * @params millisUntilFinish milliseconds until the countdown is done
     */
    void updateView(long millisUntilFinish) {
        long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinish) + 1;
        long frame = SEC_TO_MILLIS - (millisUntilFinish % SEC_TO_MILLIS);

        mSecondsView.setText(Long.toString(currentTimeSeconds));
        if (frame <= ANIMATION_DURATION_IN_MILLIS) {
            float factor = frame / ANIMATION_DURATION_IN_MILLIS;
            mSecondsView.setAlpha(factor * ALPHA_DELIMITER);
            mSecondsView.setTranslationY(MAX_TRANSLATION_Y * (1 - factor));
        } else {
            float factor = (frame - ANIMATION_DURATION_IN_MILLIS) / ANIMATION_DURATION_IN_MILLIS;
            mSecondsView.setAlpha(ALPHA_DELIMITER + factor * (1 - ALPHA_DELIMITER));
        }
    }
}
