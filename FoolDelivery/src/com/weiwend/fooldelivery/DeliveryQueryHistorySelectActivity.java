package com.weiwend.fooldelivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.DeliveryQueryHistoryItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.FileUtils;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class DeliveryQueryHistorySelectActivity extends BaseActivity implements OnClickListener,onDataLoaderListener{
	
	//获取用户收藏列表的异步加载类
	private AsyncTaskDataLoader mCollectQueryAsyncTaskDataLoader;
	
	//保存用户云端的收藏记录
	private ArrayList<DeliveryQueryHistoryItem> mDeliveryCollectHistoryItems=new ArrayList<DeliveryQueryHistoryItem>();
	
	//保存本地查询历史记录
	private ArrayList<DeliveryQueryHistoryItem> mDeliveryLocalHistoryItems=new ArrayList<DeliveryQueryHistoryItem>();
	
	//“查询历史”的列表Listview
	private PullToRefreshListView mPullToRefreshListView;
	
	//“查询历史”列表Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//获取用户云端收藏列表的各种状态信息
	private final int DELIVERY_COLLECT_QUERY_GETTING=5;
	private final int DELIVERY_COLLECT_QUERY_GET_SUCCESS=6;
	private final int DELIVERY_COLLECT_QUERY_GET_FAILED=7;
	
	//耗时操作时的loading对话框
    private MyProgressDialogUtils mProgressDialogUtils;
    
    //保存用户选择的查询历史位置
    private int selectPosition=-1;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {

			case DELIVERY_COLLECT_QUERY_GETTING:      //获取用户云端收藏列表的接口已经调用，正在等待服务器端的回复
							
				Log.e("zyf","delivery query history getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_query_history, DeliveryQueryHistorySelectActivity.this);
				mProgressDialogUtils.showDialog();
							
				break;
				
			case DELIVERY_COLLECT_QUERY_GET_SUCCESS:        //用户云端收藏列表获取成功
				
				Log.e("zyf","delivery query history get success......");
				
				//更新本地查询历史记录
				mDeliveryLocalHistoryItems=FileUtils.readDeliveryQueryHistory();
				for(int i=0;i<mDeliveryLocalHistoryItems.size();i++){
        			DeliveryQueryHistoryItem item=mDeliveryLocalHistoryItems.get(i);
        			for(int j=0;j<mDeliveryCollectHistoryItems.size();j++){
        				if(item.getSnum().equals(mDeliveryCollectHistoryItems.get(j).getSnum())){
        					mDeliveryCollectHistoryItems.remove(j);
        					break;
        				}
        			}
        			mDeliveryCollectHistoryItems.add(item);
        		}
                FileUtils.updateDeliveryQueryHistory(mDeliveryCollectHistoryItems);
                
                mPullToRefreshListView.onRefreshComplete();
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(DeliveryQueryHistorySelectActivity.this);
					mPullToRefreshListView.setAdapter(mBaseAdapter);
					mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							
							selectPosition=position-1;
							mBaseAdapter.notifyDataSetChanged();
							
							//历史查询记录选择完毕，跳转到之前的页面
							Intent intent=new Intent();
							intent.putExtra("snum", mDeliveryCollectHistoryItems.get(position-1).getSnum());
							setResult(ActivityResultCode.CODE_DELIVERY_HISTORY_SELECTOR, intent);
							
							finish();
						}
					});
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				MyToast.makeText(DeliveryQueryHistorySelectActivity.this, getString(R.string.query_history_get_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			case DELIVERY_COLLECT_QUERY_GET_FAILED:      //用户云端收藏列表获取失败
				
				Log.e("zyf","delivery query history get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(DeliveryQueryHistorySelectActivity.this, getString(R.string.query_history_get_failed), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_delivery_query_history_select);
		
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				Log.e("zyf","onRefresh onRefresh onRefresh...");
				
				String label = DateUtils.formatDateTime(DeliveryQueryHistorySelectActivity.this, System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){      //下拉刷新，获取用户云端收藏列表
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
					
					mCollectQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_COLLECT_LIST, url, null, null);
					mCollectQueryAsyncTaskDataLoader.setOnDataLoaderListener(DeliveryQueryHistorySelectActivity.this);
					mCollectQueryAsyncTaskDataLoader.execute();
					
				}else{
					
				}
				
			}
		});
		
		//获取用户云端收藏列表
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
		mCollectQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_COLLECT_LIST, url, null, null);
		mCollectQueryAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mCollectQueryAsyncTaskDataLoader.execute();
	}

	//初始化页面标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_query_history));
	}

	@Override
	public void start(int flag) {
		if(flag==Flag.FLAG_DELIVERY_COLLECT_LIST){      //开始获取用户云端的收藏列表
			
			Utils.sendMessage(mHandler, DELIVERY_COLLECT_QUERY_GETTING);
			
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_DELIVERY_COLLECT_LIST){      //获取用户云端收藏列表返回结果
			
			Log.e("zyf","delivery query history: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //用户云端收藏列表获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, DELIVERY_COLLECT_QUERY_GET_SUCCESS);
						
						return;
					}
					
					JSONArray deliveryInfoJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					DeliveryQueryHistoryItem deliveryQueryHistoryItem;
					
					mDeliveryCollectHistoryItems.clear();
					
				    for(int i=0;i<deliveryInfoJsonArray.length();i++){
				    	
				    	jsonObject=deliveryInfoJsonArray.getJSONObject(i);
				    	
				    	deliveryQueryHistoryItem=new DeliveryQueryHistoryItem();
				    	
				    	deliveryQueryHistoryItem.setId(jsonObject.getString("id"));
				    	deliveryQueryHistoryItem.setCid(jsonObject.getString("cid"));
				    	deliveryQueryHistoryItem.setSnum(jsonObject.getString("snum"));
				    	deliveryQueryHistoryItem.setLogo(UrlConfigs.SERVER_URL+jsonObject.getString("logo"));
				    	deliveryQueryHistoryItem.setStat(jsonObject.getString("stat"));
				    	deliveryQueryHistoryItem.setName(jsonObject.getString("name"));
				    	deliveryQueryHistoryItem.setStm(jsonObject.getString("stm"));
				    	
				    	mDeliveryCollectHistoryItems.add(deliveryQueryHistoryItem);
				    }
				    
				    Utils.sendMessage(mHandler, DELIVERY_COLLECT_QUERY_GET_SUCCESS);
				    
				    return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, DELIVERY_COLLECT_QUERY_GET_FAILED);      //用户云端收藏列表获取失败
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.leftBtn:      //标题栏的返回功能
			finish();
			break;

		default:
			break;
		}
	}
	
	//“查询历史”列表Listview的适配器
    class MyBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mDeliveryCollectHistoryItems.size();
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
			
			final DeliveryQueryHistoryItem item=mDeliveryCollectHistoryItems.get(position);
			
			final int pos=position;
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_delivery_query_history_select, null);
			}
			
			TextView mDeliveryInfoTv = (TextView) convertView.findViewById(R.id.mDeliveryInfoTv);
			TextView mStatusTv = (TextView) convertView.findViewById(R.id.mStatusTv);
			TextView mOrderTimeTv = (TextView) convertView.findViewById(R.id.mOrderTimeTv);
			MyLoaderImageView mLoaderImageView= (MyLoaderImageView) convertView.findViewById(R.id.mLoaderImageView);
			ImageView mSelectView=(ImageView)convertView.findViewById(R.id.mSelectView);
			
			if(pos==selectPosition){
				mSelectView.setBackgroundResource(R.drawable.selector_active);
			}else{
				mSelectView.setBackgroundResource(R.drawable.selector_normal);
			}
			
			mDeliveryInfoTv.setText(item.getName()+"  "+item.getSnum());
			
			if(item.getStat()!=null){
				mStatusTv.setText(getString(R.string.status)+item.getStat());
			}else{
				mStatusTv.setText(getString(R.string.status));
			}
			
			if(item.getStm()!=null){
				mOrderTimeTv.setText(getString(R.string.order_time)+" "+item.getStm());
			}else{
				mOrderTimeTv.setText(getString(R.string.order_time)+" ");
			}
			
			
			mLoaderImageView.setURL(item.getLogo());
			
			return convertView;
			
		}
    }
}
