package com.malata.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.malata.recorder.utils.FileUtils;
import com.malata.recorder.utils.Preferences;
import com.malata.recorder.utils.Storage;
import com.malata.recorder.utils.XLog;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.widget.Toast;

/***
 * 
 * @author hsm
 * 
 * 
 */

@SuppressWarnings("deprecation")
public class RecorderManager implements IRecorderActor, OnErrorListener {

	private final double ASPECT_TOLERANCE = 0.001;
	private final Point mLowPoint = new Point(720, 1280);
	private final Point mMidPoint = new Point(1080, 1920);
	private final int mLowBitRate = 1 * 512 * 1024;
	private final int mMidBitRate = 1 * 1024 * 1024;

	private final String TAG = "RecorderManager";

	private boolean openCamera = true;

	private int mWidth = mLowPoint.x;
	private int mHeight = mLowPoint.y;
	private int mBitRate;
	private int mRecordMaxTime = 60 * 5 + 1;
	private File mVecordFile = null;
	private Camera mCamera;
	private MediaRecorder mMediaRecorder;
	private Context mContext;
	private Handler mHandler;
	private SurfaceHolder mSurfaceHolder;
	private RecorderState mRecordState = RecorderState.STATE_UNKNOWN;

	private ObjectAnimator mAnimator;

	public RecorderManager(Context context, Handler handler,
			SurfaceHolder surfaceHolder) {
		mContext = context;
		mHandler = handler;
		mSurfaceHolder = surfaceHolder;
	}

	@Override
	public boolean isOpenCamera() {
		return openCamera;
	}

	public void setOpenCamera(boolean openCamera) {
		this.openCamera = openCamera;
	}

	@Override
	public void initCamera() throws Exception {
		if (mCamera != null) {
			releaseRecorder();
		}
		try {
			mCamera = Camera.open();
			mCamera.setAutoFocusMoveCallback(null);
		} catch (Exception e) {
			mHandler.sendEmptyMessage(RecorderActivity.OPERATION_GONE);
			releaseRecorder();
			Message msg=new Message();
			msg.what=RecorderActivity.OPERATION_WRAN;
			msg.obj=mContext.getString(R.string.cannot_connect_camera);
			mHandler.sendMessage(msg);
		} finally {

			if (mCamera == null)
				return;
		}
		setCameraParams(null);
		mCamera.setDisplayOrientation(0);
		mCamera.setPreviewDisplay(mSurfaceHolder);
		mCamera.startPreview();
		mCamera.unlock();
		setRecorderState(RecorderState.STATE_IDLE);
	}

	@Override
	public void setCameraParams(Parameters params) {
		if (mCamera != null) {
			if (params == null)
				params = mCamera.getParameters();
			Point ponit = getBestPreviewSize(params);
			params.setPreviewSize(ponit.x, ponit.y);
			params.setPictureSize(ponit.x, ponit.y);
			mCamera.setParameters(params);
		}
	}

	@Override
	public Point getBestPreviewSize(Parameters params) {
		List<Point> list = new ArrayList<Point>();
		List<Size> Sizes = params.getSupportedVideoSizes();
		for (Size s : Sizes) {
			if (toleranceRatio(RATIO16X9, (double) s.width / s.height)) {
				XLog.e(TAG, s.width + "----" + s.height);
				list.add(new Point(s.width, s.height));
			}
		}
		return new Point(list.get(list.size() - 1).x,
				list.get(list.size() - 1).y);
	}

	/**
	 * 
	 * @param target
	 * @param candidate
	 * @return
	 */
	private boolean toleranceRatio(double target, double candidate) {
		boolean tolerance = true;
		if (candidate > 0) {
			tolerance = Math.abs(target - candidate) <= ASPECT_TOLERANCE;
		}
		XLog.d(TAG, "toleranceRatio(" + target + ", " + candidate + ") return "
				+ tolerance);
		return tolerance;
	}

	@Override
	public Camera getCamera() {
		return mCamera;
	}

