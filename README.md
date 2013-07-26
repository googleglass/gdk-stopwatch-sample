apk-stopwatch-sample
====================

Simple APK Glassware acting as a stopwatch.

## Getting started

Checkout our documentation to learn how to get started on https://developers.google.com/glass/gdk

## Running the APK on Glass

You can use your IDE to compile, install, and run the APK or use
[`adb`](https://developer.android.com/tools/help/adb.html)
on the command line:

    $ adb install -r apk-stopwatch-sample.apk
    $ adb shell am start -n com.google.glass.samples.stopwatch/.StopWatchActivity

Note: The Glass screen must be on when you run the APK.
