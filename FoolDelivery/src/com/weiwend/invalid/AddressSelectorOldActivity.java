package com.weiwend.invalid;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.weiwend.fooldelivery.BaseActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.R.id;
import com.weiwend.fooldelivery.R.layout;
import com.weiwend.fooldelivery.R.string;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyDeletableBtn;
import com.weiwend.fooldelivery.customviews.MyDeletableBtn.MyOnDeletedClickLister;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AreaItem;
import com.weiwend.fooldelivery.items.HotCityItem;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

//*****************************该界面由于需求问题，暂时已经作废*********************
public class AddressSelectorOldActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOnDeletedClickLister{

	private GridView mProvinceGridView,mCityGridView,mDistrictGridView;
	
	private MyDeletableBtn mProvinceBtn,mCityBtn,mDistrictBtn;
	
	private LinearLayout mSearchContainer;
	
	private GridView mHotCityGridView;
	private ArrayList<HotCityItem> hotCityItems=new ArrayList<HotCityItem>();
	private MyHotCityBaseAdapter mHotCityBaseAdapter;
	
	private ArrayList<AreaItem> addressItemList=new ArrayList<AreaItem>();
	
	private ArrayList<AreaItem> provinceItemList=new ArrayList<AreaItem>();
	private ArrayList<AreaItem>cityItemList=new ArrayList<AreaItem>();
	private ArrayList<AreaItem>districtItemList=new ArrayList<AreaItem>();
	
	private String pid,cid,did;
	private String pName,cName,dName;
	
	private MyProvinceBaseAdapter mProvinceBaseAdapter;
	private MyCityBaseAdapter mCityBaseAdapter;
	private MyDistrictBaseAdapter mDistrictBaseAdapter;
	
	private final int HOT_CITY_GETTIING=1;
	private final int HOT_CITY_GET_SUCCESS=2;
	private final int HOT_CITY_GET_FAILED=3;
	
	private final int ADDRESS_PROVINCE_GETTING=4;
	private final int ADDRESS_PROVINCE_GET_SUCCESS=5;
	private final int ADDRESS_PROVINCE_GET_FAILED=6;
	
	private final int ADDRESS_CITY_GETTING=7;
	private final int ADDRESS_CITY_GET_SUCCESS=8;
	private final int ADDRESS_CITY_GET_FAILED=9;
	
	private final int ADDRESS_DISTRICT_GETTING=10;
	private final int ADDRESS_DISTRICT_GET_SUCCESS=11;
	private final int ADDRESS_DISTRICT_GET_FAILED=12;
	
	private AsyncTaskDataLoader mAddressAsyncTaskDataLoader,mHotCityAsyncTaskDataLoader;
	
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case HOT_CITY_GETTIING:
            	
                Log.e("zyf","get hot city......");
                
                mProgressDialogUtils.showDialog();

            	break;
			
            case HOT_CITY_GET_SUCCESS:
            	
                Log.e("zyf","get hot city success......");
                
                mProvinceGridView.setVisibility(View.GONE);
                mCityGridView.setVisibility(View.GONE);
                mDistrictGridView.setVisibility(View.GONE);
                
                mHotCityGridView.setVisibility(View.VISIBLE);
				
				if(mHotCityBaseAdapter==null){
					mHotCityBaseAdapter=new MyHotCityBaseAdapter(AddressSelectorOldActivity.this);
					mHotCityGridView.setAdapter(mHotCityBaseAdapter);
				}else{
					mHotCityBaseAdapter.notifyDataSetChanged();
				}
				
				mProgressDialogUtils.dismissDialog();
				
            	break;
            case HOT_CITY_GET_FAILED:
            	
