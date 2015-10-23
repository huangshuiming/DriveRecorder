package com.malata.recorder;

import java.io.IOException;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

@SuppressWarnings("deprecation")
public interface IRecorderActor {
	
	
	String RECORDER_QUALITY="recorder_quality";
	
	String RECORDER_TIME="recorder_time";
	
	String RECORDER_SOUND="recorder_sound";
	
	String RECORDER_CACHE_SPACE="recorder_cache_space";
		
	public enum RecorderState{
		STATE_UNKNOWN,
		STATE_IDLE,
		STATE_RECORDING,
	}
	
	public enum RecorderQuality{
		QUALITY_LOW,
		QUALITY_MID,
	}
	
	double RATIO16X9=1.7778;
	
	double RATIO4X3=1.3333;
	
	public void initCamera() throws Exception;
	
	public void initRecorder()throws IOException;
	
	public Camera getCamera();
	
	public void releaseRecorder();
	
	public void startRecorder();
	
	public void stop();
	
	public RecorderState getRecorderState();
	
	public void setRecorderState(RecorderState state);
	
	public void setRecordTime(int time);
	
	public void setCameraParams(Parameters params);
	
	public boolean isOpenCamera();
	
	public Point getBestPreviewSize(Parameters params);
	
}
