package com.flickr.yogesh;

import java.util.ArrayList;

import com.flickr.yogesh.R;
import com.flickr.yogesh.Utils.GetThumbnailsThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts.Photo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public MyHandler handler;
	public MyAdapter mAdapter;
	private ArrayList<JsonItem> imageList;

	private Button downloadPhotos;
	private GridView mGridView;
	private Button imgView;
	private AutoCompleteTextView editText;
	ProgressDialog mProgress;
	private int limit = 20;
	private int page = 1;
	private String[] SuggestionList;
	SharedPreferences sharedpreferences;
	private InputMethodManager mImm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		handler = new MyHandler();

		downloadPhotos = (Button) findViewById(R.id.button1);
		editText = (AutoCompleteTextView) findViewById(R.id.editText1);
		mGridView = (GridView) findViewById(R.id.grid);
		mGridView.setOnItemClickListener(itemClick);
		imgView = (Button) findViewById(R.id.imageView1);
		imgView.setVisibility(View.GONE);
		makeSuggestion();
		mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imgView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new GetMoreTask().execute();
			}
		});
		downloadPhotos.setOnClickListener(onSearchButtonListener);

	}
	
	private void makeSuggestion() {
		sharedpreferences = getSharedPreferences(Utils.MYPREFERENCES, Context.MODE_PRIVATE);
		int size = sharedpreferences.getInt("Array_size", 0);
		SuggestionList = new String[size];
		if(size > 0) {
			for(int i=0;i<size;i++) {
				SuggestionList[i] = sharedpreferences.getString("array_"+i, "");
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, SuggestionList);
		
		editText.setAdapter(adapter);
	}

	private OnItemClickListener itemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			Holder holder = (Holder) arg1.getTag();
			Intent i = new Intent(getApplicationContext(), PhotoViewActivity.class);
			i.putExtra("PhotoURL", holder.photoURL);
			startActivity(i);
		}
	};

	public class MyAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<JsonItem> imageContener;

		public ArrayList<JsonItem> getImageContener() {
			return imageContener;
		}

		public void setImageContener(ArrayList<JsonItem> imageContener) {
			this.imageContener = imageContener;
		}

		public MyAdapter(Context c, ArrayList<JsonItem> imageContener) {
			mContext = c;
			this.imageContener = imageContener;
		}

		public int getCount() {
			return imageContener.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if(convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.imageview1);
				convertView.setTag(holder);
			}
			else {
				holder = (Holder) convertView.getTag();
			}
			if(position == imageContener.size()-1) {
				//Last element
				new GetMoreTask().execute();
			}
			Bitmap bmp = imageContener.get(position).getThumb();
			holder.photoURL = imageContener.get(position).getLargeURL();
			holder.img.setImageBitmap(bmp);
			return convertView;
		}

	}
	
	class Holder {
		ImageView img;
		String photoURL;
	}
	class MyHandler extends Handler {
		public static final int ID_METADATA_DOWNLOADED = 0;
		public static final int ID_SHOW_IMAGE = 1;
		public static final int ID_UPDATE_ADAPTER = 2;
		public static final int FINISH = 3;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ID_METADATA_DOWNLOADED:
				if (msg.obj != null) {
					imageList = (ArrayList<JsonItem>) msg.obj;
					mAdapter = new MyAdapter(getApplicationContext(), imageList);
					mGridView.setAdapter(mAdapter);
					imgView.setText("Load More");
					for (int i = 0; i < mAdapter.getCount(); i++) {
						new GetThumbnailsThread(handler, mAdapter.getImageContener().get(i)).start();
					}
				}
				break;
			
			case ID_UPDATE_ADAPTER:
				mAdapter.notifyDataSetChanged();
				break;
				
			case FINISH:
				imgView.setText("Load Next Page");
				page++;
				limit = 20;
				//new GetMoreTask().execute();
				break;
			}
			super.handleMessage(msg);
		}
	}

	OnClickListener onSearchButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mGridView.getAdapter() != null) {
				mAdapter.imageContener = new ArrayList<JsonItem>();
				mGridView.setAdapter(mAdapter);
			}
			if(mImm != null) {
				new Handler().postAtTime(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mImm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
						
					}
				}, 500);
			}
			int orignal_size = sharedpreferences.getInt("Array_size", 0);
			int new_size = orignal_size+1;
			String newTag = editText.getText().toString().trim();
			Editor edit = sharedpreferences.edit();
			edit.putInt("Array_size", new_size);
			boolean AlreadyPresent = false;
			for(int i=0;i<orignal_size;i++) {
				if(newTag.equalsIgnoreCase(sharedpreferences.getString("array_"+i, ""))) {
					AlreadyPresent = true;
					break;
				}
					
			}
			if(!AlreadyPresent)
				edit.putString("array_" + orignal_size, newTag);
			edit.commit();
			
			new GetPhotoTask().execute();
		}
	};
	
	class GetPhotoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mProgress = new ProgressDialog(MainActivity.this);
			mProgress.setTitle("Loading");
			mProgress.setMessage("Loading the photos...");
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.show();
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			String tag = editText.getText().toString().trim();
			if (tag != null && tag.length() >= 3)
				Utils.searchImagesByTag(handler, getApplicationContext(), tag,limit,page);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(mProgress != null)
				mProgress.dismiss();
			imgView.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
		
	}
	
	class GetMoreTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mProgress = new ProgressDialog(MainActivity.this);
			mProgress.setTitle("Loading");
			mProgress.setMessage("Loading the photos...");
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.show();
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			String tag = editText.getText().toString().trim();
			if (tag != null && tag.length() >= 3)
				Utils.searchImagesByTag(handler, getApplicationContext(), tag,limit+=20,page);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(mProgress != null)
				mProgress.dismiss();
			super.onPostExecute(result);
		}
		
	}

}