            	Log.e("zyf","get hot city failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	break; 
            case ADDRESS_PROVINCE_GETTING:
            	
            	//mTitleView.setText(getString(R.string.title_province));
            	
            	Log.e("zyf","get hot city failed......");
            	
            	mProgressDialogUtils.showDialog();
            	break;
            case ADDRESS_PROVINCE_GET_SUCCESS:
            	
            	Log.e("zyf","province get success......");
            	
            	mTitleView.setText(getString(R.string.title_province));
            	
            	if(mProvinceBaseAdapter==null){
            		mProvinceBaseAdapter=new MyProvinceBaseAdapter(AddressSelectorOldActivity.this);
            		mProvinceGridView.setAdapter(mProvinceBaseAdapter);
            	}else{
            		mProvinceBaseAdapter.notifyDataSetChanged();
            	}
            	
            	mProvinceGridView.setVisibility(View.VISIBLE);
            	mCityGridView.setVisibility(View.GONE);
            	mDistrictGridView.setVisibility(View.GONE);
            	
            	/*mProvinceBtn.setVisibility(View.VISIBLE);
            	mCityBtn.setVisibility(View.GONE);
            	mDistrictBtn.setVisibility(View.GONE)*/;
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
            case ADDRESS_PROVINCE_GET_FAILED:
            	
            	Log.e("zyf","province get failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
            case ADDRESS_CITY_GETTING:
            	
            	//mTitleView.setText(getString(R.string.title_city));
            	
            	Log.e("zyf","province get failed......");
            	
            	mProgressDialogUtils.showDialog();
            	break;
            case ADDRESS_CITY_GET_SUCCESS:
            	
                Log.e("zyf","city get success......");
                
                mTitleView.setText(getString(R.string.title_city));
            	
            	if(mCityBaseAdapter==null){
            		mCityBaseAdapter=new MyCityBaseAdapter(AddressSelectorOldActivity.this);
            		mCityGridView.setAdapter(mCityBaseAdapter);
            	}else{
            		mCityBaseAdapter.notifyDataSetChanged();
            	}
            	
            	mProvinceGridView.setVisibility(View.GONE);
            	mCityGridView.setVisibility(View.VISIBLE);
            	mDistrictGridView.setVisibility(View.GONE);
            	
            	/*mProvinceBtn.setVisibility(View.VISIBLE);
            	mCityBtn.setVisibility(View.VISIBLE);
            	mDistrictBtn.setVisibility(View.GONE);*/
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
            case ADDRESS_CITY_GET_FAILED:
            	
            	Log.e("zyf","city get failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
           case ADDRESS_DISTRICT_GETTING:
        	   
        	   //mTitleView.setText(getString(R.string.title_district));
            	
            	Log.e("zyf","city get failed......");
            	
            	mProgressDialogUtils.showDialog();
            	break;
            case ADDRESS_DISTRICT_GET_SUCCESS:
            	
                Log.e("zyf","district get success......");
                
                mTitleView.setText(getString(R.string.title_district));
                
                mHotCityGridView.setVisibility(View.GONE);
                mProvinceGridView.setVisibility(View.GONE);
            	mCityGridView.setVisibility(View.GONE);
            	
            	mDistrictGridView.setVisibility(View.VISIBLE);
            	
            	mProvinceBtn.setText(pName);
            	mCityBtn.setText(cName);
            	
            	if(mDistrictBaseAdapter==null){
            		mDistrictBaseAdapter=new MyDistrictBaseAdapter(AddressSelectorOldActivity.this);
            		mDistrictGridView.setAdapter(mDistrictBaseAdapter);
            	}else{
            		mDistrictBaseAdapter.notifyDataSetChanged();
            	}
            	
            	/*mProvinceBtn.setVisibility(View.VISIBLE);
            	mCityBtn.setVisibility(View.VISIBLE);
            	mDistrictBtn.setVisibility(View.GONE);*/
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	break;
            case ADDRESS_DISTRICT_GET_FAILED:
            	
            	Log.e("zyf","district get failed......");
            	
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
		
		setContentView(R.layout.activity_address_selector);
		
		initViews();
		
		mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_province, this);
		
		/*mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.GET_PROVINCE_URL, null, null);
		mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelector2Activity.this);
		mAddressAsyncTaskDataLoader.execute();*/
		
		//获取热门城市列表
		mHotCityAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_HOT_CITY, UrlConfigs.SERVER_URL+UrlConfigs.GET_HOT_CITY_URL, null, null);
		mHotCityAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
		mHotCityAsyncTaskDataLoader.execute();
	}
	
