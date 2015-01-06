package com.weiwend.fooldelivery.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

import com.weiwend.fooldelivery.items.DeliveryQueryHistoryItem;

public class FileUtils {
	
	//用户本地查询记录文件
	private static String FILE_NAME_QUERY_HISTORY="queryhistory.out";   
    
	//读取本地的历史查询记录
    public static ArrayList<DeliveryQueryHistoryItem> readDeliveryQueryHistory() {
    	
		File sdFile = new File(CacheHandler.getUserInfoCacheDir(), FILE_NAME_QUERY_HISTORY);  
		          
	    try {  
	        FileInputStream fis=new FileInputStream(sdFile); 
	        ObjectInputStream ois = new ObjectInputStream(fis);  
	        ArrayList<DeliveryQueryHistoryItem> deliveryQueryHistoryList = (ArrayList<DeliveryQueryHistoryItem>)ois.readObject();
	        
	        ois.close();
	        
	        return deliveryQueryHistoryList;
	    }catch (Exception e) {  
	        Log.e("zyf","object file read ecxeption: "+e.toString()); 
	    }
		        
		return new ArrayList<DeliveryQueryHistoryItem>();
	}
	
    //更新本地的历史查询记录
    public static void updateDeliveryQueryHistory(ArrayList<DeliveryQueryHistoryItem> deliveryQueryHistoryList){
    	
    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            
            File sdFile = new File(CacheHandler.getUserInfoCacheDir(), FILE_NAME_QUERY_HISTORY);     
       
	        try{  
	             FileOutputStream fos = new FileOutputStream(sdFile);  
	             ObjectOutputStream oos = new ObjectOutputStream(fos);  
	             oos.writeObject(deliveryQueryHistoryList);
	             fos.close(); 
	         }catch (Exception e) {  
	             Log.e("zyf","write file exception: "+e.toString());
	         }
    	}
    }
	
}
