package com.malata.recorder.widget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;


import java.util.Calendar;
import java.util.TimeZone;

public class TimeClockView extends TextView {
    public static final CharSequence DEFAULT_FORMAT_12_HOUR = "yyyy-MM-dd a h:mm:ss";

    public static final CharSequence DEFAULT_FORMAT_24_HOUR = "yyyy-MM-dd HH:mm:ss";

    private CharSequence mFormat12=DEFAULT_FORMAT_12_HOUR;
    private CharSequence mFormat24=DEFAULT_FORMAT_24_HOUR;

    private CharSequence mFormat;
    private boolean mHasSeconds;

    private boolean mAttached;

    private Calendar mTime;
    private String mTimeZone;

    private final ContentObserver mFormatChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            chooseFormat();
            onTimeChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            chooseFormat();
            onTimeChanged();
        }
    };

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra("time-zone");
                createTime(timeZone);
            }
            onTimeChanged();
        }
    };

    private final Runnable mTicker = new Runnable() {
        public void run() {
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            getHandler().postAtTime(mTicker, next);
        }
    };

    public TimeClockView(Context context) {
        super(context);
        init();
    }
    public TimeClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimeClockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        createTime(mTimeZone);
        // Wait until onAttachedToWindow() to handle the ticker
        chooseFormat(false);
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            mTime = Calendar.getInstance();
        }
    }
    
    public CharSequence getFormat12Hour() {
        return mFormat12;
    }

    public void setFormat12Hour(CharSequence format) {
        mFormat12 = format;

        chooseFormat();
        onTimeChanged();
    }

    public CharSequence getFormat24Hour() {
        return mFormat24;
    }

    public void setFormat24Hour(CharSequence format) {
        mFormat24 = format;

        chooseFormat();
        onTimeChanged();
    }

    public boolean is24HourModeEnabled() {
          return DateFormat.is24HourFormat(getContext());
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;

        createTime(timeZone);
        onTimeChanged();
    }

    private void chooseFormat() {
        chooseFormat(true);
    }

    public CharSequence getFormat() {
        return mFormat;
    }

    private void chooseFormat(boolean handleTicker) {
        final boolean format24Requested = is24HourModeEnabled();


        if (format24Requested) {
            mFormat = mFormat24;
        } else {
            mFormat = mFormat12;
        }

        boolean hadSeconds = mHasSeconds;
        mHasSeconds = hasSeconds(mFormat);

        if (handleTicker && mAttached && hadSeconds != mHasSeconds) {
            if (hadSeconds) getHandler().removeCallbacks(mTicker);
            else mTicker.run();
        }
    }

    private boolean hasSeconds(CharSequence format) {
		return ((String) format).contains("ss");
	}
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;

            registerReceiver();
            registerObserver();

            createTime(mTimeZone);

            if (mHasSeconds) {
                mTicker.run();
            } else {
                onTimeChanged();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            unregisterReceiver();
            unregisterObserver();

            getHandler().removeCallbacks(mTicker);

            mAttached = false;
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
    }

    private void registerObserver() {
        final ContentResolver resolver = getContext().getContentResolver();
            resolver.registerContentObserver(Settings.System.CONTENT_URI, true,
                    mFormatChangeObserver);
    }

    private void unregisterReceiver() {
    	try{
    		getContext().unregisterReceiver(mIntentReceiver);
    	}catch(Exception e){
    		
    	}
    }

    private void unregisterObserver() {
        final ContentResolver resolver = getContext().getContentResolver();
        resolver.unregisterContentObserver(mFormatChangeObserver);
    }

    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(mFormat, mTime));
    }
}
