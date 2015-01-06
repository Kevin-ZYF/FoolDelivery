package com.weiwend.fooldelivery.customviews;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.weiwend.fooldelivery.R;

//自定义Toast
public class MyToast extends Toast {
	
	private static Toast mToast;
	  
    public MyToast(Context context) {  
        super(context);  
    }  
      
    public static Toast makeText(Context context, boolean original, CharSequence text, int duration) {
    	
    	if(mToast!=null){
    		mToast.cancel();
    	}
    	
    	mToast = new Toast(context);  
          
        //获取LayoutInflater对象  
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        //由layout文件创建一个View对象  
        View layout;
        
        if(original){  //系统默认的风格
        	layout = inflater.inflate(R.layout.custom_my_toast_original, null);
        }else{
        	layout = inflater.inflate(R.layout.custom_my_toast, null); 
        } 
          
        //实例化ImageView和TextView对象  
        TextView mMsgTv = (TextView) layout.findViewById(R.id.mMsgTv);  
        
        mMsgTv.setText(text);  
          
        mToast.setView(layout);   
        mToast.setDuration(duration);  
          
        return mToast;  
    }
    
    public static Toast makeText(Context context, CharSequence text, int duration) {  
        
    	return makeText(context, false, text, duration);
    	
    }
    
    //消失
    public static void dismiss(){
    	if(mToast!=null){
    		mToast.cancel();
    	}
    }
  
}  