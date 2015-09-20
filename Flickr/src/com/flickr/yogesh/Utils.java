package com.flickr.yogesh;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.flickr.yogesh.MainActivity.MyHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

public class Utils {

	
	private static int CONNECT_TIMEOUT_MS = 5000;
	private static int READ_TIMEOUT_MS = 15000;
	
	private static final String BASE_URL = "https://api.flickr.com/services/rest/?method=";
	private static final String FLICKR_PHOTOS_SEARCH_STRING = "flickr.photos.search";
	
	private static final String APIKEY_SEARCH_STRING = "&api_key=04913f5bf37d5ada4a68a07ffa9a7066";
	
	private static final String TAGS_STRING = "&tags=";
	public static final String MYPREFERENCES = "searchPref";
	private static final String FORMAT_STRING = "&format=json";
	public static final int PHOTO_THUMB = 111;
	private static final String PAGE = "&page=";
	public static final int PHOTO_FULL = 222;;

	public static MyHandler handler;

	private static String createURL(String parameter,int page) {
		
		String method_type = "";
		String url = null;
		method_type = FLICKR_PHOTOS_SEARCH_STRING;
		url = BASE_URL + method_type + APIKEY_SEARCH_STRING + TAGS_STRING + parameter + FORMAT_STRING + "&nojsoncallback=1"+PAGE+page;
		
		return url;
	}


	public static Bitmap getThumbnail(JsonItem imgCon) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(imgCon.thumbURL);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (Exception e) {
			Log.e("yogesh", e.getMessage());
		}
		return bm;
	}
	
	public static Bitmap getPhoto(String photoURL) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(photoURL);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (Exception e) {
			Log.e("yogesh", e.getMessage());
		}
		return bm;
	}

	public static class GetThumbnailsThread extends Thread {
		MyHandler uih;
		JsonItem imgContener;

		public GetThumbnailsThread(MyHandler uih, JsonItem imgCon) {
			this.uih = uih;
			this.imgContener = imgCon;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			imgContener.thumb = getThumbnail(imgContener);
			if (imgContener.thumb != null) {
				Message msg = Message.obtain(uih, MyHandler.ID_UPDATE_ADAPTER);
				uih.sendMessage(msg);

			}
		}

	}

	public static ArrayList<JsonItem> searchImagesByTag(MyHandler uih, Context ctx, String tag, int offset,int page) {
		handler = uih;
		String url = createURL(tag,page);
		ArrayList<JsonItem> tmp = new ArrayList<JsonItem>();
		String jsonString = null;
		try {
			if (isOnline(ctx)) {
				ByteArrayOutputStream baos = readBytes(url);
				jsonString = baos.toString();
			}
			try {
				JSONObject root = new JSONObject(jsonString.replace("jsonFlickrApi(", "").replace(")", ""));
				JSONObject photos = root.getJSONObject("photos");
				JSONArray imageJSONArray = photos.getJSONArray("photo");
				for (int i = 0; i < offset; i++) {
					if(offset > 100) {
						Message msg = Message.obtain(uih, MyHandler.FINISH);
						uih.sendMessage(msg);
						return tmp;
					}
					JSONObject item = imageJSONArray.getJSONObject(i);
					JsonItem imgCon = new JsonItem(item.getString("id"), item.getString("owner"), item.getString("secret"), item.getString("server"),
							item.getString("farm"));
					imgCon.position = i;
					tmp.add(imgCon);
				}
				Message msg = Message.obtain(uih, MyHandler.ID_METADATA_DOWNLOADED);
				msg.obj = tmp;
				uih.sendMessage(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NullPointerException nue) {
			nue.printStackTrace();
		}

		return tmp;
	}
	
	public static boolean isOnline(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	
	public static ByteArrayOutputStream readBytes(String urlS) {
		ByteArrayOutputStream baos = null;
		InputStream is = null;
		HttpURLConnection httpURLConnection = null;
		try {
			
			URL url = new URL(urlS);
			Log.i("yogesh", url.toString());
			httpURLConnection = (HttpURLConnection) url.openConnection();
			int response = httpURLConnection.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
				httpURLConnection.setReadTimeout(READ_TIMEOUT_MS);
				is = new BufferedInputStream(httpURLConnection.getInputStream());

				int size = 1024;
				byte[] buffer = new byte[size];

				baos = new ByteArrayOutputStream();
				int read = 0;
				while ((read = is.read(buffer)) != -1) {
					if (read > 0) {
						baos.write(buffer, 0, read);
						buffer = new byte[size];
					}

				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (httpURLConnection != null) {
				try {
					httpURLConnection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return baos;
	}

}
