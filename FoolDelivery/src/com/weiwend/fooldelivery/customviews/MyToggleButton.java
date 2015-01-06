package com.weiwend.fooldelivery.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.weiwend.fooldelivery.R;

//自定义ToggleButton
public class MyToggleButton extends View {
	
	private boolean open=false;   //记录是否处于打开状态
	
	private int button_width;   //按钮的宽度
	private int button_height;    //按钮的高度
	
	//按钮开、关切换时的监听器
	private OnToggleClickListener mOnToggleClickListener;
	
	//按钮开、关切换时的监听器
	public interface OnToggleClickListener
	{
		void onClick(boolean open);
	}
	
	//设置按钮开、关切换时的监听器
	public void setOnToggleClickListener(OnToggleClickListener onToggleClickListener)
	{
		mOnToggleClickListener=onToggleClickListener;
	}

	public MyToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		button_width=w;
		button_height=h;
	}

	//更新按钮的界面显示
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Bitmap bitmap=null;
		
		if(open)
		{
			bitmap=(Bitmap)BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.toggle_off));
		}else
		{
			bitmap=(Bitmap)BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.toggle_on));
		}
		
		int bitmap_width=bitmap.getWidth();
		int bitmap_height=bitmap.getHeight();
		
		canvas.drawBitmap(bitmap, new Rect(0,0,bitmap_width,bitmap_height), new Rect(0,0,button_width,button_height), null);
	}

	//处理用户触摸设置按钮的开关状态
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(event.getAction()==MotionEvent.ACTION_UP)
		{
			if(open)
			{
				open=false;
			}else
			{
				open=true;
			}
			
			invalidate();
			
			if(mOnToggleClickListener!=null)
			{
				mOnToggleClickListener.onClick(open);
			}
		}
		return true;
	}
	
	//设置按钮的开关状态
	public void setToggleStatus(boolean b)
	{
		open=b;
		invalidate();
	}
	
	//获取按钮的开关状态
	public boolean getToggleStatus(){
		
		return open;
		
	}
	
}
