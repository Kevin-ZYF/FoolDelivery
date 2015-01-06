package com.weiwend.invalid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.weiwend.fooldelivery.utils.CacheHandler;

//*****************************该界面由于需求问题，暂时已经作废*************************
@SuppressLint("NewApi") 
public class CustomImageView extends FrameLayout{
	
	private ImageView mImageView = null;
	private Context context;
	private AsyncDataLoader dataLoader = null;
	
	public static HashMap<String, Object> md5Map=new HashMap<String, Object>();

	public CustomImageView(Context context) {
		super(context);
		this.context = context;

		FrameLayout.LayoutParams imageViewLP = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,3);
		mImageView = new ImageView(context);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageView.setLayoutParams(imageViewLP);
		
		addView(mImageView);
	}
	
	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		
		FrameLayout.LayoutParams imageViewLP = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,3);
		mImageView = new ImageView(context);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageView.setLayoutParams(imageViewLP);
		
		addView(mImageView);
	}

	public void updateData(Context context, String url){
		if (dataLoader!=null)
		{
			dataLoader.cancel(true);
			dataLoader=null;
			
			Log.e("zyf","cancel the pre dataLoader...");
		}
		
		dataLoader=new AsyncDataLoader(url);
		dataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void setURL(String url){
		Uri uri = (Uri) md5Map.get(url);
		
		if(uri != null){
			mImageView.setImageURI(uri);
			
			Log.e("zyf","md5 md5 md5 md5 md5......");
		}else{
			mImageView.setImageBitmap(null);
			updateData(context, url);
		}
	}
	
	public void setScale(ScaleType scaleType){
		mImageView.setScaleType(scaleType);
	}
	
	public void setBackgroundResource(int resource){
		mImageView.setBackgroundResource(resource);
	}
	
	protected class AsyncDataLoader extends AsyncTask<Object, Object, Object>{
		private String url = null;
		private Uri uri = null;
		private String fileName = null; 
		
		public AsyncDataLoader(String url){
			this.url = url;
			
			fileName=url.substring(url.lastIndexOf("/") + 1);
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			
			Log.e("zyf","AsyncDataLoader doInBackground...");
			
			File f = null;
			String fileUrl = null;
			
			if(fileName != null){
				f = new File(CacheHandler.getImageCacheDir(), fileName);
				fileUrl = f.getAbsolutePath();
				
				Log.e("zyf","fileUrl: "+fileUrl);
			}
			 	
			if(fileUrl != null){
				md5Map.put(url, Uri.parse(fileUrl));
			}

			if(f != null){		
				try {
					loadImage(url, fileName);	
				} catch (Exception e) {
					Log.e("zyf","loadImage expection: "+e.toString());
				}
			}
			
			uri = (Uri) md5Map.get(url);
			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			mImageView.requestFocus();
			mImageView.setImageURI(uri);    
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}	
	}
	
	public static File loadImage(String urlStr, String filename){
		
		Log.e("zyf","loadImage start...");
		
		InputStream inStream = null;
		File f = new File(CacheHandler.getImageCacheDir(),filename);
		if(!f.exists()||f.length() == 0){
			HttpURLConnection con = null;
			try{
				Log.e("zyf","loadImage through Network...");
				Log.e("zyf","loadImage url: "+urlStr);
				
				f.createNewFile();
							
				URL url = new URL(urlStr);
				con = (HttpURLConnection)url.openConnection();
				con.setConnectTimeout(30000);
				
				inStream = con.getInputStream(); 
				FileOutputStream fos = new FileOutputStream(f);
				byte[] buffer = new byte[1024];
				int len = -1;
				
				while((len = inStream.read(buffer)) != -1){
					fos.write(buffer,0,len);
				}
				fos.close();
				
				Log.e("zyf","loadImage through Network successfully...");
			}catch(Exception e){
				f.delete();
				Log.e("zyf","loadImage exception: "+e.toString());
			}finally{
				if(con!=null){
					con.disconnect();
				}
			}
		}else{
			Log.e("zyf","image is exsited,loadImage directly...");
		}
		Log.e("zyf","loadImage end...");
		return f;
	}

}
