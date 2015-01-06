package com.weiwend.fooldelivery.customviews;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.items.GalleryItem;
import com.weiwend.fooldelivery.utils.MyOnImageLoadSuccessListener;
import com.weiwend.fooldelivery.utils.Utils;

public class MyCycleGallery  extends FrameLayout{
	
	//控件的上下文
	private Context mContext;
	
	//切换图片的控件
	private ViewPager mViewPager;
	
	private MyAdapter mAdapter;
	
	//处理消息的控件
	private Handler mHandler;
	
	//用于判断是否需要轮播
	private boolean enableCycle;
	
	//保存轮播图的信息
	private ArrayList<GalleryItem> galleryList;
	
	//保存所有需要加载的轮播图
	private MyLoaderImageView iconImageViews[];
	
	//保存底下的点点
    private ImageView cursorImageViews[];
    
    //点点外围的容器
    private LinearLayout mCursorContainer;
    
    //每张轮播图的停留时间
    private int cycleTime;
	
    //点击单张轮播图的监听器
	public interface MyOnGalleryItemClickListener{
		void onGalleryItemClick(int position);
	}
	
	//点击单张轮播图的监听器
	private MyOnGalleryItemClickListener mOnGalleryItemClickListener;
	
	//设置点击单张轮播图的监听器
	public void setMyGalleryOnItemClickListener(MyOnGalleryItemClickListener mOnGalleryItemClickListener){
		
		this.mOnGalleryItemClickListener=mOnGalleryItemClickListener;
	}

	public MyCycleGallery(Context context) {
		this(context,null);
	}
	
	public MyCycleGallery(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
	public MyCycleGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext=context;
		
		mHandler=new Handler();
	}
	
	

	//绑定数据
	public void setItemDatas(ArrayList<GalleryItem> galleryList){
		
		this.galleryList=galleryList;
		
		if(galleryList.size()>1){  //轮播图的张数>1,开启轮播功能
			
			enableCycle=true;
			
		}else{   //轮播图的张数<1,不开启轮播功能
			
			enableCycle=false;
		}
		
		if(enableCycle){
			
			GalleryItem startItem=galleryList.get(0);    
			GalleryItem endItem=galleryList.get(galleryList.size()-1);
			
			//首末增加两项用于循环轮播
			galleryList.add(0, endItem);
			galleryList.add(startItem);
		}
		
		init();
	}

	void init(){
		
		View contentView=LayoutInflater.from(mContext).inflate(R.layout.custom_my_gallery, this);
		
		mCursorContainer=(LinearLayout)contentView.findViewById(R.id.mCursorContainer);
		
		iconImageViews=new MyLoaderImageView[galleryList.size()];
		
		if(enableCycle){
			
			cursorImageViews=new ImageView[galleryList.size()-2];   //循环轮播时，点点的数量比图片少两个
		}else{
			cursorImageViews=new ImageView[galleryList.size()];
		}
		
		MyLoaderImageView item;
		ImageView item2;
		for(int i=0;i<iconImageViews.length;i++){   //初始化轮播图图片
			
			item=new MyLoaderImageView(mContext);
			item.setURL(galleryList.get(i).getPath());
			iconImageViews[i]=item;
		}
		
		for(int i=0;i<cursorImageViews.length;i++){   //初始化点点
			
			item2=new ImageView(mContext);
			item2.setBackgroundResource(R.drawable.cursor);
			LinearLayout.LayoutParams lpLayoutParams=new LinearLayout.LayoutParams(Utils.dip2px(mContext, 8), Utils.dip2px(mContext, 8));
			lpLayoutParams.setMargins(Utils.dip2px(mContext, 8), 0, Utils.dip2px(mContext, 8), 0);
			
			cursorImageViews[i]=item2;
			
			mCursorContainer.addView(item2,lpLayoutParams);
		}
		
		mViewPager=(ViewPager)contentView.findViewById(R.id.mViewPager);
		mAdapter=new MyAdapter();
		mViewPager.setAdapter(mAdapter);
		
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				if(enableCycle){
					
					if(position==0){   //当前已经滑到最左端，通过代码设置mViewPager切换到iconImageViews.length-2位置，这样用户便可继续右滑
						
						
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								mViewPager.setCurrentItem(iconImageViews.length-2,false);
							}
							
						},300);
						
						
						position=iconImageViews.length-2;
						
					}else if(position==iconImageViews.length-1){  //当前已经滑到最右端，通过代码设置mViewPager切换到1位置，这样用户便可继续左滑
						
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								mViewPager.setCurrentItem(1,false);
							}
							
						},300);
						
						position=1;
						
					}else{
						
						if(position==iconImageViews.length-3){
							
							iconImageViews[iconImageViews.length-2].setURL(galleryList.get(iconImageViews.length-2).getPath());
							
						}else if(position==iconImageViews.length-2){
							
							iconImageViews[iconImageViews.length-1].setURL(galleryList.get(iconImageViews.length-1).getPath());
						}
						
						startCycle();
					}
					
					position--;  //轮播情况下，点点的数量比轮播图少2，因此在更新点点的状态时，position需进行自减操作
				}
					
				updateCursorStatus(position);
			
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		if(enableCycle){
			mViewPager.setCurrentItem(1);
		}else{
			mViewPager.setCurrentItem(0);
		}
	}
	
	//ViewPager的适配器
	public class MyAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			
			return iconImageViews.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			
			((ViewPager)container).removeView(iconImageViews[position]);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			
			final int pos=position;
			
			((ViewPager)container).addView(iconImageViews[position], 0); 
			
			iconImageViews[position].setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					//设置轮播图的点击事件
					if(mOnGalleryItemClickListener!=null){
						
						if(enableCycle){
							mOnGalleryItemClickListener.onGalleryItemClick(pos-1);
						}else{
							mOnGalleryItemClickListener.onGalleryItemClick(pos);
						}
					}
				}
			});
			
            return iconImageViews[position]; 
		}

		@Override
		public int getItemPosition(Object object) {
			
			if(getCount()>0){
				return POSITION_NONE;
			}
			
			return super.getItemPosition(object);
		}
    }
	
	//更新点点的状态
	void updateCursorStatus(int position){
		for(int i=0;i<cursorImageViews.length;i++){
			if(i==position){
				cursorImageViews[i].setBackgroundResource(R.drawable.cursor_active);
			}else{
				cursorImageViews[i].setBackgroundResource(R.drawable.cursor);
			}
		}
	}
	
	//开启轮播图的轮播功能
	public void startCycle(){
		
		if(enableCycle){
			mHandler.removeCallbacks(mCycleRunnable);
			cycleTime=galleryList.get(mViewPager.getCurrentItem()).getTime()*1000;
			mHandler.postDelayed(mCycleRunnable, cycleTime);
		}
	}
	
	//关闭轮播图的轮播功能
    public void stopCycle(){
		
		mHandler.removeCallbacks(mCycleRunnable);
	}
	
    //处理轮播时的图片切换
	Runnable mCycleRunnable=new Runnable() {
		
		@Override
		public void run() {
			
		    int index=mViewPager.getCurrentItem()%galleryList.size()+1;
		    
		    mViewPager.setCurrentItem(index, true);
		    
		}
	};
	
	BroadcastReceiver mUpdateImgReveiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(mAdapter!=null){
				
				for(int i=0;i<iconImageViews.length;i++){   //初始化轮播图图片
					
					MyLoaderImageView item=new MyLoaderImageView(mContext);
					item.setURL(galleryList.get(i).getPath());
					iconImageViews[i]=item;
				}
				
				mAdapter.notifyDataSetChanged();
			}
		}
	};
	
}

