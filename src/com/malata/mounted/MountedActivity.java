package com.malata.mounted;

import com.malata.recorder.R;
import com.malata.recorder.RecorderActivity;
import com.malata.recorder.widget.TimeClockView;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MountedActivity extends BaseActivity implements
		View.OnClickListener {
	
	private ImageView mCall;
	private ImageView mDriveRecorder;
	private ImageView mMusic;
	private ImageView mNavigation;
	private ImageView mRecorderSign;
	private TimeClockView  mTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mounted);
		Settings.System.putInt(getContentResolver(), "drive_mode", 1);
		initUI();
	}

	private void initUI() {

		mCall = (ImageView) findViewById(R.id.img_call);
		mCall.setOnClickListener(this);
		mDriveRecorder = (ImageView) findViewById(R.id.img_drive_recorder);
		mDriveRecorder.setOnClickListener(this);
		mMusic = (ImageView) findViewById(R.id.img_music);
		mMusic.setOnClickListener(this);
		mNavigation = (ImageView) findViewById(R.id.img_navigation);
		mNavigation.setOnClickListener(this);
		mRecorderSign = (ImageView) findViewById(R.id.img_recorder_sign);
		
		findViewById(R.id.exit).setOnClickListener(this);
		
		// set show time fotmat
		mTime=(TimeClockView) findViewById(R.id.time);
		mTime.bringToFront();
		mTime.setFormat24Hour(getString(R.string.time_24_hours_format));
		mTime.setFormat12Hour(getString(R.string.time_12_hours_format));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.img_call:
			Toast.makeText(this, "未开发", Toast.LENGTH_SHORT).show();
			break;
		case R.id.img_drive_recorder:
			Intent intent=new Intent();
			intent.setClass(this, RecorderActivity.class);
			startActivity(intent);
			break;
		case R.id.img_music:
			Toast.makeText(this, "未开发", Toast.LENGTH_SHORT).show();
			break;
		case R.id.img_navigation:
			Toast.makeText(this, "未开发", Toast.LENGTH_SHORT).show();
			break;
		case R.id.exit:
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.dialog_message_exit));
			builder.setTitle(getString(R.string.dialog_title_hit));
			builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Settings.System.putInt(getContentResolver(), "drive_mode", 0);
					mAppManager.finishActivity();
				}
			});
            builder.setNegativeButton(getString(android.R.string.cancel),null); 	
			AlertDialog dialog = builder.create();
			dialog.show();
		default:
			break;
		}
	}
	
}
