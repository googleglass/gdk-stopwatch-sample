/*
 * Copyright (C) 2013 Google Inc.
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

package com.google.glass.samples.stopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * StopWatch sample's main activity.
 */
public class StopWatchActivity extends Activity {

  private Chronometer mChronometer;
  private TextView mHint;

  private long mBase = 0;
  private boolean mStarted = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_stopwatch);

    mChronometer = (Chronometer) findViewById(R.id.stopwatch);
    mHint = (TextView) findViewById(R.id.phrase_hint);
  }

  /**
   * Handle the tap event from the touchpad.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    // Handle tap events.
    case KeyEvent.KEYCODE_DPAD_CENTER:
    case KeyEvent.KEYCODE_ENTER:
      toggleStopWatch();
      return true;
    default:
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mStarted) {
      mBase = mChronometer.getBase();
      toggleStopWatch();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mStarted) {
      mChronometer.stop();
      mStarted = false;
    }
  }

  @Override
  public void onDestroy() {
    if (mStarted) {
      mChronometer.stop();
      mStarted = false;
    }
    super.onDestroy();
  }

  /**
   * Toggle the StopWatch states.
   */
  private void toggleStopWatch() {
    if (mStarted) {
      mBase = SystemClock.elapsedRealtime();
      mChronometer.stop();
      mHint.setText(R.string.stopwatch_resume);
    } else {
      mChronometer.setBase(mChronometer.getBase() + SystemClock.elapsedRealtime() - mBase);
      mChronometer.start();
      mHint.setText(R.string.stopwatch_pause);
    }
    mStarted = !mStarted;
  }
}
