package com.weiwend.fooldelivery.customviews;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.items.GalleryItem;
import com.weiwend.fooldelivery.utils.Utils;

//显示轮播图的控件
public class MyGallery extends FrameLayout {
	
	//用于加载自定义布局对象
	private LayoutInflater mInflater;
	
	//上下文对象
	private Context mContext;
	
	//用于轮播图的左右滑动
	private ViewPager mViewPager;
	
	//保存轮播图的云端地址
	private ArrayList<String> iconUrls;
	
	//保存轮播图对象
	private MyLoaderImageView iconImageViews[];
	
	//保存底部的游标对象
    private ImageView cursorImageViews[];
    
    //底部游标的容器
    private LinearLayout mCursorContainer;
	
    //轮播图变化时的监听器
	public interface MyOnItemSelectedChangedListener{
		void handleSelectedChanged(int item);
	}
	
	//轮播图点击时的监听器
	public interface MyOnItemSelectedListener{
		void handleSelected(int item);
	}
	
	//轮播图变化时的监听器
	private MyOnItemSelectedChangedListener mOnItemSelectedChangedListener;
	
	//轮播图点击时的监听器
	private MyOnItemSelectedListener mOnItemSelectedListener;
	
	//设置轮播图变化时的监听器
	public void setMyOnItemSelectedChangedListener(MyOnItemSelectedChangedListener mOnItemSelectedChangedListener){
		this.mOnItemSelectedChangedListener=mOnItemSelectedChangedListener;
	}
	
	//设置轮播图点击时的监听器
	public void setMyOnItemSelectedListener(MyOnItemSelectedListener mOnItemSelectedListener){
		this.mOnItemSelectedListener=mOnItemSelectedListener;
	}

	public MyGallery(Context context) {
		this(context,null);
	}
	
	public MyGallery(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
	public MyGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext=context;
		mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

	}
	
	//初始化轮播图的信息
	public void setDatas(ArrayList<String> a){
		iconUrls=a;
		
		init();
	}
	
	//初始化轮播图的信息
	public void setItemDatas(ArrayList<GalleryItem> galleryList){
		iconUrls=new ArrayList<String>();
		for(int i=0;i<galleryList.size();i++){
			iconUrls.add(galleryList.get(i).getPath());
		}
		init();
	}
	
	//初始化ui
	void init(){
		View contentView=mInflater.inflate(R.layout.custom_my_gallery, this);
		
		mCursorContainer=(LinearLayout)contentView.findViewById(R.id.mCursorContainer);
		
		iconImageViews=new MyLoaderImageView[iconUrls.size()];
		
		cursorImageViews=new ImageView[iconUrls.size()];
		
		MyLoaderImageView item;
		ImageView item2;
		for(int i=0;i<iconUrls.size();i++){  
			
			//初始化轮播图对象
			item=new MyLoaderImageView(mContext);
			item.setURL(iconUrls.get(i));
			iconImageViews[i]=item;
			
			// 初始化底部的游标对象
			item2=new ImageView(mContext);
			item2.setBackgroundResource(R.drawable.cursor);
			LinearLayout.LayoutParams lpLayoutParams=new LinearLayout.LayoutParams(Utils.dip2px(mContext, 8), Utils.dip2px(mContext, 8));
			lpLayoutParams.setMargins(Utils.dip2px(mContext, 8), 0, Utils.dip2px(mContext, 8), 0);
			
			cursorImageViews[i]=item2;
			
			mCursorContainer.addView(item2,lpLayoutParams);
		}
		
		mViewPager=(ViewPager)contentView.findViewById(R.id.mViewPager);
		mViewPager.setAdapter(new MyAdapter());
		
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				//更新底部游标的状态
				setCursorStatus(position);
				
				//回调轮播图变化时的处理函数
				if(mOnItemSelectedChangedListener!=null){
					mOnItemSelectedChangedListener.handleSelectedChanged(position);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		setCursorStatus(0);   //初始化状态时，默认选中第一个游标
	}
	
	//用于显示轮播图mViewPager的适配器
	public class MyAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			
			return iconUrls.size();
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
			
			iconImageViews[position].setOnClickListener(new View.OnClickListener() {   //设置指定轮播图的点击事件
				
				@Override
				public void onClick(View arg0) {
					if(mOnItemSelectedListener!=null){    //回调轮播图点击时的处理函数
						mOnItemSelectedListener.handleSelected(pos);
					}
				}
			});
			
            return iconImageViews[position]; 
		}
    }
	
	//设置选中指定的轮播图
	public void setSelection(int position){
		
		if(position<0||position>=iconUrls.size())
			return;
		
		mViewPager.setCurrentItem(position,true);
		setCursorStatus(position);
	}
	
	//设置底部游标的显示状态
	void setCursorStatus(int position){
		for(int i=0;i<cursorImageViews.length;i++){
			if(i==position){
				cursorImageViews[i].setBackgroundResource(R.drawable.cursor_active);
			}else{
				cursorImageViews[i].setBackgroundResource(R.drawable.cursor);
			}
		}
	}
}
