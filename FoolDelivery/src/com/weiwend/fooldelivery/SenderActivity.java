package com.weiwend.fooldelivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.weiwend.fooldelivery.customviews.MySenderTitleTabView;
import com.weiwend.fooldelivery.customviews.MySenderTitleTabView.MyOnNewTitleClickLister;
import com.weiwend.fooldelivery.customviews.MyTabView;
import com.weiwend.fooldelivery.customviews.MyTabView.MyOnTabClickLister;
import com.weiwend.fooldelivery.customviews.MyTitleTabView;
import com.weiwend.fooldelivery.customviews.MyTitleTabView.MyOnTitleClickLister;
import com.weiwend.fooldelivery.fragments.SenderFragment;
import com.weiwend.fooldelivery.fragments.SenderHistoryFragment;

public class SenderActivity extends BaseActivity implements OnClickListener,MyOnTabClickLister,MyOnNewTitleClickLister{
	
	//页面底部tab控件
	private MyTabView mTabView;
	
	//private MyTitleTabView mSenderTitleView;
	
	//“新寄件”、“历史寄件”的切换控件
	private MySenderTitleTabView mSenderTitleView2;
	
	//页面底部tab控件的小图标
	private int mTabViewNormalIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};
	private int mTabViewActiveIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};
	
	//“新寄件”fragment
	private SenderFragment mSenderFragment;
	
	//“历史寄件”fragment
	private SenderHistoryFragment mSenderHistoryFragment;
	
	//用来记录其他页面调用该页面时的用户选择，以此来初始化界面  0:新寄件  1:历史寄件
	private int selectedIndex;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_sender);
		
		selectedIndex=getIntent().getIntExtra("selectedIndex", 0);
		
		mTabView=(MyTabView)findViewById(R.id.mTabView);
		mTabView.setDatas(mTabViewNormalIcons, mTabViewActiveIcons);
		
		/*mSenderTitleView=(MyTitleTabView)findViewById(R.id.mSenderTitleView);
		mSenderTitleView.setOnTitleClickListener(this);
		mSenderTitleView.setCurItem(selectedIndex);*/
		
		mSenderTitleView2=(MySenderTitleTabView)findViewById(R.id.mSenderTitleView2);
		mSenderTitleView2.setOnTitleClickListener(this);
		mSenderTitleView2.setCurItem(selectedIndex);
		
		mTabView=(MyTabView)findViewById(R.id.mTabView);
		mTabView.setDatas(mTabViewNormalIcons, mTabViewActiveIcons);
		mTabView.setOnTabClickListener(this);
		mTabView.setCurItem(2);     //页面底部tab控件定位到当前位置
	}

	//初始化当前页面的标题
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		leftBtn.setVisibility(View.INVISIBLE);
		
		Button userCenterBtn=(Button)findViewById(R.id.userCenterBtn);
		userCenterBtn.setOnClickListener(this);
		userCenterBtn.setVisibility(View.VISIBLE);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_sender));
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			
			finish();
			break;
        case R.id.userCenterBtn:      //标题栏右侧的“用户中心”快捷入口
			
			Intent intent5=new Intent(this, UserCenterActivity.class);
			startActivity(intent5);
			
			finish();
			break;

		default:
			break;
		}
	}


	/*@Override
	public void OnTitleClick(int index) {
		
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
		
		hideFragments(transaction);
		
		switch (index) {
		case 0:
			if(mSenderFragment==null){
				mSenderFragment=new SenderFragment();
				
				transaction.add(R.id.contentLayout, mSenderFragment);  
			}else{
				transaction.show(mSenderFragment);
			}
			break;
		case 1:
			if(mSenderHistoryFragment==null){
				mSenderHistoryFragment=new SenderHistoryFragment();
				
				transaction.add(R.id.contentLayout, mSenderHistoryFragment);  
			}else{
				transaction.show(mSenderHistoryFragment);
			}		
			break;
		default:
			break;
		}
		
		transaction.commit();
	}*/
	
	//隐藏已经加入到Container中的fragment，用于缓存处理
	private void hideFragments(FragmentTransaction transaction){
			
		if(mSenderFragment!=null){
			transaction.hide(mSenderFragment);
		}
		
		if(mSenderHistoryFragment!=null){
			transaction.hide(mSenderHistoryFragment);
		}	
	}

	//页面底部MyTabView控件点击回调函数
	@Override
	public void OnTabClick(int choice) {
		
		switch (choice) {
		case 0:      //跳转到首页
			finish();
			break;
		case 1:      //跳转到查件界面
			
			Intent intent=new Intent(this,QueryActivity.class);
			startActivity(intent);
			
			finish();
			break;
		case 2:      //跳转到寄件界面，即当前界面
			break;
		case 3:      //跳转到消息界面
			
			Intent intent2=new Intent(this,MessageCenterActivity.class);
			startActivity(intent2);
			
			finish();
			break;
		default:
			break;
		}
	}

	//MyNewTitleTabView控件切换时的回调函数
	@Override
	public void OnNewTitleClick(int index) {
		
		FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
		
		hideFragments(transaction);
		
		switch (index) {
		
		case 0:      //切换到“新寄件”
			
			if(mSenderFragment==null){
				mSenderFragment=new SenderFragment();
				
				transaction.add(R.id.contentLayout, mSenderFragment);  
			}else{
				transaction.show(mSenderFragment);
			}
			break;
			
		case 1:      //切换到“历史寄件”
			
			if(mSenderHistoryFragment==null){
				mSenderHistoryFragment=new SenderHistoryFragment();
				
				transaction.add(R.id.contentLayout, mSenderHistoryFragment);  
			}else{
				transaction.show(mSenderHistoryFragment);
			}		
			break;
		default:
			break;
		}
		
		transaction.commit();
	}

}
