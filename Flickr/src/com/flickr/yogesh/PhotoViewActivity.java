package com.flickr.yogesh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoViewActivity extends Activity {

	private String photoURL;
	private Bitmap bmp;
	private ImageView mFullImage;
	private ImageButton mDownLoadButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view);
		
		mFullImage = (ImageView) findViewById(R.id.my_photo);
		mDownLoadButton = (ImageButton) findViewById(R.id.imageButton1);
		
		mDownLoadButton.setOnClickListener(download);
		
		Intent i = getIntent();
		photoURL = i.getStringExtra("PhotoURL");
		
		
		new LoadPhotoTask(1).execute();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_view, menu);
		return true;
	}

	private OnClickListener download = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			new LoadPhotoTask(2).execute();
		}
	};
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		/*int id = item.getItemId();
		if(id == R.id.download) {
			new LoadPhotoTask(2).execute();
		}*/
		return super.onOptionsItemSelected(item);
	}
	
	
	class LoadPhotoTask extends AsyncTask<Void, Void, Void> {

		int type;
		public LoadPhotoTask(int type) {
			// TODO Auto-generated constructor stub
			this.type = type;
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			if(type == 1)
				bmp = Utils.getPhoto(photoURL);
			else if (type == 2) {
				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlickDown";
				File dir = new File(file_path);
				String name = photoURL.substring(photoURL.length()-10, photoURL.length()-6);
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, name+ ".png");
				FileOutputStream fOut = null;
				try {
					fOut = new FileOutputStream(file);

					bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
				try {
					fOut.flush();
					fOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(mFullImage != null)
				mFullImage.setImageBitmap(bmp);
			if(type == 2)
				Toast.makeText(getApplicationContext(), "Photo Downloaded", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
		
	}
}