	private void initViews(){
		
		mProvinceBtn=(MyDeletableBtn)findViewById(R.id.mProvinceBtn);
		mCityBtn=(MyDeletableBtn)findViewById(R.id.mCityBtn);
		mDistrictBtn=(MyDeletableBtn)findViewById(R.id.mDistrictBtn);
		
		mProvinceBtn.setType(MyDeletableBtn.TYPE_PROVINCE);
		mCityBtn.setType(MyDeletableBtn.TYPE_CITY);
		mDistrictBtn.setType(MyDeletableBtn.TYPE_DISTRICT);
		
		mProvinceBtn.setOnDeletedClickLister(this);
		mCityBtn.setOnDeletedClickLister(this);
		mDistrictBtn.setOnDeletedClickLister(this);
		
		mSearchContainer=(LinearLayout)findViewById(R.id.mSearchContainer);
		mSearchContainer.setOnClickListener(this);
		
		mProvinceGridView=(GridView)findViewById(R.id.mProvinceGridView);
		mCityGridView=(GridView)findViewById(R.id.mCityGridView);
		mDistrictGridView=(GridView)findViewById(R.id.mDistrictGridView);
		
		mProvinceGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				mProvinceBtn.setVisibility(View.VISIBLE);
				mProvinceBtn.setText(provinceItemList.get(position).getName());
				
				pid=provinceItemList.get(position).getId();
				pName=provinceItemList.get(position).getName();
				
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+provinceItemList.get(position).getId();
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
			}
		});
		
		mCityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				mCityBtn.setVisibility(View.VISIBLE);
				mCityBtn.setText(cityItemList.get(position).getName());
				
				
				cid=cityItemList.get(position).getId();
				cName=cityItemList.get(position).getName();
				
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+cityItemList.get(position).getId();
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
			}
		});
		
		mDistrictGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				mDistrictBtn.setText(districtItemList.get(position).getName());
				
				
				did=districtItemList.get(position).getId();
				dName=districtItemList.get(position).getName();
				
				Log.e("zyf","save save save......");
				
				Intent data=new Intent();
				
				AddressItem item=new AddressItem();
				item.setpName(pName);
				item.setcName(cName);
				item.setdName(dName);
				item.setpId(pid);
				item.setcId(cid);
				item.setdId(did);
				data.putExtra("AddressItem", item);
				setResult(ActivityResultCode.CODE_ADDRESS_SELECTOR, data);
				
				finish();
			}
		});
		
		mHotCityGridView=(GridView)findViewById(R.id.mHotCityGridView);
		mHotCityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				if(position==0){
					return;
				}
				
				position--;
				
				pid=hotCityItems.get(position).getpId();
				pName=hotCityItems.get(position).getpName();
				
				cid=hotCityItems.get(position).getcId();
				cName=hotCityItems.get(position).getcName();
				
				//<add by Yongfeng.zhang 2014.10.29
				//mPcdSelectBtn.setVisibility(View.GONE);
				mSearchContainer.setVisibility(View.GONE);
				//>end by Yongfeng.zhang
				
				
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+hotCityItems.get(position).getcId();
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
			}
		});
	}
	
	private TextView mTitleView;

	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_address_add));
	}

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_PROVINCE){
			Log.e("zyf","get province......");
			
			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GETTING);
		}else if(flag==Flag.FLAG_GET_CITY){
			
			Log.e("zyf","get city......");
			
			Utils.sendMessage(mHandler, ADDRESS_CITY_GETTING);
		}else if(flag==Flag.FLAG_GET_DISTRICT){
			
			Log.e("zyf","get district......");
			
			Utils.sendMessage(mHandler, ADDRESS_DISTRICT_GETTING);
			
		}else if(flag==Flag.FLAG_GET_HOT_CITY){
			Log.e("zyf","get hot city......");
			
			Utils.sendMessage(mHandler, HOT_CITY_GETTIING);
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		Log.e("zyf","task completed......result: "+result);
		
		if(flag==Flag.FLAG_GET_HOT_CITY){
			Log.e("zyf","hot city: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray hotCityJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					HotCityItem item;
					
					hotCityItems.clear();
					
					for(int i=0;i<hotCityJsonArray.length();i++){
						item=new HotCityItem();
						
						jsonObject=hotCityJsonArray.getJSONObject(i);
						
						item.sethIndex(jsonObject.getInt("hindex"));
						item.setpId(jsonObject.getString("pid"));
						item.setpName(jsonObject.getString("pname"));
						item.setcId(jsonObject.getString("cid"));
						item.setcName(jsonObject.getString("cname"));
						
						hotCityItems.add(item);
					}
				}
				
				Utils.sendMessage(mHandler, HOT_CITY_GET_SUCCESS);
				
				return;
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			Utils.sendMessage(mHandler, HOT_CITY_GET_FAILED);
			
			return;
		}
		
		//省份选择回调接口
		try {
			JSONObject totalJsonObject=new JSONObject(result);
			
			Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
			Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
			
			String rc=totalJsonObject.getString("rc");
			
			if("0".equals(rc)){
				JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
				Iterator it = areaJsonObject.keys();
				AreaItem item;
				String key,value;
				addressItemList.clear();
	            while(it.hasNext()) {  
	                key=(String)it.next();  
	                value=areaJsonObject.getString(key);
	                
	                item=new AreaItem();
	                item.setId(key);
	                item.setName(value);
	                
	                addressItemList.add(item);
	            }
	            
	            if(flag==Flag.FLAG_GET_PROVINCE){
	            	provinceItemList.clear();
	    			for(int i=0;i<addressItemList.size();i++){
	    				provinceItemList.add(addressItemList.get(i));
	    			}
	    			
	    			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GET_SUCCESS);
	    			
	    		}else if(flag==Flag.FLAG_GET_CITY){
	    			cityItemList.clear();
	    			for(int i=0;i<addressItemList.size();i++){
	    				cityItemList.add(addressItemList.get(i));
	    			}
	    			
	    			Utils.sendMessage(mHandler, ADDRESS_CITY_GET_SUCCESS);
	    		}else if(flag==Flag.FLAG_GET_DISTRICT){
	    			
	    			districtItemList.clear();
	    			for(int i=0;i<addressItemList.size();i++){
	    				districtItemList.add(addressItemList.get(i));
	    			}
	    			
	    			Utils.sendMessage(mHandler, ADDRESS_DISTRICT_GET_SUCCESS);
	    		}
	            
	            return;
			}
		} catch (Exception e) {
			Log.e("zyf",e.toString());
		}
		
		if(flag==Flag.FLAG_GET_PROVINCE){			
			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GET_FAILED);	
		}else if(flag==Flag.FLAG_GET_CITY){
			Utils.sendMessage(mHandler, ADDRESS_CITY_GET_FAILED);
		}else if(flag==Flag.FLAG_GET_DISTRICT){
			Utils.sendMessage(mHandler, ADDRESS_DISTRICT_GET_FAILED);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:
			
			finish();
			break;
		case R.id.rightBtn:
			Log.e("zyf","save save save......");
			
			Intent data=new Intent();
			
			AddressItem item=new AddressItem();
			item.setpName(pName);
			item.setcName(cName);
			item.setdName(dName);
			item.setpId(pid);
			item.setcId(cid);
			item.setdId(did);
			data.putExtra("AddressItem", item);
			setResult(ActivityResultCode.CODE_ADDRESS_SELECTOR, data);
			
			finish();
			break;
			
		case R.id.mSearchContainer:
			
			mSearchContainer.setVisibility(View.GONE);
			
			mHotCityGridView.setVisibility(View.GONE);
			
			if(mAddressAsyncTaskDataLoader!=null){
				mAddressAsyncTaskDataLoader.canceled();
			}
			
			mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.SERVER_URL+UrlConfigs.GET_PROVINCE_URL, null, null);
			mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
			mAddressAsyncTaskDataLoader.execute();
			break;
		default:
			break;
		}
	}
	
   class MyProvinceBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyProvinceBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return provinceItemList.size();
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
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_area, null);
			}
			
			AreaItem item;
			item=provinceItemList.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			
			mTitleView.setText(item.getName());
			
			return convertView;
		}	
	}
   
   class MyCityBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyCityBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return cityItemList.size();
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
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_area, null);
			}
			
			AreaItem item;
			item=cityItemList.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			
			mTitleView.setText(item.getName());
			
			return convertView;
		}	
	}
   
   class MyDistrictBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyDistrictBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return districtItemList.size();
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
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_area, null);
			}
			
			AreaItem item;
			item=districtItemList.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			
			mTitleView.setText(item.getName());
			
			return convertView;
		}	
	}
   
   class MyHotCityBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyHotCityBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return hotCityItems.size()+1;   //包含一个“热门城市”
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
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_area, null);
			}
			
			HotCityItem item;
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			
			if(position==0){
				mTitleView.setText(getString(R.string.hot_city));
				mTitleView.setTextColor(Color.parseColor("#ED6900"));
			}else{
				item=hotCityItems.get(position-1);
				mTitleView.setText(item.getcName());
				mTitleView.setTextColor(Color.parseColor("#000000"));
			}
			
			return convertView;
		}	
	}

	@Override
	public void OnDeleted(int flag) {
		
        if(flag==MyDeletableBtn.TYPE_PROVINCE){  //返回之前的省份列表
        	
        	mTitleView.setText(getString(R.string.title_province));
			
        	mProvinceBtn.setVisibility(View.GONE);
			mCityBtn.setVisibility(View.GONE);
			mDistrictBtn.setVisibility(View.GONE);
			
			mProvinceGridView.setVisibility(View.VISIBLE);
        	mCityGridView.setVisibility(View.GONE);
        	mDistrictGridView.setVisibility(View.GONE);
        	
            if(provinceItemList.size()==0){
        		
        		Log.e("zyf","no province list,link to server......");
        		
        		mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.SERVER_URL+UrlConfigs.GET_PROVINCE_URL, null, null);
        		mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
        		mAddressAsyncTaskDataLoader.execute();
        	}
			
		}else if(flag==MyDeletableBtn.TYPE_CITY){  //返回之前的城市列表
			
			mTitleView.setText(getString(R.string.title_city));
			
			mProvinceGridView.setVisibility(View.GONE);
        	mCityGridView.setVisibility(View.VISIBLE);
        	mDistrictGridView.setVisibility(View.GONE);
        	
        	mDistrictBtn.setVisibility(View.INVISIBLE);
        	
        	if(cityItemList.size()==0){
        		Log.e("zyf","no city list,link to server......");
        		
        		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+pid;
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorOldActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
        	}
		}else if(flag==MyDeletableBtn.TYPE_DISTRICT){     //返回之前的县/区列表
			
			mProvinceGridView.setVisibility(View.GONE);
        	mCityGridView.setVisibility(View.GONE);
        	mDistrictGridView.setVisibility(View.VISIBLE);
		}
	}

}

