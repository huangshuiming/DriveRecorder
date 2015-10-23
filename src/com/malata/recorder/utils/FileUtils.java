package com.malata.recorder.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.malata.recorder.R;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;

public class FileUtils {

	/**
	 * create record dir
	 */
	public static File createRecordDir() {
		File sampleDir = new File(Storage.StoragePath);
		if (!sampleDir.exists()) {
			sampleDir.mkdirs();
		}
		File vecordDir = sampleDir;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		CharSequence format = DateFormat.format("yyyyMMddHHmmss", calendar);
		return new File(vecordDir, "record_" + format + ".mp4");
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	public static List<String> getVideoFileName() {
		List<String> fileList = new ArrayList<String>();
		File file = new File(Storage.StoragePath);
		File[] subFile = file.listFiles();

		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// is folder
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getName();
				// suffix is .mp4
				if (filename.trim().toLowerCase().endsWith(".mp4")) {
					fileList.add(filename);
				}
			}
		}
		return fileList;
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	public static String getFileSize(String filePath) {
		File file = new File(Storage.StoragePath + filePath);
		long totalSpace = file.length();
		if (totalSpace < 1024) {
			return totalSpace + " B";
		}
		long K = totalSpace / 1024;
		if (K < 1024) {
			return K + " KB";
		}
		double M = (double) K / 1024;
		if (M < 1024) {
			// not need to the nearest
			double M_D = (int) (M * 100);
			return M_D / 100 + " MB";
		}
		double G = M / 1024;
		double G_D = (int) (G * 100);
		return G_D / 100 + " GB";
	}

	/**
	 * 
	 * @return boolean is delete succeed
	 */
	public static boolean deleteEarlyFile() {
		List<String> videoList = getVideoFileName();
		if (videoList.size() <= 0) {
			return false;
		}
		String firstName = videoList.get(0);
		for (String str : videoList) {
			if (str.compareTo(firstName) < 0)
				firstName = str;
		}
		return deleteRecordFile(firstName);
	}

	/**
	 * delete record StoragePath file
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteRecordFile(String filePath) {

		File file = new File(Storage.StoragePath + filePath);

		return file.delete();
	}

	/**
	 * delete record StoragePath All file
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteAllRecordFile() {

		List<String> videoList = getVideoFileName();
		if (videoList.size() <= 0) {
			return false;
		}
		for (String str : videoList) {
			File file = new File(Storage.StoragePath + str);
			file.delete();
		}
		return true;
	}

	/**
	 * @author hsm
	 * @param cr
	 * @param fileName
	 * @return video time
	 */
	public static String getVideoTime(ContentResolver cr, Resources rs,
			String fileName) {
		String[] proj = { MediaStore.Video.Media.DURATION,
				MediaStore.Video.Media.TITLE };
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = null;
		String nameFromURI = rs.getString(R.string.file_recording_str);
		int lastIndexOf = fileName.lastIndexOf(".");
		String substring = fileName.substring(0, lastIndexOf);
		try {
			cursor = cr.query(uri, proj, MediaStore.Video.Media.TITLE + "=?",
					new String[] { substring }, null);
			if (cursor == null) {
				return null;
			}
			int colummIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
			cursor.moveToFirst();
			nameFromURI = cursor.getString(colummIndex);
			nameFromURI = formatDetailedDuration(rs,
					Integer.parseInt(nameFromURI));

		} catch (Exception e) {
			Log.e("", "getVideoRealPathFromURI Exception", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return nameFromURI;
	}

	/**
	 * millis/100    
	 * 
	 * format 00:00
	 * 
	 * @param rs
	 * @param millis
	 * @return
	 */
	public static String formatDetailedDuration(Resources rs, long millis) {
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int elapsedSeconds = (int) (millis / 1000);
		if (elapsedSeconds >= 3600) {
			hours = elapsedSeconds / 3600;
			elapsedSeconds -= hours * 3600;
		}
		if (elapsedSeconds >= 60) {
			minutes = elapsedSeconds / 60;
			elapsedSeconds -= minutes * 60;
		}
		seconds = elapsedSeconds;

		StringBuilder duration = new StringBuilder();
		if (hours > 0) {
			duration.append(minutes + rs.getString(R.string.file_time_hour));
		}
		if (minutes > 0) {
			if (hours > 0) {
				duration.append(' ');
			}
			duration.append(minutes + rs.getString(R.string.file_time_minute));
		}
		if (seconds > 0) {
			if (hours > 0 || minutes > 0) {
				duration.append(' ');
			}
			duration.append(seconds + rs.getString(R.string.file_time_second));
		}
		return duration.toString();
	}
}
