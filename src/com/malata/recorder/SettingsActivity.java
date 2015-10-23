package com.malata.recorder;

import com.malata.mounted.BaseActivity;
import com.malata.recorder.utils.Preferences;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends BaseActivity implements
		OnSeekBarChangeListener, OnCheckedChangeListener, OnClickListener {

	private final int RECOED_MIN_TIME = 1;
	private final int RECOED_MAX_TIME = 10 - 1; // minute == mSizeBar.max()

	private final int RECOED_MIN_CACHE = 500; // MB
	private final int RECOED_MAX_CACHE = 2000; // MB

	private SeekBar mCacheBar;
	private SeekBar mSizeBar;
	private TextView mCacheText;
	private TextView mSizeText;
	private RadioButton mQualityLow;
	private RadioButton mQualityMid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		initView();
	}

	private void initView() {
		mCacheBar = (SeekBar) findViewById(R.id.cache_seekbar);
		mCacheBar.setOnSeekBarChangeListener(this);

		mSizeBar = (SeekBar) findViewById(R.id.size_seekbar);
		mSizeBar.setMax(RECOED_MAX_TIME);
		mSizeBar.setOnSeekBarChangeListener(this);

		mCacheText = (TextView) findViewById(R.id.cache_text);
		mSizeText = (TextView) findViewById(R.id.size_text);

		mQualityLow = (RadioButton) findViewById(R.id.quality_low);
		mQualityLow.setOnCheckedChangeListener(this);
		mQualityMid = (RadioButton) findViewById(R.id.quality_mid);
		mQualityMid.setOnCheckedChangeListener(this);

		findViewById(R.id.cache_increase).setOnClickListener(this);
		findViewById(R.id.cache_reduce).setOnClickListener(this);
		findViewById(R.id.size_increase).setOnClickListener(this);
		findViewById(R.id.size_reduce).setOnClickListener(this);
		initSettingData();
		intiActionbar();

	}

	private void intiActionbar() {
		// TODO Auto-generated method stub
		findViewById(R.id.settings).setVisibility(View.GONE);
		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mAppManager.finishActivity();
			}
		});
		TextView txt = (TextView) findViewById(R.id.actionbar_name);
		txt.setText(getString(R.string.action_settings));
	}

	private void initSettingData() {
		// TODO Auto-generated method stub
		int cacheSpace = Preferences.getCacheSpace(this);
		mCacheBar.setProgress((cacheSpace/1024/1024 - RECOED_MIN_CACHE)
				/ ((RECOED_MAX_CACHE - RECOED_MIN_CACHE) / 100));

		int recorderTime = Preferences.getRecorderTime(this);
		mSizeBar.setProgress(recorderTime / 60 - 1);
		
		int recorderQuality = Preferences.getRecorderQuality(this);
		
		if(IRecorderActor.RecorderQuality.QUALITY_LOW.ordinal()==recorderQuality){
			mQualityLow.setChecked(true);
		}else{
			mQualityMid.setChecked(true);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar == mCacheBar) {

			mCacheText.setText((RECOED_MAX_CACHE - RECOED_MIN_CACHE) / 100
					* seekBar.getProgress() + RECOED_MIN_CACHE + "");
			
			Preferences.setCacheSpace(this,
					RECOED_MIN_CACHE + (RECOED_MAX_CACHE - RECOED_MIN_CACHE)
							/ 100 * seekBar.getProgress());

		} else if (seekBar == mSizeBar) {

			mSizeText.setText(seekBar.getProgress() + RECOED_MIN_TIME + "");
			
			Preferences.setRecorderTime(this, seekBar.getProgress());
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView == mQualityLow && isChecked) {
			Preferences.setRecorderQuality(this,
					IRecorderActor.RecorderQuality.QUALITY_LOW.ordinal());
		} else if (buttonView == mQualityMid && isChecked) {
			Preferences.setRecorderQuality(this,
					IRecorderActor.RecorderQuality.QUALITY_MID.ordinal());
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cache_increase:
			mCacheBar.setProgress(mCacheBar.getProgress()+1);
			break;
		case R.id.cache_reduce:
			mCacheBar.setProgress(mCacheBar.getProgress()-1);
			break;
		case R.id.size_increase:
			mSizeBar.setProgress(mSizeBar.getProgress()+1);
			break;
		case R.id.size_reduce:
			mSizeBar.setProgress(mSizeBar.getProgress()-1);
			break;

		default:
			break;
		}
	}

}
