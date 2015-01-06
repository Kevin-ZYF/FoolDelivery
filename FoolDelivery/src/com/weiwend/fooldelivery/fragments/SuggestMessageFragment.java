package com.weiwend.fooldelivery.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.weiwend.fooldelivery.MyApplicaition;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.UserLoginActivity;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.SuggestionItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class SuggestMessageFragment extends Fragment implements onDataLoaderListener{
	
	//调用“投诉建议”接口后的各种状态信息
	private final int SUGGESTION_GETTING=1;
	private final int SUGGESTION_GET_SUCCESS=2;
	private final int SUGGESTION_GET_FAILED=3;
	
	//耗时操作的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            
			case SUGGESTION_GETTING:    //获取投诉建议列表的接口已经调用，正在等待服务器端的回复
							
				Log.e("zyf","suggestion getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_suggestion, getActivity());
				mProgressDialogUtils.showDialog();
							
				break;
			case SUGGESTION_GET_SUCCESS:    //投诉建议列表获取成功
				
				Log.e("zyf","suggestion get success......");
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(getActivity());
					mPullToRefreshListView.setAdapter(mBaseAdapter);
					mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
													
							/*Intent intent=new Intent(getActivity(),QueryResultActivity.class);
							intent.putExtra("id", mDeliveryQueryHistoryItems.get(position-1).getId());
							startActivity(intent);*/
						}
					});
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				mPullToRefreshListView.onRefreshComplete();
				
				
				MyToast.makeText(getActivity(), getString(R.string.suggestion_get_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
			case SUGGESTION_GET_FAILED:    //投诉建议列表获取失败
				
				Log.e("zyf","suggestion get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(getActivity(), getString(R.string.suggestion_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//保存投诉建议列表
	private ArrayList<SuggestionItem> suggestionItems=new ArrayList<SuggestionItem>();
	
	//投诉建议Listview
	private PullToRefreshListView mPullToRefreshListView;
	
	//投诉建议Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//获取投诉建议的异步加载类
	private AsyncTaskDataLoader mSuggestionAsyncTaskDataLoader;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View contentView=inflater.inflate(R.layout.fragment_suggest_message, container, false);
		
		mPullToRefreshListView=(PullToRefreshListView)contentView.findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){ //下拉刷新
					
					//获取投诉建议列表
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SUGGESTION_LIST+"?sesn="+MyApplicaition.sesn;
					mSuggestionAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_SUGGESTION, url, null, null);
					mSuggestionAsyncTaskDataLoader.setOnDataLoaderListener(SuggestMessageFragment.this);
					mSuggestionAsyncTaskDataLoader.execute();
					
				}else{
					
				}
				
			}
		});
		
		//获取投诉建议列表
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SUGGESTION_LIST+"?sesn="+MyApplicaition.sesn;
		mSuggestionAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_SUGGESTION, url, null, null);
		mSuggestionAsyncTaskDataLoader.setOnDataLoaderListener(SuggestMessageFragment.this);
		mSuggestionAsyncTaskDataLoader.execute();
		
		return contentView;
	}
	
	//投诉建议Listview的适配器
    class MyBaseAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;

		public MyBaseAdapter(Context mContext) {
			super();
			
			mInflater=LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return suggestionItems.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			
			final ViewHolder mViewHolder;
			
			if(convertView==null){
				
				convertView=mInflater.inflate(R.layout.item_suggestion, null);
				
				mViewHolder=new ViewHolder();
				
				mViewHolder.mSnumTv=(TextView)convertView.findViewById(R.id.mSnumTv);
				mViewHolder.mContentTv=(TextView)convertView.findViewById(R.id.mContentTv);
				
				convertView.setTag(mViewHolder);
				
			}else{
				mViewHolder=(ViewHolder) convertView.getTag();
			}
			
			/*TextView mSnumTv=(TextView)convertView.findViewById(R.id.mSnumTv);
			TextView mContentTv=(TextView)convertView.findViewById(R.id.mContentTv);*/
			
			mViewHolder.mSnumTv.setText(getString(R.string.delivery_number)+" "+suggestionItems.get(position).getSnum());
			mViewHolder.mContentTv.setText(getString(R.string.suggestion_content)+" "+suggestionItems.get(position).getContent());
			
			return convertView;
		}
		
	}
    
    class ViewHolder{
    	
    	TextView mSnumTv;
    	TextView mContentTv;
    }

    //AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_SUGGESTION){    //开始获取投诉建议列表
			
			Utils.sendMessage(mHandler, SUGGESTION_GETTING);
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_SUGGESTION){   //获取投诉建议列表返回结果
			
			try {
				
				Log.e("zyf","suggestion: "+result);
				
				JSONObject totalJsonObject=new JSONObject(result);						
				
				String rc=totalJsonObject.getString("rc");
				
				Log.e("zyf: ","rc: "+rc);
				
				if("0".equals(rc)){    //投诉建议列表获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, SUGGESTION_GET_SUCCESS);
						
						return;
					}
					
					JSONArray suggetionJSONArray=dataJsonObject.getJSONArray("items");
					
					suggestionItems.clear();
					
					SuggestionItem item;
					JSONObject jsonObject;
					
					for(int i=0;i<suggetionJSONArray.length();i++){
						
						item=new SuggestionItem();
						
						jsonObject=suggetionJSONArray.getJSONObject(i);
						
						item.setType(jsonObject.getInt("typ"));
						item.setSnum(jsonObject.getString("snum"));
						item.setContent(jsonObject.getString("cont"));
						
						suggestionItems.add(item);
						
					}
		            
		            Utils.sendMessage(mHandler, SUGGESTION_GET_SUCCESS);
		            
		            return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(getActivity(), UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, SUGGESTION_GET_FAILED);   //投诉建议列表获取失败
			
            return;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
        super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_LOGIN){
			
			if(data!=null){
				
				if(data.getBooleanExtra("loginStatus", false)){   //用户登录成功，重新获取投诉建议列表
					
					Log.e("zyf","login status : true");
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SUGGESTION_LIST+"?sesn="+MyApplicaition.sesn;
					mSuggestionAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_SUGGESTION, url, null, null);
					mSuggestionAsyncTaskDataLoader.setOnDataLoaderListener(SuggestMessageFragment.this);
					mSuggestionAsyncTaskDataLoader.execute();
				}
			}
		}
	}
}
