/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.glass.sample.stopwatch;

import android.content.Context;
import android.test.AndroidTestCase;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link CountDownView}.
 */
public class CountDownViewTest extends AndroidTestCase {

    private final long COUNT_DOWN_SECONDS = 5;

    private long mElapsedRealtime;

    private CountDownView mView;
    private TextView mSecondsView;

    // Test flags.
    private long mPostedCallbackDelayMillis;
    private long mTimeMillis;
    private Runnable mPostedCallback;
    private int mPlayedSoundId;

    private long mMillisLeft;
    private boolean mOnFinishCalled;

    /** Extension of {@link CountDownView} for easier testing. */
    private class MockCountDownView extends CountDownView {

        public MockCountDownView(Context context) {
            super(context);
        }

        @Override
        public boolean postDelayed(Runnable action, long delayMillis) {
            mPostedCallback = action;
            mPostedCallbackDelayMillis = delayMillis;
            return true;
        }

        @Override
        public long getElapsedRealtime() {
            return mElapsedRealtime;
        }

        @Override
        protected void playSound(int soundId) {
            mPlayedSoundId = soundId;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mView = new MockCountDownView(getContext());
        mView.setListener(new CountDownView.Listener() {

            @Override
            public void onTick(long millisLeft) {
                mMillisLeft = millisLeft;
            }

            @Override
            public void onFinish() {
                mOnFinishCalled = true;
            }
        });
        mView.setCountDown(COUNT_DOWN_SECONDS);

        // Retrieve the underlying views.
        mSecondsView = (TextView) mView.findViewById(R.id.seconds);

        // Reset the test flags.
        mPostedCallbackDelayMillis = 0;
        mPostedCallback = null;
        mOnFinishCalled = false;
        mMillisLeft = 0;
    }

    public void testStartPostsRunnable() {
        mView.start();
        assertNotNull(mPostedCallback);
        assertEquals(0, mPostedCallbackDelayMillis);
    }

    public void testStartWhenStartedIsNoOp() {
        mView.start();
        assertNotNull(mPostedCallback);
        assertEquals(0, mPostedCallbackDelayMillis);
        // Reset flags.
        mPostedCallback = null;
        mPostedCallbackDelayMillis = 0;
        mView.start();
        assertNull(mPostedCallback);
        assertEquals(0, mPostedCallbackDelayMillis);
    }

    public void testUpdateViewCallsOnTick() {
        long elapsedTimeMillis = 300;
        long expectedMillisLeft = TimeUnit.SECONDS.toMillis(COUNT_DOWN_SECONDS) - elapsedTimeMillis;

        mView.start();
        mElapsedRealtime += elapsedTimeMillis;
        assertFalse(mView.updateView());
        assertEquals(expectedMillisLeft, mMillisLeft);
    }

    public void testUpdateViewCallsOnFinish() {
        mView.start();
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(COUNT_DOWN_SECONDS);
        assertTrue(mView.updateView());
        assertEquals(0, mMillisLeft);
        assertTrue(mOnFinishCalled);
    }

    public void testUpdateViewProperlyPlaysCountDownSound() {
        mView.start();
        assertFalse(mView.updateView());
        assertEquals(mView.mCountDownSoundId, mPlayedSoundId);
        // Reset flag.
        mPlayedSoundId = -1;
        assertFalse(mView.updateView());
        assertEquals(-1, mPlayedSoundId);
        // Ensure that previous test is not a no-op.
        assertFalse(-1 == mView.mCountDownSoundId);

        mElapsedRealtime += 1500;
        assertFalse(mView.updateView());
        assertEquals(mView.mCountDownSoundId, mPlayedSoundId);
    }

    public void testUpdateViewProperlyPlaysFinishSound() {
        mView.start();
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(COUNT_DOWN_SECONDS);
        assertTrue(mView.updateView());
        assertEquals(mView.mFinishSoundId, mPlayedSoundId);
    }

    public void testUpdateViewProperlyUpdatesSecondsViewWithTranslation() {
        long millisUntilFinish = 2900;
        float factor = 100 / CountDownView.ANIMATION_DURATION_IN_MILLIS;
        float expectedTranslation = CountDownView.MAX_TRANSLATION_Y * (1 - factor);
        float expectedAlpha = factor * CountDownView.ALPHA_DELIMITER;
        String expectedSecondsText = "3";

        mView.updateView(millisUntilFinish);
        assertEquals(expectedTranslation, mSecondsView.getTranslationY());
        assertEquals(expectedAlpha, mSecondsView.getAlpha());
        assertEquals(expectedSecondsText, mSecondsView.getText());
    }

    public void testUpdateViewProperlyUpdatesSecondsViewAlpha() {
        long millisUntilFinish = 2100;
        float factor = (900 - CountDownView.ANIMATION_DURATION_IN_MILLIS)
                / CountDownView.ANIMATION_DURATION_IN_MILLIS;
        float expectedTranslation = mSecondsView.getTranslationY();
        float expectedAlpha =
                CountDownView.ALPHA_DELIMITER + factor * (1 - CountDownView.ALPHA_DELIMITER);
        String expectedSecondsText = "3";

        mView.updateView(millisUntilFinish);
        assertEquals(expectedTranslation, mSecondsView.getTranslationY());
        assertEquals(expectedAlpha, mSecondsView.getAlpha());
        assertEquals(expectedSecondsText, mSecondsView.getText());
    }

}
