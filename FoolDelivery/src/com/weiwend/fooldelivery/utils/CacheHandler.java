package com.weiwend.fooldelivery.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;

public class CacheHandler {
	
	//应用的缓存根目录
	private static String PROJECT_NAME="/FoolDelivery";
	
	//应用的缓存二级目录
	public static File getCacheDir(){
		
		String cachePath = PROJECT_NAME+"/cache";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	//图片缓存目录
	public static File getImageCacheDir(){
		String cachePath = PROJECT_NAME+"/cache/image";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	//app下载缓存目录
	public static File getDownloadCacheDir(){
		String cachePath = PROJECT_NAME+"/cache/download";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	//保存用户头像的缓存目录
	public static File getHeadImageCacheDir(){
		String cachePath = PROJECT_NAME+"/cache/image/head";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	//本地查询历史记录缓存目录
	public static File getUserInfoCacheDir(){
		String cachePath = PROJECT_NAME+"/cache/user";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	//分享快递单号的图片缓存目录
	public static File getShareCacheDir(){
		String cachePath = PROJECT_NAME+"/cache/share";
		File cacheDir = new File(Environment.getExternalStorageDirectory(),cachePath);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	/*public static File loadImage(String urlStr, String filename){
		InputStream inStream = null;
		File f = null;
		Log.e("zyf","loadImage start...");
		f = new File(getImageCacheDir(),filename);
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
					Log.e("zyf","after load image from network,disconnect it...");
				}
			}
		}else{
			Log.e("zyf","image is exsited,loadImage directly...");
		}
		Log.e("zyf","loadImage end...");
		return f;
	}*/
}
