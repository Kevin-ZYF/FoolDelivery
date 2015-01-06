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
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.DeliveryCompanyItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class DeliveryCompanySelectActivity extends BaseActivity implements OnClickListener,onDataLoaderListener{
	
	//快递公司列表Listview
	private PullToRefreshListView mPullToRefreshListView;
	
	//快递公司列表Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//保存从服务器端获取的快递公司列表
	private ArrayList<DeliveryCompanyItem> deliveryCompanyItems=new ArrayList<DeliveryCompanyItem>();
	
	//调用获取快递公司列表接口后的各种状态信息
	private final int DELIVERY_COMPANY_GETTING=0;
	private final int DELIVERY_COMPANY_GET_SUCCESS=1;
	private final int DELIVERY_COMPANY_GET_FAILED=2;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
            case DELIVERY_COMPANY_GETTING:      //获取快递公司列表的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","delivery company list getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_delivery_company, DeliveryCompanySelectActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_COMPANY_GET_SUCCESS:      //快递公司列表获取成功
				
				Log.e("zyf","delivery company list get success......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(DeliveryCompanySelectActivity.this);
					
					mPullToRefreshListView.setAdapter(mBaseAdapter);
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				MyToast.makeText(DeliveryCompanySelectActivity.this, getString(R.string.delivery_company_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case DELIVERY_COMPANY_GET_FAILED:      //快递公司列表获取失败
				
				Log.e("zyf","delivery company list get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
                MyToast.makeText(DeliveryCompanySelectActivity.this, getString(R.string.delivery_company_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	 //获取快递公司列表的异步加载类
	private AsyncTaskDataLoader mGetAllCompanyAsyncTaskDataLoader;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_delivery_company_select);
		
		initViews();
		
	    //获取快递公司列表
		mGetAllCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DELIVERY_COMPANY, UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COMPANY_URL, null, null);
		mGetAllCompanyAsyncTaskDataLoader.setOnDataLoaderListener(this);
		
		mGetAllCompanyAsyncTaskDataLoader.execute();
	}
	
	private void initViews(){
		
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				Log.e("zyf","onRefresh onRefresh onRefresh...");
				
				String label = DateUtils.formatDateTime(DeliveryCompanySelectActivity.this, System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){      //下拉刷新
					
					mGetAllCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DELIVERY_COMPANY, UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COMPANY_URL, null, null);
					mGetAllCompanyAsyncTaskDataLoader.setOnDataLoaderListener(DeliveryCompanySelectActivity.this);
					
					mGetAllCompanyAsyncTaskDataLoader.execute();
					
				}else{
					
				}
				
			}
		});
		mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				//选择快递公司完毕，返回之前的界面
				Intent intent=new Intent();
				intent.putExtra("DeliveryCompanyItem", deliveryCompanyItems.get(position-1));
				setResult(ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR,intent);
				finish();
			}
		});
		
	}

	//初始化页面标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_delivery_company_select));
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
	
	//快递公司列表Listview的适配器
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
			return deliveryCompanyItems.size();
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
				
				convertView=mInflater.inflate(R.layout.item_delivery_company_selector, null);
				
				mViewHolder=new ViewHolder();
				
				mViewHolder.mTitleView=(TextView) convertView.findViewById(R.id.mTitleView);
				mViewHolder.mLoaderImageView=(MyLoaderImageView) convertView.findViewById(R.id.mLoaderImageView);
				
				convertView.setTag(mViewHolder);
				
 			}else{
 				
 				mViewHolder=(ViewHolder) convertView.getTag();
 			}
			
			DeliveryCompanyItem item;
			item=deliveryCompanyItems.get(position);
			
			/*TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			MyLoaderImageView mLoaderImageView=(MyLoaderImageView)convertView.findViewById(R.id.mLoaderImageView);*/
			
			mViewHolder.mTitleView.setText(item.getName());
			mViewHolder.mLoaderImageView.setURL(item.getLogo());
			
			return convertView;
		}	
	}
    
    class ViewHolder{
    	
    	TextView mTitleView;
    	MyLoaderImageView mLoaderImageView;
    }

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_DELIVERY_COMPANY){      //开始获取快递公司列表
			
			Log.e("zyf","get delivery company.....");
			
			Utils.sendMessage(mHandler, DELIVERY_COMPANY_GETTING);
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_DELIVERY_COMPANY){      //获取快递公司列表返回结果
			
			Log.e("zyf","company list: "+result);
			
			try {
				
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //快递公司列表获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray deliveryCompJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					DeliveryCompanyItem deliveryCompanyItem;
					deliveryCompanyItems.clear();
				    for(int i=0;i<deliveryCompJsonArray.length();i++){
				    	jsonObject=deliveryCompJsonArray.getJSONObject(i);
				    	
				    	deliveryCompanyItem=new DeliveryCompanyItem();
				    	deliveryCompanyItem.setId(jsonObject.getString("id"));
				    	deliveryCompanyItem.setName(jsonObject.getString("name"));
				    	deliveryCompanyItem.setTelp(jsonObject.getString("telp"));
				    	deliveryCompanyItem.setDesp(jsonObject.getString("desp"));
				    	deliveryCompanyItem.setLogo(UrlConfigs.SERVER_URL+jsonObject.getString("logo"));
				    	
				    	deliveryCompanyItems.add(deliveryCompanyItem);
				    }
				    
				    Utils.sendMessage(mHandler, DELIVERY_COMPANY_GET_SUCCESS);
				    
				    return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, DELIVERY_COMPANY_GET_FAILED);      //快递公司列表获取失败
		}
	}

}
