package com.malata.recorder;

import com.malata.recorder.IRecorderActor.RecorderState;

import android.view.View;

public interface IRecorderAppUI {
	
	public void onShutterRecorderClick();
	
	public void onFileManagerClick();
	
	public View getView();
	
	public RecorderState getRecorderState();
	
	public void initCamera();
	
	public void stop();
	
}
