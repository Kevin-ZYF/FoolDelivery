package com.weiwend.fooldelivery;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyAppUpdatePromptDialog;
import com.weiwend.fooldelivery.customviews.MyAppUpdatePromptDialog.MySubmmitListener;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.CacheHandler;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class SettingActivity extends BaseActivity implements OnClickListener,onDataLoaderListener{
	
	//"消息提醒设置"、"软件更新"、"关于"
	private RelativeLayout mContainer1,mContainer2,mContainer3;
	
	//用于检查版本更新的异步加载类
	private AsyncTaskDataLoader mCheckVersionAsyncTaskDataLoader;
	
	//服务器端apk的版本号、版本描述、下载链接
	private String newVersion,newDesc,newPath;
	
	//调用检查更新接口后的各种状态信息
	private final int VERSION_CHECKING=0;
	private final int VERSION_NEED_UPDATE=1;
	private final int VERSION_NEED_NOT_UPDATE=2;
	private final int VERSION_CHECK_FAILED=3;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//根据各种状态信息更新ui等
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case VERSION_CHECKING:   //检查更新的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","version checking......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_check_version, SettingActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case VERSION_NEED_UPDATE:   //检查更新成功，提示用户需要更新当前的app
				
				Log.e("zyf","need update version......");
				
				mProgressDialogUtils.dismissDialog();
				
				/*new AlertDialog.Builder(SettingActivity.this)
				.setMessage(getString(R.string.prompt_need_update_version))
				.setCancelable(false)
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.setPositiveButton(getString(R.string.summit), new DialogInterface.OnClickListener() {
					@SuppressWarnings("static-access")
					public void onClick(DialogInterface dialog, int whichButton) {
						
						new Thread(){

							@Override
							public void run() {
								
								downloadFile(SettingActivity.this, newPath, new File(CacheHandler.getDownloadCacheDir(),"new.apk"));
							}
							
						}.start();
						
					}
				}).show();*/
				
				final MyAppUpdatePromptDialog mAppUpdatePromptDialog=new MyAppUpdatePromptDialog(SettingActivity.this, R.style.MyDialog,getString(R.string.prompt_need_update_version));
				mAppUpdatePromptDialog.setMySubmmitListener(new MySubmmitListener() {
					
					@Override
					public void summit(String content) {
						
						mAppUpdatePromptDialog.dismiss();
						
						new Thread(){

							@Override
							public void run() {
								
								downloadFile(SettingActivity.this, newPath, new File(CacheHandler.getDownloadCacheDir(),"new.apk"));
							}
							
						}.start();
					}
				});
				mAppUpdatePromptDialog.show();
            	
				break;
            case VERSION_NEED_NOT_UPDATE:    //检查更新成功，不需要更新当前的版本
				
				Log.e("zyf","need not update version......");
				
				MyToast.makeText(SettingActivity.this, getString(R.string.check_version_need_not_update), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case VERSION_CHECK_FAILED:      //检查更新失败
				
				Log.e("zyf","version check failed......");
				
				MyToast.makeText(SettingActivity.this, getString(R.string.check_version_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_setting);
		
		initViews();
	}
	
	//初始化ui
	private void initViews(){
		mContainer1=(RelativeLayout)findViewById(R.id.mContainer1);
		mContainer2=(RelativeLayout)findViewById(R.id.mContainer2);
		mContainer3=(RelativeLayout)findViewById(R.id.mContainer3);
		
		mContainer1.setOnClickListener(this);
		mContainer2.setOnClickListener(this);
		mContainer3.setOnClickListener(this);
	}

	//初始化页面的标题信息
	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.system_settings));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.mContainer1:   //消息提醒设置
			
			Intent intent=new Intent(this,SettingMessageAlertActivity.class);
			startActivity(intent);
			
			break;
		case R.id.mContainer2:   //软件更新
			
			if(mCheckVersionAsyncTaskDataLoader!=null){
				mCheckVersionAsyncTaskDataLoader.canceled();
			}
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CHECK_VERSION_URL+"?version="+Utils.getVersion(this);
			mCheckVersionAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_CHECK_VERSION, url, null, null);
			mCheckVersionAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mCheckVersionAsyncTaskDataLoader.execute();
			
			break;
		case R.id.mContainer3:   //关于
			
			Intent intent2=new Intent(this,AboutUsActivity.class);
			startActivity(intent2);
			break;
		default:
			break;
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_CHECK_VERSION){    //开始进行软件更新
			
			Utils.sendMessage(mHandler, VERSION_CHECKING);
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_CHECK_VERSION){   //获取版本更新返回结果
			
			Log.e("zyf","version check result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //版本更新检测成功
					
                    JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					String ver=dataJsonObject.getString("ver");   //服务器端的版本号
					
					if(ver.equals(Utils.getVersion(SettingActivity.this))){    //本地app版本号与服务器端的版本号一致，不需要进行版本更新
						
						Log.e("zyf","no version update......");
						
						Utils.sendMessage(mHandler, VERSION_NEED_NOT_UPDATE);
						
					}else{      //本地app版本号与服务器端的版本号不一致，需要进行版本更新
						
						newVersion=dataJsonObject.getString("ver");
						newDesc=dataJsonObject.getString("des");
						newPath=dataJsonObject.getString("path");
						
						Utils.sendMessage(mHandler, VERSION_NEED_UPDATE);
					}
					
					return;
					
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, VERSION_CHECK_FAILED);  //版本更新检测失败
		}
	}
	
	//下载最新版本的app，下载完毕后自动弹出app安装界面
	public void downloadFile(Context context,String downloadUrl, File saveFile) {
		
		Log.e("zyf","start downloading......");
		
        long downLoadFileSize=0;
        
        int notificationId=0;
        
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher,getString(R.string.version_checking), System.currentTimeMillis());
        
        Intent intent = new Intent();
        intent.setAction(ActivityResultCode.ACTION_CHECK_VERSION);
        intent.putExtra("path", saveFile.getAbsolutePath());
        
        PendingIntent pendingIntent  = PendingIntent.getBroadcast(context, 0, intent, 0);  
        notification.contentIntent = pendingIntent;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        RemoteViews rv=new RemoteViews(getPackageName(), R.layout.custom_notification);  
        rv.setTextViewText(R.id.mStatusTv, getString(R.string.downloading));  
        notification.contentView = rv;
   
        notificationManager.notify(notificationId, notification);
         
        try {  
        	
            URL url = new URL(downloadUrl);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
          
            conn.setConnectTimeout(DataConfigs.TIME_OUT);
            conn.setReadTimeout(DataConfigs.TIME_OUT);
           
            InputStream is = conn.getInputStream();
            
            RandomAccessFile  fos =  new RandomAccessFile(saveFile,"rw");
		    fos.seek(downLoadFileSize);
		    
		    byte[] buffer = new byte[1024];
		    int len = 0;
		    int i=0;
		    while((len = is.read(buffer)) != -1){
		    	fos.write(buffer, 0, len);
		    	downLoadFileSize += len;
		    }
		    fos.close();  
            is.close();
            
            rv.setTextViewText(R.id.mStatusTv, getString(R.string.download_success));
            notificationManager.notify(notificationId, notification);
            
            Utils.installApp(SettingActivity.this, saveFile);
            
            Log.e("zyf","download success......");
            
        } catch (Exception e) {
        	
        	Log.e("zyf","download exception: "+e.toString());
        	
        	if(saveFile.delete()){
        		Log.e("zyf","download failed,delete the saved file success......");
        	}else{
        		Log.e("zyf","download failed,delete the saved file failed......");
        	}
        	
        	rv.setTextViewText(R.id.mStatusTv, getString(R.string.download_failed));
            notificationManager.notify(notificationId, notification);
        } 
    }

}
