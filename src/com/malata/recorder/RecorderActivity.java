package com.malata.recorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.malata.mounted.BaseActivity;
import com.malata.mounted.MountedActivity;
import com.malata.recorder.IRecorderActor.RecorderState;
import com.malata.recorder.R;
import com.malata.recorder.widget.RecorderAppUI;

public class RecorderActivity extends BaseActivity{

	private WindowManager wm;
	private IRecorderAppUI mRecorderUI;
	private LayoutParams params;

	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;
	public static final int OPERATION_VISIBLE = 102;
	public static final int OPERATION_GONE = 103;
	public static final int OPERATION_WRAN = 104;
	public static final int OPERATION_FINISH = 105;
	
	private View mWindowLayout;
	private boolean mAttached;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		createFloatView();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if(mRecorderUI.getRecorderState()!=RecorderState.STATE_RECORDING){
			    mRecorderUI.initCamera();
			}
			mHandler.sendEmptyMessage(OPERATION_VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mRecorderUI.getRecorderState()!=RecorderState.STATE_RECORDING){
		    mRecorderUI.stop();
		}
		mHandler.sendEmptyMessage(OPERATION_GONE);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Window window = this.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = 1;
		window.setAttributes(lp);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.sendEmptyMessage(OPERATION_HIDE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(this, MountedActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Point getPoint() {
		Point p = new Point();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		p.x = metrics.widthPixels;
		p.y = metrics.heightPixels;
		return p;
	}

	private void createFloatView() {
		
		wm = (WindowManager) getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);
		WindowParams(true);
		mRecorderUI = new RecorderAppUI(this, mHandler);
		mWindowLayout = mRecorderUI.getView();
		wm.addView(mWindowLayout, params);
		mAttached = true;
	}

	private void WindowParams(boolean visible) {
		params = new WindowManager.LayoutParams();
		// window type
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		/*
		 * params.type = WindowManager.LayoutParams.TYPE_PHONE; 
		 * 
		 */

		params.format = PixelFormat.RGBA_8888; 

		// Window flag
		params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_FULLSCREEN;
		;
		// params.flags &=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		/*
		 * 
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */
		
		if (visible) {
			Point point = getPoint();
			params.width = point.x;
			params.height = point.y;
		} else {
			params.width = 1;
			params.height = 1;
		}
		params.x = 0;
		params.y = 0;
	}
	
	private void showWranDialog(String message){
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(getString(R.string.dialog_title_wran));
		builder.setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mAppManager.finishActivity();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	/**
	 * 
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OPERATION_SHOW:
				if (!mAttached) {
					WindowParams(true);
					wm.addView(mWindowLayout, params);
					mAttached = true;
				}
				break;
			case OPERATION_HIDE:
				if (mAttached) {
					wm.removeView(mWindowLayout);
					mAttached = false;
				}
				break;
			case OPERATION_VISIBLE:
				if (mAttached) {
					WindowParams(true);
					wm.updateViewLayout(mWindowLayout, params);
				}
				break;
			case OPERATION_GONE:
				if (mAttached) {
					WindowParams(false);
					wm.updateViewLayout(mWindowLayout, params);
				}
				break;
			case OPERATION_WRAN:
				showWranDialog((String)msg.obj);
				break;
			case OPERATION_FINISH:
				mAppManager.finishActivity();
				break;
			}
		}
	};
}
