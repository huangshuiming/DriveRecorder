package com.malata.recorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.malata.mounted.BaseActivity;
import com.malata.recorder.utils.FileUtils;
import com.malata.recorder.utils.PoolThread;
import com.malata.recorder.utils.Storage;

import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class FileManageActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {

	private final String TAG = FileManageActivity.class.getSimpleName();
	private ListView mListView;
	private Context mContext;
	private View mBack;
	private View mSettings;
	private BaseAdapter mAdapter;
	private List<String> mFileList = new ArrayList<String>();
	private List<String> mFileSize = new ArrayList<String>();
	private List<String> mFileTime = new ArrayList<String>();
	private Map<Integer, Bitmap> mThumbnailMap = new HashMap<Integer, Bitmap>();
	private Map<Integer, Boolean> mTaskMap = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private ThumbnailTask mThumbnailTask;
	private FileTask mFileTask;
	private ProgressDialog mProgressDialog;
	private boolean isSeleceOption = false;
	private boolean isAllSelect = false;

	private View mOptionView;
	private View mActiobar;
	private Button mAllOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_file_manage);
		mFileTask = new FileTask();
		mFileTask.executeOnExecutor(PoolThread.THREAD_POOL_EXECUTOR, "");
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		mBack = findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mSettings = findViewById(R.id.settings);
		mSettings.setVisibility(View.VISIBLE);
		mSettings.setOnClickListener(this);

		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new FileAdaprer();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		mOptionView = findViewById(R.id.layout_option);
		mOptionView.setVisibility(View.GONE);
		findViewById(R.id.back_option).setOnClickListener(this);
		mAllOption = (Button) findViewById(R.id.all_option);
		mAllOption.setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);
		mActiobar = findViewById(R.id.actionbar);

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mThumbnailTask != null
				&& mThumbnailTask.getStatus() == Status.RUNNING) {
			mThumbnailTask.cancel(true);
		}
		if (mFileTask != null && mFileTask.getStatus() == Status.RUNNING) {
			mFileTask.cancel(true);
		}
	}

	private class FileAdaprer extends BaseAdapter {

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getCount() {
			return mFileList.size();
		}
		
		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			if(mFileTime.get(position).equals(getString(R.string.file_recording_str))){
				return false;
			}
			return super.isEnabled(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHodler hodler = null;
			if (convertView == null) {
				hodler = new ViewHodler();
				convertView = getLayoutInflater().inflate(R.layout.file_item,
						null);
				hodler.fileName = (TextView) convertView
						.findViewById(R.id.fileName);
				hodler.fileSize = (TextView) convertView
						.findViewById(R.id.fileSize);
				hodler.fileTime = (TextView) convertView
						.findViewById(R.id.fileTime);
				hodler.img = (ImageView) convertView.findViewById(R.id.fileImg);
				hodler.option = (ImageView) convertView
						.findViewById(R.id.option);
				convertView.setTag(hodler);
			} else {
				hodler = (ViewHodler) convertView.getTag();
			}
			hodler.fileSize.setText(String.format(getString(R.string.file_size_str), mFileSize.get(position)));
			hodler.fileTime.setText(String.format(getString(R.string.file_time_str), mFileTime.get(position)));
			Log.e("FileManager", "position----" + position);
			hodler.fileName.setText(mFileList.get(position));
			if (mThumbnailMap.get(position) != null) {
				hodler.img.setImageBitmap(mThumbnailMap.get(position));
			} else {
				hodler.img.setImageResource(R.drawable.record_defalut_icon);
				if (mTaskMap.get(position) == null || mTaskMap.get(position)==false) {
					mTaskMap.put(position, true);
					mThumbnailTask = new ThumbnailTask(position, hodler.img);
					mThumbnailTask.executeOnExecutor(
							PoolThread.THREAD_POOL_EXECUTOR, "");
				}
			}

			if (isSeleceOption) {
				if (mSelectMap.get(position) == null
						|| mSelectMap.get(position) == false) {
					hodler.option
							.setImageResource(R.drawable.record_check_defalut);
				} else {
					hodler.option
							.setImageResource(R.drawable.record_check_press);
				}
				hodler.option.setVisibility(View.VISIBLE);

			} else {
				hodler.option.setVisibility(View.GONE);
			}
			
			return convertView;
		}

	}

	private class ViewHodler {
		ImageView img;
		TextView fileName;
		TextView fileSize;
		TextView fileTime;
		ImageView option;
	}

	private class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView img;
		private int position;

		public ThumbnailTask(int position, ImageView img) {
			this.img = img;
			this.position = position;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			return ThumbnailUtils.createVideoThumbnail(Storage.StoragePath
					+ mFileList.get(position), Images.Thumbnails.MINI_KIND);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result == null)
				return;
			mThumbnailMap.put(position, result);
			if (mListView.getFirstVisiblePosition() <= position
					&& mListView.getLastVisiblePosition() >= position) {
				img.setImageBitmap(result);
			}
		}
	}

	private class FileTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage(getString(R.string.loading_str));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			mFileSize.clear();
			mFileTime.clear();
			ArrayList<String> fileList = (ArrayList<String>) FileUtils
					.getVideoFileName();
			for (String str : fileList) {
				mFileSize.add(FileUtils.getFileSize(str));
				mFileTime
						.add(FileUtils.getVideoTime(getContentResolver(),getResources(), str));
			}
			mFileList = fileList;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.cancel();
			mAdapter.notifyDataSetChanged();
		}

	}

	private void exitOptionSelect() {
		exitOptionSelect(true);
	}
	
	private void exitOptionSelect(boolean isNotify) {
		isSeleceOption = false;
		isAllSelect = false;
		mSelectMap.clear();
		mOptionView.setVisibility(View.GONE);
		mActiobar.setVisibility(View.VISIBLE);
		if(isNotify)
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.back_option:
			exitOptionSelect();
			break;
		case R.id.all_option:
			optionAllSelect();
			break;
		case R.id.delete:
			deleteRecorder();
			break;
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void optionAllSelect() {
		if (!isSeleceOption)
			return;
		isAllSelect = !isAllSelect;
		for (int i = 0; i < mFileList.size(); i++) {
			if (isAllSelect) {
				mSelectMap.put(i, true);
			} else {
				mSelectMap.put(i, false);
			}
		}
		mAllOption.setText(isAllSelect ? getString(R.string.all_select_option) : getString(R.string.all_no_select_option));
		mAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("rawtypes")
	private void deleteRecorder() {
		Iterator<?> iter = mSelectMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Boolean val = (Boolean) entry.getValue();
			if (val) {
				Integer key = (Integer) entry.getKey();
				FileUtils.deleteRecordFile(mFileList.get(key));
				mThumbnailMap.put(key, null);
				mTaskMap.put(key, false);
			}
		}
		mFileTask = new FileTask();
		mFileTask.executeOnExecutor(PoolThread.THREAD_POOL_EXECUTOR, "");
		exitOptionSelect(false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (isSeleceOption) {

			if (mSelectMap.get(position) == null
					|| mSelectMap.get(position) == false) {
				mSelectMap.put(position, true);
			} else {
				mSelectMap.put(position, false);
			}
			mAdapter.notifyDataSetChanged();

		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			String type = "video/mp4";
			System.out.println(TAG + Storage.StoragePath
					+ mFileList.get(position));
			Uri uri = Uri.parse(Storage.StoragePath + mFileList.get(position));
			intent.setDataAndType(uri, type);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		isSeleceOption = !isSeleceOption;
		if (isSeleceOption) {
			mSelectMap.put(position, true);
			mOptionView.setVisibility(View.VISIBLE);
			mActiobar.setVisibility(View.GONE);
			mAdapter.notifyDataSetChanged();
		} else {
			exitOptionSelect();
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && isSeleceOption) {
			exitOptionSelect();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
