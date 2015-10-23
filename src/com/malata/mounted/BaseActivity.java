package com.malata.mounted;


import com.malata.recorder.utils.AppManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

public class BaseActivity extends Activity {
	
	protected AppManager mAppManager ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
				LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		
		mAppManager = AppManager.getAppManager();
		mAppManager.addActivity(this);
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mAppManager.finishActivity(this);
	}

}
