package com.malata.recorder.utils;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

@SuppressWarnings("deprecation")
public class Storage {
	
	public static final String TAG = "Storage";
	
	// one minute space size
	public static final long QUALITY_LOW_SIZE=5*1024*1024;
	public static final long QUALITY_MID_SIZE=10*1024*1024;
	
	public static String StoragePath = Environment
			.getExternalStorageDirectory() + File.separator + "CarRrecord/";
	
	
	/** 
     *  sd card free space size
     *  
     * @return 
     */  
    
	public static Long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return blockSize * availableBlocks;  
    }  
	
	/**
	 * the file  exists space size
	 * @param file
	 * @return
	 */
	public static double getDirSize(File file) { 
		
        if (file.exists()) {     
            if (file.isDirectory()) {     
                File[] children = file.listFiles();     
                double size = 0;     
                for (File f : children)     
                    size += getDirSize(f);     
                return size;     
            } else {   
                double size = (double) file.length();        
                return size;     
            }     
        } else {     
            System.out.println("file not exists, please check "+ file.getAbsolutePath());     
            return 0.0;     
        }     
    }  
	
	/**
	 *  get Record file dir space size
	 */
	public static double getDirRecordSize() {
		return getDirSize(new File(StoragePath));
	}
	
}
