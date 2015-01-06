package com.weiwend.fooldelivery.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.configs.DataConfigs;


public class Utils {
	
	//判断字符创是否是中文字符串
	public static boolean isStrChinese(String str){
		
		char c=str.charAt(0);
		
		if(c>=0x0391&&c<=0xFFE5){ 
			return true;
		}
		return false;
	}
	
	//handler对象发送消息函数
	public static void sendMessage(Handler handler,int what){
		Message msg=new Message();
		msg.what=what;
		handler.sendMessage(msg);
	}
	
	//通过给定的url获取bitmap对象
	public static Bitmap getRemoteImage(final URL url) { 
	    try { 
	        final URLConnection conn = url.openConnection(); 
	        conn.connect(); 
	        final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream()); 
	        final Bitmap bm = BitmapFactory.decodeStream(bis); 
	        bis.close();      
	        return bm; 
	    } catch (IOException e){
	    	
	    }
	    
	    return null; 
	}
	
	//将对定的字符串转化为utf-8格式
	public static String parseToUtf8String(String str){
		try {
			
			return URLEncoder.encode(str,"UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			Log.e("zyf","parseToUtf8String: "+e.toString());
		}
		
		return "";
	}
	
	//获取当前格式化后的时间
	public static  String getFormattedCurTime(){
		long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();  
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        
        Date d1=new Date(time);
        
        String t1=format.format(d1);
        
        Log.e("t1: ", t1);
        
        return t1;
	}
	
	//“马上寄出”、“今天”、“明天”、“后天”所对应的日期
	public static  String[] getFuture4Date(){
		
		long time=System.currentTimeMillis();  
		
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        
        long a=60*60*24*1000;
        
        Date d1=new Date(time);
        Date d2=new Date(time+a);
        Date d3=new Date(time+a*2);
        
        String t1=format.format(d1);
        String t2=format.format(d2);
        String t3=format.format(d3);
        
        String dates[]=new String[4];
        dates[0]=t1;
        dates[1]=t1;
        dates[2]=t2;
        dates[3]=t3;
        
        return dates;
	}
	
	//返回今天用户还可以预约取件的整点时间（6:00-18:00之间）
	public static String[] getAfterTime(){
		int len;
		String times[];
		
		Time t=new Time();
		t.setToNow();
		
		int curHour=t.hour;
		
		if(curHour<6||curHour>18){
			Log.e("zyf","cur hour: "+t.hour+"当前已经不可以寄送快递......");
			
			times=new String[0];
			
		}else{
			len=18-curHour;
			
			times=new String[len];
			
			for(int i=0;i<len;i++){
				times[i]=curHour+i+1+":00";
			}
		}
		
		for(int i=0;i<times.length;i++){
			Log.e("zyf","time"+i+" :"+times[i]);
		}
		
		return times;
	}
	
	//判断用户今天是否还可以选择预约取件时间
	public static boolean isTodayAvailable(){
		
		Time t=new Time();
		t.setToNow();
		
		int curHour=t.hour;
		
		Log.e("zyf","curHour: "+curHour);
		
		if(curHour<6||curHour>=18){
			return false;
		}
		
		return true;
	}
	
	//将dip转化为px
	public static int dip2px(Context context, float dipValue){ 
        float density = context.getResources().getDisplayMetrics().density; 
        return (int)(dipValue*density + 0.5f);
    }
	
	//解决Listview在ScrollView显示异常的问题
	public static void setListViewHeightBasedOnChildren(ListView listView) {  
        ListAdapter listAdapter=listView.getAdapter();   
        if(listAdapter==null){  
            return;  
        }  

        int totalHeight=0;  
        for (int i=0;i<listAdapter.getCount();i++) {  
            View listItem=listAdapter.getView(i,null,listView);  
            listItem.measure(0,0);  
            totalHeight+=listItem.getMeasuredHeight();  
        }  

        ViewGroup.LayoutParams params=listView.getLayoutParams();  
        params.height=totalHeight+(listView.getDividerHeight()*(listAdapter.getCount()-1));  
        listView.setLayoutParams(params);  
    }
	
	//获取本应用的版本号
	public static String getVersion(Context context){
		
		try{
			
			 PackageManager pm=context.getPackageManager();
			 PackageInfo info=pm.getPackageInfo(context.getPackageName(), 0);
			 
			 return info.versionName;
		}catch(Exception e){
			
			Log.e("zyf","exception: "+e.toString());
			
			return "0.0";
		}
	}
	
	//调用系统界面安装指定的apk文件
	public static void installApp(Context context, File file){
		if(file.exists()){
			Intent intent = new Intent();
		    intent.setAction(android.content.Intent.ACTION_VIEW);
		    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(intent);
		}else{
			Log.e("zyf","file is not existed......");
		}
	}
	
	//用于分享查询结果时，将快递助手的logo预先保存至sdcard中，为之后的分享做准备
	public static void saveShareImage(Context context){
		
		File file=new File(CacheHandler.getShareCacheDir(),DataConfigs.SHARE_IMAGE_FILE_NAME);
		
		try {
			if(!file.exists()){
				
				file.createNewFile();
				
				FileOutputStream fos=new FileOutputStream(file);
				Bitmap mBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.share_logo);
				
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				
				Log.e("zyf","share image save success...");
			}else{
				Log.e("zyf","share image exists...");
				
				return;
			}
		} catch (Exception e) {
			Log.e("zyf","save share image exception: "+e.toString());
		}
	}
	
	/*//根据给出的快递单号，返回模糊匹配成功的快递公司列表
	public static String[] getExpressNoForRule(String number)
	{
		int length=number.length();
		
		if(length<14||length>18){
			
			if(length==8||length==9){   //长度为8或者9
				
				return new String[]{"dp"};
				
			}else if(length==10){     //长度为10
				
				if(number.matches("^(23[1-7]|610|611|710).*")
						||number.matches("^(230|50[2-5]).*")
						||number.matches("^(37[3-6]|317|322|323|327|329|330|460|466|48[0-2]|489|803|856|860|869).*")){
					
					return new String[]{"gt","zjs","yt"};
					
				}else if(number.matches("^(119|12[0-9]|130).*")){
					
					return new String[]{"gt"};
					
				}else{
					
					return new String[]{"yt"};
				}
			}else if(length==11){    //长度为11
				
				if(number.matches("^\\d.*")){
					
					return new String[]{"jd"};
					
				}else{
					
					return new String[]{"ups"};
					
				}
			}else if(length==12){    //长度为12
				
				if(number.matches("^\\d{3}.*"))    //前三位为数字
				{
				
					if(number.matches("^(268|368|468|568|668|868|888|900).*")){
						
						return new String[]{"sto"};
						
					}else if(number.matches("^(358|518|618|7[1-3]8|751|76[1-3]|778).*")){
						
						return new String[]{"zt"};
						
					}else if(number.matches("^(0(?!00)|11[34678]|131|199|20[3-6]|302|575|59[14]|660|730|756|90[3-5]|966).*")){
						
						return new String[]{"sf"};
						
					}else if(number.matches("^(2[158]0|420).*")){
						
						return new String[]{"ht"};
						
					}else if(number.matches("^(560|580|776).*")){
						
						return new String[]{"tt"};
						
					}else if(number.matches("^(300|340|370|710).*")){
						
						return new String[]{"qf"};
						
					}else if(number.matches("^768.*")){
						
						return new String[]{"sto","zt"};
						
					}else if(number.matches("^220.*")){
						
						return new String[]{"sto","ht"};
						
					}else if(number.matches("^(701|660|757).*")){
	
						return new String[]{"zt","sf"};
						
					}else if(number.matches("^350.*")){
	
						return new String[]{"ht","qf"};
						
					}else if(number.matches("^(310|510).*")){
	
						return new String[]{"sf","ht"};
						
					}else if(number.matches("^(550|886|530).*")){
						
						return new String[]{"tt","sf","kj"};
						
					}else if(number.matches("^688.*")){
						
						return new String[]{"sto","sf","zt"};
						
					}else if(number.matches("^000.*")){
						
						return new String[]{"jd"};
						
					}else if(number.matches("^370.*")){
						
						return new String[]{"sf","qf"};
						
					}else if(number.matches("^500.*")){
						
						return new String[]{"se","ht"};
						
					}else if(number.matches("^880.*")){
						
						return new String[]{"se","kj"};
						
					}else if(number.matches("^9[89]0.*")){
						
						return new String[]{"kj"};
						
					}else{
						
						return new String[]{"sf","tt","ht"};
						
					}
				}else if(number.matches("^[a-zA-Z]{3}.*")){   //前三位为字母
					
					return new String[]{"yt"};
					
				}else{

					return new String[]{"sf","tt","ht"};
					
				}
			}else if(length==13){
				
				if(number.matches("^\\d{2}.*")){   //前两位为数字
					
					if(number.matches("^\\d{3}.*")){  //前三位为数字
						
						if(number.matches("^(1[2345679]0|2[02]0|310|5[02]0|660|8[08]0|900).*")){
							
							return new String[]{"yd"};
							
						}else if(number.matches("^(1[02][1-9]|11[012456789]).*")){
							
							return new String[]{"ems"};
							
						}else if(number.matches("^(99[0-9]).*")){
							
							return new String[]{"post"};
							
						}else if(number.matches("^[15]00.*")){
							
							return new String[]{"yd","ems"};
							
						}else if(number.matches("^113.*")){
							
							return new String[]{"ems","rfd"};
							
						}else if(number.matches("^([3579]13).*")){
							
							return new String[]{"rfd","yd","ems"};
							
						}else if(number.matches("^901.*")){
							
							return new String[]{"jd"};
							
						}else{
							
							return new String[]{"yd","ems","post"};
							
						}
						
					}else{

						return new String[]{"yd","ems","post"};
						
					}
					
				}else if(number.matches("^.{11}[a-zA-Z]{2}.*")){

					return new String[]{"ems"};
					
				}else{
					
					return new String[]{"post"};
					
				}
			}
		}else{
			
			if(number.matches("^(1Z|1z).*")){
				
				return new String[]{"ups"};
				
			}else{
				
				return new String[]{"rfd"};
				
			}
		}
		
		return null;
		
	}
	
	//快递公司的英文简称到中文全称的映射
	public static String getExpressNoStr(String paramString)
	  {
	    String str = "";
	    if (paramString.equals("dhl"))
	      str = "DHL";
	    do
	    {
	      if (paramString.equals("yt"))
	        return "圆通";
	      if (paramString.equals("sto"))
	        return "申通";
	      if (paramString.equals("zt"))
	        return "中通";
	      if (paramString.equals("yd"))
	        return "韵达";
	      if (paramString.equals("dp"))
	        return "德邦";
	      if (paramString.equals("ems"))
	        return "EMS";
	      if (paramString.equals("gt"))
	        return "国通";
	      if (paramString.equals("ht"))
	        return "汇通";
	      if (paramString.equals("hq"))
	        return "汇强";
	      if (paramString.equals("kj"))
	        return "快捷";
	      if (paramString.equals("lb"))
	        return "龙邦";
	      if (paramString.equals("qy"))
	        return "全一";
	      if (paramString.equals("qf"))
	        return "全峰";
	      if (paramString.equals("qrt"))
	        return "全日通";
	      if (paramString.equals("rfd"))
	        return "如风达";
	      if (paramString.equals("sf"))
	        return "顺丰";
	      if (paramString.equals("se"))
	        return "速尔";
	      if (paramString.equals("tt"))
	        return "天天";
	      if (paramString.equals("zjs"))
	        return "宅急送";
	      if (paramString.equals("post"))
	        return "邮政";
	      if ((paramString.equals("fedexInter")) || (paramString.equals("fedex")))
	        return "联邦";
	      if (paramString.equals("jd"))
	        return "京东";
	      if (paramString.equals("lht"))
	        return "联昊通";
	      if (paramString.equals("nd"))
	        return "能达";
	      if (paramString.equals("ups"))
	        return "UPS";
	      if (paramString.equals("ys"))
	        return "优速";
	      if (paramString.equals("zy"))
	        return "增益";
	      if (paramString.equals("dy"))
	        return "大洋";
	      if (paramString.equals("wx"))
	        return "万象";
	      if (paramString.equals("sad"))
	        return "赛澳递";
	      if (paramString.equals("yc"))
	        return "远长";
	    }while (!paramString.equals("kr"));
	    
	    return "宽容";
	  }*/
	
	//根据给出的bitmap，返回圆形的bitmap
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		final int color = Color.parseColor("#00FF00");

		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) { //高大于宽
		roundPx = width / 2;
		left = 0;
		top = 0;
		right = width;
		bottom = width;
		height = width;
		dst_left = 0;
		dst_top = 0;
		dst_right = width;
		dst_bottom = width;
		} else { //宽大于高
		roundPx = height / 2;
		float clip = (width - height) / 2;
		left = clip;
		right = width - clip;
		top = 0;
		bottom = height;
		width = height;
		dst_left = 0;
		dst_top = 0;
		dst_right = height;
		dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);// 设置画笔无锯齿

		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
		paint.setColor(color);
		paint.setDither(true);


		canvas.drawCircle(roundPx, roundPx, roundPx, paint);//画圆
		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

		return output;
	}

}
