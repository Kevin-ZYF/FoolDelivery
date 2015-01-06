package com.weiwend.fooldelivery.customviews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.utils.CacheHandler;
import com.weiwend.fooldelivery.utils.MyOnImageLoadSuccessListener;

@SuppressLint("NewApi") 
public class MyLoaderImageView extends FrameLayout{
	
	//显示图像的控件
	private ImageView mImageView = null;
	
	//上下文对象
	private Context context;
	
	//下载图像的异步加载类
	private AsyncDataLoader dataLoader = null;
	
	//保存正在下载的图像
	public static HashMap<String, Object> md5Map=new HashMap<String, Object>();
	
	private MyOnImageLoadSuccessListener mImageLoadSuccessListener;
	
	public void setMyOnImageLoadSuccessListener(MyOnImageLoadSuccessListener mImageLoadSuccessListener){
		
		this.mImageLoadSuccessListener=mImageLoadSuccessListener;
	}

	public MyLoaderImageView(Context context) {
		this(context,null);
	}
	
	public MyLoaderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}
	
	private void init(Context context){
		
		FrameLayout.LayoutParams imageViewLP = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView=new ImageView(context);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageView.setLayoutParams(imageViewLP);
		
		addView(mImageView);
	}

	//下载云端的图像
	public void updateData(Context context, String url){
		
		if(dataLoader!=null){
			dataLoader.cancel(true);
			dataLoader=null;
		}
		
		dataLoader=new AsyncDataLoader(url);
		dataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	//设置图像的url
	public void setURL(String url){
		
		Uri uri=(Uri)md5Map.get(url);
		
		if(uri!=null){    //所要下载的图像已经下载成功，且存在于内存中，所以直接加载图像
			
			mImageView.setImageURI(uri);
			
			Log.e("zyf","load from memory......");
			
		}else{    //所要下载的图像在内存中不存在，从本地缓存中寻找
			
			String fileName=url.substring(url.lastIndexOf("/")+1);
			
			File f=new File(CacheHandler.getImageCacheDir(),fileName);;
			
			if(f.exists()){  //所要下载的图像存在于缓存目录中，直接加载
				
				Log.e("zyf","load image from sdcard directly......"+f.getAbsolutePath());
				
				try {
					mImageView.setImageURI(Uri.parse(f.getAbsolutePath()));
				} catch (Exception e) {
					Log.e("zyf","pic load exception..."+e.toString());
				}
			}else{ //所要下载的图像在缓存中不存在，所以从云端下载
				
				updateData(context,url);
				
			}
		}
	}
	
	/*public void setBitmap(Bitmap bitmap){
		mImageView.setImageBitmap(bitmap);
	}
	
	public void setImageURI(File f){

		try {
			mImageView.setImageURI(Uri.parse(f.getAbsolutePath()));
		} catch (Exception e) {
			Log.e("zyf","加载图片异常..."+e.toString());
		}
	}
	
	public void setScale(ScaleType scaleType){
		mImageView.setScaleType(scaleType);
	}
	
	public void setBackgroundResource(int resource){
		mImageView.setBackgroundResource(resource);
	}*/
	
	//从云端下载图片
	protected class AsyncDataLoader extends AsyncTask<Object, Object, Object>{
		private String url = null;
		private Uri uri = null;
		private String fileName = null; 
		
		public AsyncDataLoader(String url){
			this.url = url;
			
			fileName=url.substring(url.lastIndexOf("/")+1);
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			
			Log.e("zyf","AsyncDataLoader doInBackground...");
			
			File f=null;
			String fileUrl = null;
			
			if(fileName!=null){
				
				f=new File(CacheHandler.getImageCacheDir(), fileName);
				
				fileUrl=f.getAbsolutePath();
				
				Log.e("zyf","fileUrl: "+fileUrl);
			}
			 	
			if(fileUrl!=null){
				md5Map.put(url,Uri.parse(fileUrl));    //将下载信息保存于内存
			}
			
			downloadImage(url,fileName);
			
			uri=(Uri)md5Map.get(url);
			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			mImageView.requestFocus();
			mImageView.setImageURI(uri);    
		}
	}
	
	private boolean downloadSuccess;
	
	//下载图片
	public File downloadImage(String urlStr, String filename){
		
		Log.e("zyf","download image start...");
		
		InputStream inStream=null;
		
		File f;
		
		f=new File(CacheHandler.getImageCacheDir(),filename);
		
		HttpURLConnection con=null;
		try{
			Log.e("zyf","download image url: "+urlStr);
			
			f.createNewFile();
						
			URL url=new URL(urlStr);
			con=(HttpURLConnection)url.openConnection();
			con.setConnectTimeout(DataConfigs.TIME_OUT);
			
			inStream=con.getInputStream(); 
			FileOutputStream fos=new FileOutputStream(f);
			byte[] buffer=new byte[1024];
			int len=-1;
			
			while((len=inStream.read(buffer))!=-1){
				fos.write(buffer,0,len);
			}
			fos.close();
			
			Log.e("zyf","download Image successfully...");
			
			downloadSuccess=true;
			
			/*if(mImageLoadSuccessListener!=null){
				mImageLoadSuccessListener.loadSuccess(index);
			}*/
			
			
		}catch(Exception e){     //图片下载失败，将下载信息从内存中移除
			f.delete();
			md5Map.remove(urlStr);
			Log.e("zyf","download Image exception: "+e.toString());
		}finally{
			if(con!=null){
				con.disconnect();
				con=null;
			}
		}
		
		Log.e("zyf","download Image end...");
		
		return f;
	}
	
}
