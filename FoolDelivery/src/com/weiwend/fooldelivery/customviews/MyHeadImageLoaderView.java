package com.weiwend.fooldelivery.customviews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.weiwend.fooldelivery.MyApplicaition;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.utils.CacheHandler;
import com.weiwend.fooldelivery.utils.SpUtils;
import com.weiwend.fooldelivery.utils.Utils;

//用于显示用户头像的控件
@SuppressLint("NewApi") 
public class MyHeadImageLoaderView extends FrameLayout{
	
	//显示头像的控件
	private ImageView mImageView = null;
	
	//上下文对象
	private Context context;
	
	//下载最新头像的异步加载类
	private AsyncDataLoader dataLoader = null;
	
	//设置头像是否显示为圆形
	private boolean isRound=true;

	public MyHeadImageLoaderView(Context context) {
		this(context,null);
	}
	
	public MyHeadImageLoaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}
	
	private void init(Context context){
		
		this.context=context;
		
		FrameLayout.LayoutParams imageViewLP = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView=new ImageView(context);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageView.setLayoutParams(imageViewLP);
		
		//初始化默认头像
		Bitmap mBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.default_head_img);
		mImageView.setImageBitmap(mBitmap);
		
		addView(mImageView);
	}
	
	//用户未登录或者退出登录时，头像恢复为默认头像
	public void logout(){
		
		Bitmap mBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.default_head_img);
		
		mImageView.setImageBitmap(mBitmap);
	}

	//访问服务器，更新本地头像
	public void updateData(Context context, String url){
		
		if(dataLoader!=null){
			dataLoader.cancel(true);
			dataLoader=null;
		}
		
		dataLoader=new AsyncDataLoader(url);
		dataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	//设置头像的url
	public void setURL(String url){
		
		String fileName=MyApplicaition.mUserName+".png";
		
		File f=new File(CacheHandler.getHeadImageCacheDir(),fileName);
		
		if(f.exists()){  //头像存在于缓存目录中，先加载
			Log.e("zyf","load image from sdcard directly......");
			
			if(isRound){   //显示圆形头像
				
				Bitmap bitmap=BitmapFactory.decodeFile(f.getAbsolutePath());
				mImageView.setImageBitmap(Utils.toRoundBitmap(bitmap));        
				
			}else{
			    mImageView.setImageURI(Uri.parse(f.getAbsolutePath()));
			}
		}else{//文件存在缓存目录中不存在
			
		}
		
		//访问服务器，判断是否需要更新头像
		updateData(context,url);
	}
	
	//设置头像
	public void setBitmap(Bitmap bitmap){
		
		if(isRound){
			Bitmap roundBitmap=Utils.toRoundBitmap(bitmap);
			mImageView.setImageBitmap(roundBitmap);
		}else{
			mImageView.setImageBitmap(bitmap);
		}
	}
	
	//设置头像
	public void setImageURI(File f){
		
		if(isRound){
			Bitmap bitmap=BitmapFactory.decodeFile(f.getAbsolutePath());
			mImageView.setImageBitmap(Utils.toRoundBitmap(bitmap));
		}else{
		    mImageView.setImageURI(Uri.parse(f.getAbsolutePath()));
		}
	}
	
	/*public void setScale(ScaleType scaleType){
		mImageView.setScaleType(scaleType);
	}
	
	public void setBackgroundResource(int resource){
		mImageView.setBackgroundResource(resource);
	}*/
	
	//连接服务器，判断是否需要更新头像
	protected class AsyncDataLoader extends AsyncTask<Object, Object, Object>{
		private String url = null;
		private Uri uri = null;
		private String fileName = null; 
		
		public AsyncDataLoader(String url){
			this.url = url;
			fileName="temp"+MyApplicaition.mUserName+".png";  //临时保存新头像文件，下载成功后，覆盖本地原来的头像文件
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			
			Log.e("zyf","AsyncDataLoader doInBackground...");
			
			File f=null;
			String fileUrl = null;
			
			if(fileName!=null){
				
				f=new File(CacheHandler.getHeadImageCacheDir(), fileName);
				
				fileUrl=f.getAbsolutePath();
				
				Log.e("zyf","fileUrl: "+fileUrl);
			}
			
			downloadImage(url,fileName);
			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			mImageView.requestFocus();
			
			String fileName=MyApplicaition.mUserName+".png";
			File f=new File(CacheHandler.getHeadImageCacheDir(),fileName);
			
			if(downloadSuccess){    //下载新头像成功，更新头像
				
				mImageView.setImageURI(null);
				
				if(isRound){
					Bitmap bitmap=BitmapFactory.decodeFile(f.getAbsolutePath());
					mImageView.setImageBitmap(Utils.toRoundBitmap(bitmap));
				}else{
				    mImageView.setImageURI(Uri.parse(f.getAbsolutePath()));
				}

			}else{     //无需更新头像
				
			}
		}
	}
	
	//标识是否需要更新新头像
	private boolean downloadSuccess;
	
	//下载新头像
	public File downloadImage(String urlStr, String filename){
		
		Log.e("zyf","download image start...");
		
		downloadSuccess=false;
		
		InputStream inStream=null;
		
		File f;
		
		f=new File(CacheHandler.getHeadImageCacheDir(),filename);
		
		HttpURLConnection con=null;
		try{
			Log.e("zyf","download image url: "+urlStr);
						
			URL url=new URL(urlStr);
			con=(HttpURLConnection)url.openConnection();
			con.setConnectTimeout(DataConfigs.TIME_OUT);
			
			inStream=con.getInputStream();
			
			Log.e("zyf","con.getContentLength(): "+con.getContentLength());
				
			f.createNewFile();

			FileOutputStream fos=new FileOutputStream(f);
			byte[] buffer=new byte[1024];
			int len=-1;
			
			while((len=inStream.read(buffer))!=-1){
				fos.write(buffer,0,len);
			}
			fos.close();
			
			//删除本地保存的原头像文件
			File file=new File(CacheHandler.getHeadImageCacheDir(),MyApplicaition.mUserName+".png");
			file.delete();
			
			//更名下载成功的新头像
			f.renameTo(new File(CacheHandler.getHeadImageCacheDir(),MyApplicaition.mUserName+".png"));
			
			//本地保存头像更新的时间，用于下一次上传给服务器端，服务器端据此判断是否返回给客户端新的头像
			SpUtils.saveHeadImgLastUpdatedTime(context, MyApplicaition.mUserName, Utils.getFormattedCurTime());
			
			downloadSuccess=true;
			
		}catch(Exception e){
			f.delete();
			Log.e("zyf","download Image exception: "+e.toString());  //服务器端没有返回给客户端新的头像信息，客户端无需更新头像
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
