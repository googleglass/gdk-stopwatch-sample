/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.content.ContextWrapper;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * Unit tests for {@link MenuActivity}.
 */
public class MenuActivityTest extends ActivityUnitTestCase<MockMenuActivity> {

    private boolean mServiceStopped;
    private Intent mActivityIntent;

    public MenuActivityTest() {
        super(MockMenuActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set a mock context to simulate service binding.
        setActivityContext(new ContextWrapper(getInstrumentation().getTargetContext()) {

            @Override
            public boolean stopService(Intent intent) {
                assertEquals(
                        StopwatchService.class.getName(), intent.getComponent().getClassName());
                mServiceStopped = true;
                return true;
            }
        });

        mActivityIntent = new Intent(getInstrumentation().getTargetContext(), MenuActivity.class);
    }

    public void testOptionsMenuStop() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        boolean menuHandled = getInstrumentation().invokeMenuActionSync(activity, R.id.stop, 0);

        assertTrue(menuHandled);
        assertTrue(mServiceStopped);
    }

    public void testOptionsMenuUnknown() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        boolean menuHandled = getInstrumentation().invokeMenuActionSync(activity, R.id.stop + 1, 0);

        assertFalse(menuHandled);
        assertFalse(mServiceStopped);
    }

    public void testOptionsMenuClosedFinishesActivity() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);

        activity.onOptionsMenuClosed(null);
        assertTrue(isFinishCalled());
    }
}
