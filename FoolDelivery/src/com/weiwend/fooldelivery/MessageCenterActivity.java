package com.weiwend.fooldelivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.weiwend.fooldelivery.customviews.MyTabView;
import com.weiwend.fooldelivery.customviews.MyTabView.MyOnTabClickLister;
import com.weiwend.fooldelivery.customviews.MyTitleTabView;
import com.weiwend.fooldelivery.customviews.MyTitleTabView.MyOnTitleClickLister;
import com.weiwend.fooldelivery.fragments.SystemMessageFragment;
import com.weiwend.fooldelivery.fragments.SuggestMessageFragment;

public class MessageCenterActivity extends BaseActivity implements OnClickListener,MyOnTitleClickLister,MyOnTabClickLister{
	
	//“系统消息”、“投诉建议消息”切换控件
	private MyTitleTabView mTitleTabView;;
	
	//页面底部tab控件
	private MyTabView mTabView;
	
	//“系统消息”fragment
	private SystemMessageFragment mSystemMessageFragment;
	
	//“投诉建议消息”fragment
	private SuggestMessageFragment mSuggestMessageFragment;
	
	//页面底部tab控件的小图标
	private int mTabViewNormalIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};
	private int mTabViewActiveIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_message_center);
		
		initViews();
	}
	
	//初始化ui
	private void initViews(){
		
		mTitleTabView=(MyTitleTabView)findViewById(R.id.mTitleTabView);
		mTitleTabView.setTabNames(getString(R.string.message_system), getString(R.string.message_suggest));
		mTitleTabView.setOnTitleClickListener(this);
		mTitleTabView.setCurItem(0);
		
		mTabView=(MyTabView)findViewById(R.id.mTabView);
		mTabView.setDatas(mTabViewNormalIcons, mTabViewActiveIcons);
		mTabView.setOnTabClickListener(this);
		mTabView.setCurItem(3);    //页面底部tab控件定位到当前位置
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
		mTitleView.setText(getString(R.string.title_message_center));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:     //标题栏的返回功能
			finish();
			break;
	    case R.id.userCenterBtn:    //标题栏右侧的“用户中心”快捷入口
				
			Intent intent=new Intent(this, UserCenterActivity.class);
			startActivity(intent);
			
			finish();
			break;

		default:
			break;
		}
	}
	
	//用来记录用户之前点击的是“系统消息”还是“投诉建议消息”
	private int preSelectedIndex=-1;

	//MyTitleTabView控件切换时的回调函数
	@Override
	public void OnTitleClick(int index) {
		
		if(preSelectedIndex==index){   //用户重复点击，不处理
			return;
		}
		
        preSelectedIndex=index;
 		
		FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
		
		hideAllFragments(transaction);
		
		switch (index) {
		
		case 0:    //切换到“系统消息”
			
			if(mSystemMessageFragment==null){
				
				mSystemMessageFragment=new SystemMessageFragment();
				
				transaction.add(R.id.contentLayout, mSystemMessageFragment);  
				
			}else{
				transaction.show(mSystemMessageFragment);
			}
			break;
			
		case 1:    //切换到“投诉建议消息”
			
			if(mSuggestMessageFragment==null){
				
				mSuggestMessageFragment=new SuggestMessageFragment();
				
				transaction.add(R.id.contentLayout, mSuggestMessageFragment);  
			}else{
				transaction.show(mSuggestMessageFragment);
			}
			break;
		default:
			break;
		}
		
		transaction.commit();
	}
	
	//隐藏已经加入到Container中的fragment，用于缓存处理
	private void hideAllFragments(FragmentTransaction transaction){
		
		if(mSystemMessageFragment!=null){
			
			transaction.hide(mSystemMessageFragment);
		}
		
		if(mSuggestMessageFragment!=null){
			
			transaction.hide(mSuggestMessageFragment);
		}
	}

	//页面底部MyTabView控件点击回调函数
	@Override
	public void OnTabClick(int choice) {
		
		switch (choice) {
		case 0:   //回首页
			finish();
			break;
		case 1:   //回查件界面
			
			Intent intent=new Intent(this,QueryActivity.class);
			startActivity(intent);
			
			finish();
			break;
		case 2:   //回寄件页面
			
			Intent intent2=new Intent(this,SenderActivity.class);
			startActivity(intent2);
			
			finish();
			
			break;
		case 3:   //回消息页面，即当前界面
			break;
		default:
			break;
		}
	}

}
