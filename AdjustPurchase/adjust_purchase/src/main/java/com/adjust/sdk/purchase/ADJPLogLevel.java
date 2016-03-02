package com.adjust.sdk.purchase;

import android.util.Log;

/**
 * Created by uerceg on 04/12/15.
 */
public enum ADJPLogLevel {
    VERBOSE(Log.VERBOSE), DEBUG(Log.DEBUG), INFO(Log.INFO), WARN(Log.WARN),
    ERROR(Log.ERROR), ASSERT(Log.ASSERT), NONE(Log.ASSERT + 1);

    private final int androidLogLevel;

    ADJPLogLevel(final int androidLogLevel) {
        this.androidLogLevel = androidLogLevel;
    }

    public int getAndroidLogLevel() {
        return androidLogLevel;
    }
}
