package com.weiwend.fooldelivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyPhonePromptDialog;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.NearbyItem;
import com.weiwend.fooldelivery.utils.DistanceUtil;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class NearbyActivity extends BaseActivity implements AMapLocationListener,onDataLoaderListener,android.view.View.OnClickListener{
	
	//显示附近网点信息的listview
    private PullToRefreshListView mPullToRefreshListView;
	
    //附近网点Listview的适配器
    private MyBaseAdapter mBaseAdapter;
	
    //用于获取附近网点的异步加载类
	private AsyncTaskDataLoader mNearbyAsyncTaskDataLoader;
	
	//保存从服务器端获取的附近网点信息
	private ArrayList<NearbyItem> nearbyItems=new ArrayList<NearbyItem>();
	
	//高德地图定位类
	private LocationManagerProxy mLocationManagerProxy;	
	
	//保存定位返回的经纬度
	private double lon,lat;
	
	//调用附近网点信息接口后的各种状态信息
	private final int NEARBY_GETTING=1;
	private final int NEARBY_GET_SUCCESS=2;
	private final int NEARBY_GET_FAILED=3;
	
	//调用高德定位接口后的各种状态信息
	private final int ADDRESS_MATCHING=4;
	private final int ADDRESS_MATCH_SUCCESS=5;
	private final int ADDRESS_MATCH_FAILED=6;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            
			case NEARBY_GETTING:      //获取附近网点信息的接口已经调用，正在等待服务器端的回复
							
				Log.e("zyf","nearby getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_nearby, NearbyActivity.this);
				mProgressDialogUtils.showDialog();
							
				break;
			case NEARBY_GET_SUCCESS:      //获取附近网点信息获取成功
				
				Log.e("zyf","nearby get success......");
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(NearbyActivity.this);
					mPullToRefreshListView.setAdapter(mBaseAdapter);
					mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							
						}
					});
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				mPullToRefreshListView.onRefreshComplete();
				
				
				MyToast.makeText(NearbyActivity.this, getString(R.string.nearby_get_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
			case NEARBY_GET_FAILED:      //获取附近网点信息获取失败
				
				Log.e("zyf","nearby get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(NearbyActivity.this, getString(R.string.nearby_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			case ADDRESS_MATCHING:      //高德定位的接口已经调用，正在等待定位结果
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_location, NearbyActivity.this);
				
				//用户可以取消定位操作
				mProgressDialogUtils.setProgressDialogCancelable();
				mProgressDialogUtils.setMyProgressDialogCanceledListener(new MyProgressDialogUtils.MyProgressDialogCanceledListener() {
					
					@Override
					public void canceled() {
						
						Log.e("zyf","cancel cancel cancel......");
						
						if(mLocationManagerProxy!=null){
							mLocationManagerProxy.removeUpdates(NearbyActivity.this);
							mLocationManagerProxy.destroy();
						}
					}
				});
				mProgressDialogUtils.showDialog();
				
				break;
			case ADDRESS_MATCH_SUCCESS:      //定位成功
				
				MyToast.makeText(NearbyActivity.this, getString(R.string.location_match_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
							
				break;
			case ADDRESS_MATCH_FAILED:      //定位失败
				
				MyToast.makeText(NearbyActivity.this, getString(R.string.location_match_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
	    setContentView(R.layout.activity_nearby);
	    
	    initViews();
	    
	    mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.setGpsEnable(false);
		
		Utils.sendMessage(mHandler, ADDRESS_MATCHING);
		
		mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, NearbyActivity.this);   //开始定位
	}
	
	private void initViews(){
		
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				Log.e("zyf","onRefresh onRefresh onRefresh...");
				
				String label = DateUtils.formatDateTime(NearbyActivity.this, System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){      //下拉刷新
					
					if(lon!=0){    //之前已成功定位，所以直接上传经纬度至服务器获取附近网点信息
						
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_NEARBY_URL+"?lon="+lon+"&lat="+lat;
						mNearbyAsyncTaskDataLoader=new AsyncTaskDataLoader(true,Flag.FLAG_NEARBY,url,null,null);
						mNearbyAsyncTaskDataLoader.setOnDataLoaderListener(NearbyActivity.this);
						
						mNearbyAsyncTaskDataLoader.execute();
						
					}else{      //之前定位失败，所以首先进行定位，定位成功后，上传经纬度至服务器获取附近网点信息
						
						Utils.sendMessage(mHandler, ADDRESS_MATCHING);
						mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, NearbyActivity.this);
					}
					
				}else{
					
				}
				
			}
		});
	}

	//初始化当前页面的标题
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_nearby));
	}
	
	//附近网点信息Listview的适配器
    class MyBaseAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;

		public MyBaseAdapter(Context mContext) {
			super();
			
			mInflater=LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return nearbyItems.size();
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
			
			final NearbyItem item=nearbyItems.get(position);
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_nearby, null);
			}
			
			MyLoaderImageView mLoaderImageView=(MyLoaderImageView)convertView.findViewById(R.id.mLoaderImageView);
			
			TextView mWnameTv=(TextView)convertView.findViewById(R.id.mWnameTv);
			TextView mAddrTv=(TextView)convertView.findViewById(R.id.mAddrTv);
			TextView mDistanceTv=(TextView)convertView.findViewById(R.id.mDistanceTv);
			
			LinearLayout mRouteContainer=(LinearLayout)convertView.findViewById(R.id.mRouteContainer);
			LinearLayout mPhoneContainer=(LinearLayout)convertView.findViewById(R.id.mPhoneContainer);
			
			mLoaderImageView.setURL(item.getLogo());
			mWnameTv.setText(item.getWname());
			mAddrTv.setText(item.getAddr());
			mDistanceTv.setText(item.getDistance()+" km");
			
			mPhoneContainer.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					MyPhonePromptDialog mDialog=new MyPhonePromptDialog(NearbyActivity.this, R.style.MyDialog, item.getTelp());
					mDialog.setMySubmmitListener(new MyPhonePromptDialog.MySubmmitListener() {
						
						@Override
						public void summit(String phoneNumber) {
							
							Intent phoneIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+phoneNumber));
							startActivity(phoneIntent);
						}
					});
					mDialog.show();
				}
			});
			
			mRouteContainer.setOnClickListener(new View.OnClickListener() {      //实现路径规划
				
				@Override
				public void onClick(View arg0) {
					
					Intent intent=new Intent(NearbyActivity.this,RouteActivity.class);
					intent.putExtra("FromLat", lat);
					intent.putExtra("FromLon", lon);
					intent.putExtra("ToLat", item.getLat());
					intent.putExtra("ToLon", item.getLon());
					startActivity(intent);
				}
			});
			
			return convertView;
		}
		
	}

    //AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_NEARBY){
			
			Utils.sendMessage(mHandler, NEARBY_GETTING);      //开始获取附近网点信息
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_NEARBY){      //获取附近网点信息返回结果
			
            Log.e("zyf","nearby: "+result);
			
			try {
				
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //附近网点信息获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, NEARBY_GET_SUCCESS);
						
						return;
					}
					
					JSONArray deliveryInfoJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					NearbyItem item;
					
					nearbyItems.clear();
					
				    for(int i=0;i<deliveryInfoJsonArray.length();i++){
				    	
				    	jsonObject=deliveryInfoJsonArray.getJSONObject(i);
				    	
				    	item=new NearbyItem();
				    	
				    	item.setCid(jsonObject.getString("cid"));
				    	item.setCname(jsonObject.getString("cname"));
				    	item.setWid(jsonObject.getString("wid"));
				    	item.setWname(jsonObject.getString("wname"));
				    	item.setLogo(UrlConfigs.SERVER_URL+jsonObject.getString("logo"));
				    	item.setAddr(jsonObject.getString("addr"));
				    	item.setTelp(jsonObject.getString("telp"));
				    	item.setLat(jsonObject.getDouble("lat"));
				    	item.setLon(jsonObject.getDouble("lon"));
				    	item.setDistance(jsonObject.getDouble("dis"));
				    	item.setDesp(jsonObject.getString("desp"));
				    	
				    	nearbyItems.add(item);
				    }
				    
				    Utils.sendMessage(mHandler, NEARBY_GET_SUCCESS);
				    
				    return;
				}
			} catch (Exception e) {
				
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, NEARBY_GET_FAILED);      //附近网点信息获取失败
		}
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	//高德地图定位的回调函数
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		
        if(amapLocation!=null&&amapLocation.getAMapException().getErrorCode() == 0) {      //定位成功
        	
        	Utils.sendMessage(mHandler, ADDRESS_MATCH_SUCCESS);
        	
        	Log.e("zyf","Longitude: "+amapLocation.getLongitude());
        	Log.e("zyf","Latitude: "+amapLocation.getLatitude());
        	
        	lon=amapLocation.getLongitude();
        	lat=amapLocation.getLatitude();
			
        	//上传经纬度获取附近网点信息
        	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_NEARBY_URL+"?lon="+lon+"&lat="+lat;
			mNearbyAsyncTaskDataLoader=new AsyncTaskDataLoader(true,Flag.FLAG_NEARBY,url,null,null);
			mNearbyAsyncTaskDataLoader.setOnDataLoaderListener(NearbyActivity.this);
			
			mNearbyAsyncTaskDataLoader.execute();
			
		}else{
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);
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

}
