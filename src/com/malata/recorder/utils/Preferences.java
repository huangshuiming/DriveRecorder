package com.malata.recorder.utils;

import com.malata.recorder.IRecorderActor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * record Preferences 
 * @author hsm
 *
 */
public class Preferences {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static int getRecorderQuality(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);

		return sp.getInt(IRecorderActor.RECORDER_QUALITY, 1);
	}

	public static void setRecorderQuality(Context context, int quality) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		sp.edit().putInt(IRecorderActor.RECORDER_QUALITY, quality).apply();
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static int getRecorderTime(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);

		return sp.getInt(IRecorderActor.RECORDER_TIME, 5 * 60 + 1);
	}

	public static void setRecorderTime(Context context, int time) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		sp.edit().putInt(IRecorderActor.RECORDER_TIME, (time+1) * 60+1).apply();
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean getRecorderSound(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);

		return sp.getInt(IRecorderActor.RECORDER_SOUND, 0)==1;
	}

	public static void setRecorderSound(Context context, boolean is) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		if(is)
		    sp.edit().putInt(IRecorderActor.RECORDER_SOUND, 1).apply();
		else
			sp.edit().putInt(IRecorderActor.RECORDER_SOUND, 0).apply();
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static int getCacheSpace(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);

		return sp.getInt(IRecorderActor.RECORDER_CACHE_SPACE, 1000)*1024*1024;
	}

	public static void setCacheSpace(Context context,int space){

		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		    sp.edit().putInt(IRecorderActor.RECORDER_CACHE_SPACE, space).apply();
		    
	}

}
