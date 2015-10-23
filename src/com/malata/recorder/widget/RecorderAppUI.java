package com.malata.recorder.widget;

import com.malata.recorder.IRecorderActor.RecorderState;
import com.malata.recorder.utils.XLog;
import com.malata.recorder.FileManageActivity;
import com.malata.recorder.IRecorderAppUI;
import com.malata.recorder.RecorderActivity;
import com.malata.recorder.IRecorderActor;
import com.malata.recorder.RecorderManager;
import com.malata.recorder.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * camera windows
 * 
 * @author hsm
 * @time 2015-09-24
 * 
 */

public class RecorderAppUI implements IRecorderAppUI {

	private static final String TAG = "RecorderAppUI";
	
	private SurfaceHolder mSurfaceHolder;
	private IRecorderActor mRecorderActor;

	private Handler mHandler;
	private SurfaceView mSurfaceView;
	private Activity mContext;
	private View mRightLayout;
	private ImageView mRecordImg;
	private TextView mTimeClock;
	private ImageView mRecIamge; 

	public RecorderAppUI(Activity context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	@Override
	public View getView() {

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.recorder_layout, null);
		mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceView.getHolder();
		// 创建UI就可以执行
		if (mRecorderActor == null)
			mRecorderActor = new RecorderManager(mContext, mHandler,
					mSurfaceHolder);

		mSurfaceHolder.addCallback(new CustomCallBack());

		mRecIamge=(ImageView)view.findViewById(R.id.image_rec);
		mRecIamge.bringToFront();
		mRecIamge.setVisibility(View.GONE);
		
		mTimeClock=(TextView) view.findViewById(R.id.date_time);
		mTimeClock.bringToFront();
		
		mRightLayout = view.findViewById(R.id.right_layout);
		mRightLayout.bringToFront();
		
		mRecordImg = (ImageView) view.findViewById(R.id.record);
		mRecordImg.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onShutterRecorderClick();
			}
		});
		view.findViewById(R.id.manager).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						onFileManagerClick();
					}
				});
		return view;
	}

	/**
	 * 
	 * SurfaceHolder Callback
	 * 
	 * @author hsm
	 * 
	 */
	private class CustomCallBack implements Callback {

		public void surfaceCreated(SurfaceHolder holder) {
			if (!mRecorderActor.isOpenCamera())
				return;
			try {
				mRecorderActor.initCamera();
			} catch (Exception e) {
				XLog.d(TAG, "CustomCallBack  surfaceCreated--finish");
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mRecorderActor.isOpenCamera())
				return;
			mRecorderActor.releaseRecorder();
			XLog.d(TAG, "CustomCallBack  surfaceDestroyed");
		}
	}

	@Override
	public void onShutterRecorderClick() {
		if(mRecorderActor!=null){
			if(mRecorderActor.getRecorderState()==RecorderState.STATE_IDLE){
				mRecorderActor.startRecorder();
				mRecIamge.setVisibility(View.VISIBLE);
				mRecordImg.setImageResource(R.drawable.stop);
			}else if(mRecorderActor.getRecorderState()==RecorderState.STATE_RECORDING){
				mRecIamge.setVisibility(View.GONE);
				mRecorderActor.stop();
				mRecordImg.setImageResource(R.drawable.start);
				try {
					mRecorderActor.initCamera();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					XLog.e(TAG, e.getMessage());
				}
			}
		}
	}
	
	@Override
	public void onFileManagerClick() {
		// TODO Auto-generated method stub
		Intent intent=new Intent(mContext,FileManageActivity.class);
		mContext.startActivity(intent);
	}

	@Override
	public RecorderState getRecorderState() {
		
		return mRecorderActor==null?RecorderState.STATE_IDLE:mRecorderActor.getRecorderState();
	}

	@Override
	public void initCamera() {
		if(mRecorderActor==null) return;
		try {
			mRecorderActor.initCamera();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XLog.e(TAG, e.getMessage());
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		if(mRecorderActor==null) return;
		mRecorderActor.stop();
		
	}
	
}
