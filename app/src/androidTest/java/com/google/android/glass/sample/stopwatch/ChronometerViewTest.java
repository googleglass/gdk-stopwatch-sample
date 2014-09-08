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
 * Unit tests for {@link ChronometerView}.
 */
public class ChronometerViewTest extends AndroidTestCase {

    private final long COUNT_DOWN_SECONDS = 5;

    private long mElapsedRealtime;

    private ChronometerView mView;
    private TextView mMinutesView;
    private TextView mSecondsView;
    private TextView mCentiSecondsView;

    // Test flags.
    private boolean mMockUpdateView;
    private boolean mMockUpdateTextWithArgs;
    private boolean mOnChangeCalled;
    private boolean mPlaySoundCalled;
    private boolean mUpdateTextCalled;
    private boolean mUpdateTextWithArgsCalled;
    private int mTextColor;
    private long mPostedCallbackDelayMillis;
    private long mTimeMillis;
    private Runnable mPostedCallback;
    private Runnable mRemovedCallback;

    private long mMillisLeft;
    private boolean mOnFinishCalled;


    /** Extension of {@link ChronometerView} for easier testing. */
    private class MockChronometerView extends ChronometerView {

        public MockChronometerView(Context context) {
            super(context);
        }

        @Override
        public boolean postDelayed(Runnable action, long delayMillis) {
            mPostedCallback = action;
            mPostedCallbackDelayMillis = delayMillis;
            return true;
        }

        @Override
        public boolean removeCallbacks(Runnable action) {
            mRemovedCallback = action;
            return true;
        }

        @Override
        public long getElapsedRealtime() {
            return mElapsedRealtime;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mView = new MockChronometerView(getContext());
        mView.setListener(new ChronometerView.Listener() {

            @Override
            public void onChange() {
                mOnChangeCalled = true;
            }
        });

        // Retrieve the underlying views.
        mMinutesView = (TextView) mView.findViewById(R.id.minute);
        mSecondsView = (TextView) mView.findViewById(R.id.second);
        mCentiSecondsView = (TextView) mView.findViewById(R.id.centi_second);

        // Reset the test flags.
        mPostedCallbackDelayMillis = 0;
        mPostedCallback = null;
        mRemovedCallback = null;
        mOnChangeCalled = false;
    }

    public void testSetBaseMillisProperlyUpdatesText() {
        mView.setBaseMillis(mElapsedRealtime);
        assertTrue(mOnChangeCalled);
        assertEquals("00", mMinutesView.getText());
        assertEquals("00", mSecondsView.getText());
        assertEquals("00", mCentiSecondsView.getText());
    }

    public void testStartPostsRunnable() {
        mView.start();
        assertNotNull(mPostedCallback);
        assertEquals(ChronometerView.DELAY_MILLIS, mPostedCallbackDelayMillis);
    }

    public void testStartWhenStartedIsNoOp() {
        mView.start();
        assertNotNull(mPostedCallback);
        assertEquals(ChronometerView.DELAY_MILLIS, mPostedCallbackDelayMillis);
        // Reset flags.
        mPostedCallback = null;
        mPostedCallbackDelayMillis = 0;
        mView.start();
        assertNull(mPostedCallback);
        assertEquals(0, mPostedCallbackDelayMillis);
    }

    public void testStopRemovesRunnable() {
        mView.start();
        mView.stop();
        assertNotNull(mRemovedCallback);
    }

    public void testStopWhenStoppedIsNoOp() {
        mView.stop();
        assertNull(mRemovedCallback);
    }

    public void testUpdateTextProperlyUpdatesText() {
        long expectedMinutes = 3;
        long expectedSeconds = 45;
        long expectedCentiSeconds = 89;
        long elapsedTimeMillis = TimeUnit.MINUTES.toMillis(expectedMinutes)
                + TimeUnit.SECONDS.toMillis(expectedSeconds)
                + expectedCentiSeconds * 10;

        mElapsedRealtime += elapsedTimeMillis;
        mView.updateText();
        assertTrue(mOnChangeCalled);
        assertEquals("03", mMinutesView.getText());
        assertEquals("45", mSecondsView.getText());
        assertEquals("89", mCentiSecondsView.getText());
    }

}
