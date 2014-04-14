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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.test.AndroidTestCase;
import android.view.SurfaceHolder;
import android.view.Surface;

/**
 * Unit tests for {@link ChronometerDrawer}.
 */
public class ChronometerDrawerTest extends AndroidTestCase {

    private ChronometerDrawer mDrawer;
    private CountDownView mCountDownView;
    private ChronometerView mChronometerView;

    private int mCanvasLockedCount;
    private int mCanvasUnlockedCount;

    private int mCountDownDrawCount;
    private int mChronometerDrawCount;

    private boolean mCountDownStarted;
    private boolean mChronometerStarted;
    private boolean mChronometerStopped;

    /** Simple {@link SurfaceHolder} implementation for testing. */
    private final SurfaceHolder mHolder = new SurfaceHolder() {
        Canvas mCanvas = new Canvas();

        @Override
        public void addCallback(SurfaceHolder.Callback callback) {
            // Nothing to do here.
        }

        @Override
        public Surface getSurface() {
            return null;
        }

        @Override
        public Rect getSurfaceFrame() {
            return null;
        }

        @Override
        public boolean isCreating() {
            return false;
        }

        @Override
        public Canvas lockCanvas() {
            ++mCanvasLockedCount;
            return mCanvas;
        }

        @Override
        public Canvas lockCanvas(Rect rect) {
            return lockCanvas();
        }

        @Override
        public void removeCallback(SurfaceHolder.Callback callback) {
            // Nothing to do here.
        }

        @Override
        public void setFixedSize(int width, int height) {
            // Nothing to do here.
        }

        @Override
        public void setFormat(int format) {
            // Nothing to do here.
        }

        @Override
        public void setKeepScreenOn(boolean keepScreenOn) {
            // Nothing to do here.
        }

        @Override
        public void setSizeFromLayout() {
            // Nothing to do here.
        }

        @Override
        public void setType(int type) {
            // Nothing to do here.
        }

        @Override
        public void unlockCanvasAndPost(Canvas canvas) {
            assertEquals(mCanvas, canvas);
            ++mCanvasUnlockedCount;
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCountDownView = new CountDownView(getContext()) {

            @Override
            public void draw(Canvas canvas) {
                ++mCountDownDrawCount;
            }

            @Override
            public void start() {
                mCountDownStarted = true;
            }
        };
        mChronometerView = new ChronometerView(getContext()) {

            @Override
            public void draw(Canvas canvas) {
                ++mChronometerDrawCount;
            }

            @Override
            public void start() {
                mChronometerStarted = true;
            }

            @Override
            public void stop() {
                mChronometerStopped = true;
            }
        };
        mDrawer = new ChronometerDrawer(mCountDownView, mChronometerView);

        mCanvasLockedCount = 0;
        mCanvasUnlockedCount = 0;
        mCountDownDrawCount = 0;
        mChronometerDrawCount = 0;

        mCountDownStarted = false;
        mChronometerStarted = false;
        mChronometerStopped = false;
    }

    public void testConstructorSetsListener() {
        assertNotNull(mChronometerView.getListener());
        assertNotNull(mCountDownView.getListener());
    }

    public void testSurfaceChanged() {
        int width = 640;
        int height = 360;

        // Ensure the test is not a no-op.
        assertEquals(0, mCountDownView.getWidth());
        assertEquals(0, mCountDownView.getHeight());
        assertEquals(0, mChronometerView.getWidth());
        assertEquals(0, mChronometerView.getHeight());

        mDrawer.surfaceChanged(mHolder, 0, width, height);
        assertEquals(width, mCountDownView.getWidth());
        assertEquals(height, mCountDownView.getHeight());
        assertEquals(width, mChronometerView.getWidth());
        assertEquals(height, mChronometerView.getHeight());
    }

    public void testSurfaceCreatedStartsCountDown() {
        mDrawer.surfaceCreated(mHolder);
        assertTrue(mCountDownStarted);
        assertFalse(mChronometerStarted);
    }

    public void testSurfaceCreatedRenderingPaused() {
        mDrawer.renderingPaused(mHolder, true);
        assertFalse(mCountDownStarted);
        assertFalse(mChronometerStarted);
        assertTrue(mChronometerStopped);
        // Reset flag.
        mChronometerStopped = false;
        mDrawer.surfaceCreated(mHolder);
        assertTrue(mCountDownStarted);
        assertFalse(mChronometerStarted);
        assertFalse(mChronometerStopped);
    }

    public void testSurfaceDestroyedStopsChronometer() {
        mDrawer.surfaceDestroyed(mHolder);
        assertTrue(mChronometerStopped);
    }

    public void testRenderingPausedFalseNoSurface() {
        mDrawer.renderingPaused(mHolder, false);
        assertTrue(mChronometerStopped);
    }

    public void testRenderingPausedFalseWithSurface() {
        mDrawer.surfaceCreated(mHolder);
        assertTrue(mCountDownStarted);
        // Reset flag.
        mCountDownStarted = false;
        mDrawer.renderingPaused(mHolder, false);
        assertTrue(mCountDownStarted);
    }

    public void testRenderingPausedTrue() {
        mDrawer.surfaceCreated(mHolder);
        assertTrue(mCountDownStarted);
        mDrawer.renderingPaused(mHolder, true);
        assertTrue(mChronometerStopped);
    }

    public void testDrawProperlyLocksAndUnlocksCanvas() {
        mDrawer.surfaceCreated(mHolder);
        mCountDownView.getListener().onTick(3000);
        assertEquals(1, mCountDownDrawCount);
        assertEquals(1, mCanvasLockedCount);
        assertEquals(1, mCanvasUnlockedCount);
    }

    public void testListenerNoSurfaceDoesNotCallDraw() {
        mCountDownView.getListener().onTick(3000);
        assertEquals(0, mCountDownDrawCount);
        mChronometerView.getListener().onChange();
        assertEquals(0, mChronometerDrawCount);
    }

    public void testListenerWithSurfaceCallsDraw() {
        mDrawer.surfaceCreated(mHolder);
        mCountDownView.getListener().onTick(3000);
        assertEquals(1, mCountDownDrawCount);
        mChronometerView.getListener().onChange();
        assertEquals(1, mChronometerDrawCount);
    }

    public void testOnFinishListenerStartsChronometer() {
        mDrawer.surfaceCreated(mHolder);
        assertTrue(mCountDownStarted);
        assertFalse(mChronometerStarted);

        mCountDownView.getListener().onFinish();
        assertTrue(mChronometerStarted);

        // Reset flags.
        mChronometerStarted = false;
        mCountDownStarted = false;

        // Pause rendering.
        mDrawer.renderingPaused(mHolder, true);
        assertTrue(mChronometerStopped);
        assertFalse(mChronometerStarted);
        assertFalse(mCountDownStarted);

        // Reset flags.
        mChronometerStarted = false;
        mChronometerStopped = false;
        mCountDownStarted = false;

        // Resume rendering.
        mDrawer.renderingPaused(mHolder, false);
        assertTrue(mChronometerStarted);
        assertFalse(mChronometerStopped);
        assertFalse(mCountDownStarted);
    }
}
