package com.malata.recorder.utils;



import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.os.Process;
import android.util.Log;

/**
 * application activityStack
 */
public class AppManager {
	private static final String TAG = "AppManager";

	public  static Stack<Activity> activityStack;
	private static AppManager instance;

	private AppManager() {
	}

	/**
	 * Singleton 
	 */
	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}

	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * add Activity to activityStack
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * finish top Activity
	 */
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		if(activity!=null){
			finishActivity(activity);
		}
	}

	/**
	 * finish the Activity 
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * finish cls Activity 
	 */
	public void finishActivity(Class<?> cls) {
		Stack<Activity> activitys = new Stack<Activity>();
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				activitys.add(activity);
			}
		}

		for (Activity activity : activitys) {
			finishActivity(activity);
		}
	}

	/**
	 * finish All Activity
	 */
	public void finishAllActivity() {
		for (int i = 0, size = activityStack.size(); i < size; i++) {
			if (null != activityStack.get(i)) {
				activityStack.get(i).finish();
			}
		}
		activityStack.clear();
	}

	/**
	 * exit app
	 */
	public void AppExit(Context context) {
		try {
			finishAllActivity();

			int pid = Process.myPid();
			Process.killProcess(pid);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// back 
	public boolean backActivity(Class<?> cls){
		boolean isExit = false;
		for (int i = 0; i < activityStack.size(); i++) {
			if (cls.equals(activityStack.get(i).getClass())) {
				isExit = true;
			}
		}
		for (int i = activityStack.size()-1; i > 0; i--) {
			Class<? extends Activity> class1 = activityStack.get(i).getClass();
			Log.i(TAG, class1.toString());
			if (!cls.equals(class1)) {
				finishActivity();
				Log.i(TAG, class1.toString()+"delete");
			}else{
				break;
			}
		}
		return isExit;
	}
}
