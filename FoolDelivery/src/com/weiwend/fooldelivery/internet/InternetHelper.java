package com.weiwend.fooldelivery.internet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

import com.weiwend.fooldelivery.configs.DataConfigs;

public class InternetHelper {
	
	//用于post方式的HttpClient
	private HttpClient mPostHttpClient;
	
	//用于get方式的HttpURLConnection
	private HttpURLConnection mHttpURLConnection;
	
	//get方式从服务器端获取数据
	public String getContentFromServer(String url){		
		
		URL mUrl;
		mHttpURLConnection=null;
		try {
			mUrl=new URL(url);
			mHttpURLConnection=(HttpURLConnection) mUrl.openConnection(); 
			mHttpURLConnection.setConnectTimeout(DataConfigs.TIME_OUT);
			mHttpURLConnection.setReadTimeout(DataConfigs.TIME_OUT);
			InputStream inputStream = mHttpURLConnection.getInputStream();

			if(mHttpURLConnection.getResponseCode()==200)
			{
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
		        byte[] data = new byte[1024];  
		        int count = -1;  
		        while((count = inputStream.read(data,0,1024)) != -1)  
		            outStream.write(data, 0, count);
		        String datas=new String(outStream.toByteArray());		
		        
		        return datas;
			}else{
				Log.e("zyf","get content bad response code: "+mHttpURLConnection.getResponseCode());
				
				return null;
			}
		}catch(Exception e){
			Log.e("zyf","get content from server exception: "+e.toString());
		}finally{
			if(mHttpURLConnection!=null){
				mHttpURLConnection.disconnect();
				mHttpURLConnection=null;
			}
		}
		return null;
	}
	
	//post方式上传内容至服务器，params主要为“参数名-参数值”的键值对
	public String postContentToServer(String url,HashMap<Object, Object> params){
		
		mPostHttpClient=null;
		
		HttpPost httpPost = new HttpPost(url);
 
        HttpResponse httpResponse = null;
        try{
        	mPostHttpClient=new DefaultHttpClient();
        	HttpConnectionParams.setConnectionTimeout(mPostHttpClient.getParams(), DataConfigs.TIME_OUT);
        	HttpConnectionParams.setSoTimeout(mPostHttpClient.getParams(), DataConfigs.TIME_OUT);
        	
        	if(params!=null){
    	        List<NameValuePair> paramValuePairs = new ArrayList<NameValuePair>(); 
    	        Iterator iter = params.entrySet().iterator();
    	        while (iter.hasNext()) {  
    	            Map.Entry entry = (Map.Entry) iter.next();  
    	            String key = (String) entry.getKey();  
    	            String val = (String) entry.getValue();
    	            
    	            paramValuePairs.add(new BasicNameValuePair(key, val)); 
    	        }
    	        
    	        httpPost.setEntity(new UrlEncodedFormEntity(paramValuePairs, HTTP.UTF_8));
    		}
            
            httpResponse = mPostHttpClient.execute(httpPost); 
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
            	Log.e("zyf","post content success...");
            	
            	InputStream inputStream = httpResponse.getEntity().getContent();

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
		        byte[] data = new byte[1024];  
		        int count = -1;  
		        while((count = inputStream.read(data,0,1024)) != -1)  
		            outStream.write(data, 0, count);
		        String datas=new String(outStream.toByteArray());		     
		        
		        return datas;
		        
            } 
        }catch (Exception e) { 
            Log.e("zyf","post exception: "+e.toString());
        }finally{
        	
        	if(mPostHttpClient!=null){
        		mPostHttpClient.getConnectionManager().shutdown();
        		mPostHttpClient=null;
        	}
        }
		return null;
	}
	
	//post方式上传内容至服务器，content为Json格式的字符串
	public String postContentToServer(String url,String content){
		
		StringEntity entity=null;
		
		mPostHttpClient=null;
		
		try {
			entity=new StringEntity(content, HTTP.UTF_8);
			entity.setContentType("text/xml");
			HttpPost request = new HttpPost(url);
			request.addHeader("Content-Type", "text/xml; charset=utf-8");
			request.setEntity(entity);

			mPostHttpClient=new DefaultHttpClient();
			mPostHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,DataConfigs.TIME_OUT);
			mPostHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,DataConfigs.TIME_OUT);

			HttpResponse httpResponse = null;
			
			httpResponse=mPostHttpClient.execute(request);
			
			Log.e("zyf","post StatusCode: "+httpResponse.getStatusLine().getStatusCode());
			
			if (httpResponse.getStatusLine().getStatusCode()==200) {
				
            	Log.e("zyf","post content success...");
            	
            	InputStream inputStream = httpResponse.getEntity().getContent();

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
		        byte[] data = new byte[1024];  
		        int count = -1;  
		        while((count = inputStream.read(data,0,1024)) != -1)  
		            outStream.write(data, 0, count);
		        String datas=new String(outStream.toByteArray());		     
		        
		        return datas;
		        
            }
			
		}catch (Exception e) {
			Log.e("zyf","post exception: "+e.toString());
		}finally{
		   if(mPostHttpClient!=null){
			   mPostHttpClient.getConnectionManager().shutdown();
			   mPostHttpClient=null;
		   }
	   }
		
	   return null;
	}
	
	//关闭get方式的网络连接
	public void closeGetConnection(){
		if(mHttpURLConnection!=null){
			mHttpURLConnection.disconnect();
			mHttpURLConnection=null;
		}
	}
	
	//关闭post方式的网络连接
    public void closePostConnection(){
		if(mPostHttpClient!=null){
			mPostHttpClient.getConnectionManager().shutdown();
			mPostHttpClient=null;
		}
	}
}