	@Override
	public void releaseRecorder() {

		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.lock();
			mCamera.release();
			mCamera = null;
			setRecorderState(RecorderState.STATE_UNKNOWN);
		}

		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
	}

	private boolean checkStorage() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			mVecordFile = FileUtils.createRecordDir();

			// check storage size
			if (!checkStorageSize()) {
				mHandler.sendEmptyMessage(RecorderActivity.OPERATION_GONE);
				Message msg=new Message();
				msg.what=RecorderActivity.OPERATION_WRAN;
				msg.obj=mContext.getString(R.string.dialog_message_space);
				mHandler.sendMessage(msg);
				return false;
			}
			return true;
		} else {
			mHandler.sendEmptyMessage(RecorderActivity.OPERATION_HIDE);
			Message msg=new Message();
			msg.what=RecorderActivity.OPERATION_WRAN;
			msg.obj=mContext.getString(R.string.toast_no_sd_card);
			mHandler.sendMessage(msg);
		}
		return false;
	}

	private boolean checkStorageSize() {
		// sd crad totalSpase
		Long totalSpase = Storage.getSDAvailableSize();

		// record cacha space
		int cacheSpace = Preferences.getCacheSpace(mContext);

		// record usage space
		double dirRecordSize = Storage.getDirRecordSize();

		if (totalSpase < cacheSpace - dirRecordSize) {
			return false;
		}

		// if dirRecordSize will > cacheSpace ==> delete EarlyFile
		long estimateSize;
		// step 1 : get file dir excess space
		double excess = cacheSpace - dirRecordSize;
		// step 2: get this record need size
		int time = Preferences.getRecorderTime(mContext) / 60;
		if (Preferences.getRecorderQuality(mContext) == IRecorderActor.RecorderQuality.QUALITY_LOW
				.ordinal()) {
			estimateSize = time * Storage.QUALITY_LOW_SIZE;
		} else {
			estimateSize = time * Storage.QUALITY_MID_SIZE;
		}
		while (excess < estimateSize) {
			FileUtils.deleteEarlyFile();
		}
		return true;
	}

	@Override
	public void startRecorder() {
		// check storage space
		if (!checkStorage()) {
			return;
		}

		try {
			// if not open then open Camera
			if (!isOpenCamera())
				initCamera();
			initRecorder();
			mRecordMaxTime = Preferences.getRecorderTime(mContext);
			mAnimator = ObjectAnimator.ofInt(this, "recordTime", 1000,
					mRecordMaxTime);
			mAnimator.setDuration(mRecordMaxTime * 1000);
			mAnimator.start();

		} catch (Exception e) {
			XLog.e(TAG, e.getMessage());
		}
	}

	@Override
	public void stop() {
		if (mVecordFile != null)
			galleryAddPic(mContext, mVecordFile.getAbsolutePath());
		stopRecord();
		release();
		releaseRecorder();
	}

	private void release() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			try {
				mMediaRecorder.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mMediaRecorder = null;
	}

	@Override
	public void setRecordTime(int time) {
		if (time == mRecordMaxTime) {
			stop();
			startRecorder();
		}
	}

	public void stopRecord() {
		if (mMediaRecorder != null) {
			// To prevent the collapse
			mMediaRecorder.setOnErrorListener(null);
			mMediaRecorder.setPreviewDisplay(null);
			try {
				mMediaRecorder.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initRecorder() throws IOException {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.reset();
		if (mCamera != null)
			mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setVideoSource(VideoSource.CAMERA);

		if (Preferences.getRecorderSound(mContext)) {
			mMediaRecorder.setAudioSource(AudioSource.MIC);
			mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
		}

		if (Preferences.getRecorderQuality(mContext) == RecorderQuality.QUALITY_LOW
				.ordinal()) {
			mHeight = mLowPoint.y;
			mWidth = mLowPoint.x;
			mBitRate = mLowBitRate;
		} else {
			mHeight = mMidPoint.y;
			mWidth = mMidPoint.x;
			mBitRate = mMidBitRate;
		}

		mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);
		mMediaRecorder.setVideoSize(mHeight, mWidth);
		// mMediaRecorder.setVideoFrameRate(16);
		mMediaRecorder.setVideoEncodingBitRate(mBitRate);
		mMediaRecorder.setOrientationHint(0);
		mMediaRecorder.setVideoEncoder(VideoEncoder.H264);

		mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
		mMediaRecorder.prepare();
		try {
			mMediaRecorder.start();
			setRecorderState(RecorderState.STATE_RECORDING);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
		try {
			if (mr != null)
				mr.reset();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public RecorderState getRecorderState() {
		// TODO Auto-generated method stub
		return mRecordState;
	}

	@Override
	public void setRecorderState(RecorderState state) {
		// TODO Auto-generated method stub
		mRecordState = state;
	}

	/**
	 * add to gallery
	 * 
	 * @param context
	 * @param path
	 */
	private void galleryAddPic(Context context, String path) {
		if (path == null) {
			return;
		}
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

}
