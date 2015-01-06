package com.weiwend.fooldelivery;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyDeletableBtn;
import com.weiwend.fooldelivery.customviews.MyDeletableBtn.MyOnDeletedClickLister;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AreaItem;
import com.weiwend.fooldelivery.items.HotCityItem;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class AddressSelectorActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOnDeletedClickLister{

	//显示省、市、区、热门城市的GridView
	private GridView mProvinceGridView,mCityGridView,mDistrictGridView,mHotCityGridView;
	
	//选择省、市、区后，显示省、市、区信息的按钮，点击该按钮的右上方，可以实现删除操作
	private MyDeletableBtn mProvinceBtn,mCityBtn,mDistrictBtn;
	
	//热门城市的整体Container，包括热门城市的标题和内容
	private LinearLayout mHotCityContainer;
	
	//热门城市的标题、选择省份的标题
	private TextView mHotCityTitleTv,mPcdTitleTv;
	
	//页面顶部的标题显示，用于“省”、“市”，“区/县”的显示切换
	private TextView mTitleView;
	
	//获取省、市、区信息时临时保存省、市、区的列表信息，主要是为了减少代码量
	private ArrayList<AreaItem> addressItemList=new ArrayList<AreaItem>();
	
	//保存的热门城市列表
	private ArrayList<HotCityItem> hotCityItems=new ArrayList<HotCityItem>();
	
	//保存的省份信息列表
	private ArrayList<AreaItem> provinceItemList=new ArrayList<AreaItem>();
	
	//保存的城市信息列表
	private ArrayList<AreaItem>cityItemList=new ArrayList<AreaItem>();
	
	//保存的区、县列表
	private ArrayList<AreaItem>districtItemList=new ArrayList<AreaItem>();
	
	//热门城市列表GridView的适配器
	private MyHotCityBaseAdapter mHotCityBaseAdapter;
	
	//省份信息列表GridView的适配器
	private MyProvinceBaseAdapter mProvinceBaseAdapter;
	
	//城市信息列表GridView的适配器
	private MyCityBaseAdapter mCityBaseAdapter;
	
	//区、县列表GridView的适配器
	private MyDistrictBaseAdapter mDistrictBaseAdapter;
	
	//保存用户选择的省份id、城市id、区/县id
	private String pid,cid,did;
	
	//保存用户选择的省份名称、城市名称、区/县名称
	private String pName,cName,dName;
	
	//获取热门城市时的各种状态信息
	private final int HOT_CITY_GETTIING=1;
	private final int HOT_CITY_GET_SUCCESS=2;
	private final int HOT_CITY_GET_FAILED=3;
	
	//获取省份信息时的各种状态信息
	private final int ADDRESS_PROVINCE_GETTING=4;
	private final int ADDRESS_PROVINCE_GET_SUCCESS=5;
	private final int ADDRESS_PROVINCE_GET_FAILED=6;
	
	//获取城市信息时的各种状态信息
	private final int ADDRESS_CITY_GETTING=7;
	private final int ADDRESS_CITY_GET_SUCCESS=8;
	private final int ADDRESS_CITY_GET_FAILED=9;
	
	//获取区、县信息时的各种状态信息
	private final int ADDRESS_DISTRICT_GETTING=10;
	private final int ADDRESS_DISTRICT_GET_SUCCESS=11;
	private final int ADDRESS_DISTRICT_GET_FAILED=12;
	
	//获取省、市、区信息的异步加载类
	private AsyncTaskDataLoader mAddressAsyncTaskDataLoader;
	
	//获取热门城市的异步加载类
	private AsyncTaskDataLoader mHotCityAsyncTaskDataLoader;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case HOT_CITY_GETTIING:      //获取热门城市的接口已经调用，正在等待服务器端的回复
            	
                Log.e("zyf","get hot city......");
                
                mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_hot_city, AddressSelectorActivity.this);
                mProgressDialogUtils.showDialog();

            	break;
			
            case HOT_CITY_GET_SUCCESS:      //获取热门城市成功，下一步获取省份信息，只在界面创建时调用      
            	
                Log.e("zyf","get hot city success......");
                
                mProvinceGridView.setVisibility(View.GONE);
                mCityGridView.setVisibility(View.GONE);
                mDistrictGridView.setVisibility(View.GONE);
                
                mHotCityTitleTv.setVisibility(View.VISIBLE);
                mHotCityContainer.setVisibility(View.VISIBLE);
				
				if(mHotCityBaseAdapter==null){
					mHotCityBaseAdapter=new MyHotCityBaseAdapter(AddressSelectorActivity.this);
					mHotCityGridView.setAdapter(mHotCityBaseAdapter);
				}else{
					mHotCityBaseAdapter.notifyDataSetChanged();
				}
				
				mProgressDialogUtils.dismissDialog();
				
				if(mAddressAsyncTaskDataLoader!=null){      //获取省份信息
					mAddressAsyncTaskDataLoader.canceled();
				}
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.SERVER_URL+UrlConfigs.GET_PROVINCE_URL, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
				mAddressAsyncTaskDataLoader.execute();
				
            	break;
            case HOT_CITY_GET_FAILED:      //获取热门城市失败
            	
            	Log.e("zyf","get hot city failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	MyToast.makeText(AddressSelectorActivity.this, getString(R.string.hot_city_get_failed), MyToast.LENGTH_SHORT).show();
            	
            	break; 
            case ADDRESS_PROVINCE_GETTING:      //获取省份信息的接口已经调用，正在等待服务器端的回复
            	
            	Log.e("zyf","province getting ......");
            	
            	mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_province, AddressSelectorActivity.this);
            	mProgressDialogUtils.showDialog();
            	
            	break;
            case ADDRESS_PROVINCE_GET_SUCCESS:      //获取省份信息成功
            	
            	Log.e("zyf","province get success......");
            	
            	mTitleView.setText(getString(R.string.title_province));
            	
            	if(mProvinceBaseAdapter==null){
            		mProvinceBaseAdapter=new MyProvinceBaseAdapter(AddressSelectorActivity.this);
            		mProvinceGridView.setAdapter(mProvinceBaseAdapter);
            	}else{
            		mProvinceBaseAdapter.notifyDataSetChanged();
            	}
            	
            	mPcdTitleTv.setVisibility(View.VISIBLE);
            	mProvinceGridView.setVisibility(View.VISIBLE);
            	
            	mCityGridView.setVisibility(View.GONE);
            	mDistrictGridView.setVisibility(View.GONE);         
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
            case ADDRESS_PROVINCE_GET_FAILED:      //获取省份信息失败
            	
            	Log.e("zyf","province get failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	MyToast.makeText(AddressSelectorActivity.this, getString(R.string.province_get_failed), MyToast.LENGTH_SHORT).show();
            	break;
            case ADDRESS_CITY_GETTING:       //获取城市信息的接口已经调用，正在等待服务器端的回复
            	
            	mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_city, AddressSelectorActivity.this);
            	Log.e("zyf","city getting......");
            	
            	mProgressDialogUtils.showDialog();
            	
            	break;
            case ADDRESS_CITY_GET_SUCCESS:      //获取城市信息成功
            	
                Log.e("zyf","city get success......");
                
                mTitleView.setText(getString(R.string.title_city));
            	
            	if(mCityBaseAdapter==null){
            		mCityBaseAdapter=new MyCityBaseAdapter(AddressSelectorActivity.this);
            		mCityGridView.setAdapter(mCityBaseAdapter);
            	}else{
            		mCityBaseAdapter.notifyDataSetChanged();
            	}
            	
            	mPcdTitleTv.setVisibility(View.GONE);
            	mProvinceGridView.setVisibility(View.GONE);
            	
            	mCityGridView.setVisibility(View.VISIBLE);
            	mDistrictGridView.setVisibility(View.GONE);
            	
            	mProgressDialogUtils.dismissDialog();
            	break;
            case ADDRESS_CITY_GET_FAILED:      //获取城市信息失败
            	
            	Log.e("zyf","city get failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	MyToast.makeText(AddressSelectorActivity.this, getString(R.string.city_get_failed), MyToast.LENGTH_SHORT).show();
            	break;
           case ADDRESS_DISTRICT_GETTING:      //获取区、县信息的接口已经调用，正在等待服务器端的回复
            	
            	Log.e("zyf","district getting......");
            	
            	mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_district, AddressSelectorActivity.this);
            	mProgressDialogUtils.showDialog();
            	break;
            case ADDRESS_DISTRICT_GET_SUCCESS:      //获取区、县信息成功
            	
                Log.e("zyf","district get success......");
                
                mTitleView.setText(getString(R.string.title_district));
                
                mPcdTitleTv.setVisibility(View.GONE);
                mHotCityContainer.setVisibility(View.GONE);
                
                mProvinceGridView.setVisibility(View.GONE);
            	mCityGridView.setVisibility(View.GONE);
            	
            	mDistrictGridView.setVisibility(View.VISIBLE);
            	
            	mProvinceBtn.setText(pName);
            	mCityBtn.setText(cName);
            	
            	if(mDistrictBaseAdapter==null){
            		mDistrictBaseAdapter=new MyDistrictBaseAdapter(AddressSelectorActivity.this);
            		mDistrictGridView.setAdapter(mDistrictBaseAdapter);
            	}else{
            		mDistrictBaseAdapter.notifyDataSetChanged();
            	}
            	
				mHotCityContainer.setVisibility(View.GONE);
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	break;
            case ADDRESS_DISTRICT_GET_FAILED:      //获取区、县信息失败
            	
            	Log.e("zyf","district get failed......");
            	
            	mProgressDialogUtils.dismissDialog();
            	
            	MyToast.makeText(AddressSelectorActivity.this, getString(R.string.district_get_failed), MyToast.LENGTH_SHORT).show();
            	break;
			default:
				break;
			}
		}
    };
    
    //标识是否是“比价”界面调用该页面
    private boolean isFromParity;
    
    //标识是选择“出发城市”还是“目的城市”，仅当“比价”界面调用该页面时起作用
    private int type;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_address_selector2);
		
		isFromParity=getIntent().getBooleanExtra("isFromParity", false);
		type=getIntent().getIntExtra("type", 0);
		
		initViews();
		
		//初始化界面，获取热门城市列表
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_HOT_CITY_URL;
		
		mHotCityAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_HOT_CITY, url, null, null);
		mHotCityAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
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
		
		mHotCityContainer=(LinearLayout)findViewById(R.id.mHotCityContainer);
		
		mHotCityTitleTv=(TextView)findViewById(R.id.mHotCityTitleTv);
		mPcdTitleTv=(TextView)findViewById(R.id.mPcdTitleTv);
		
		mProvinceGridView=(GridView)findViewById(R.id.mProvinceGridView);
		mCityGridView=(GridView)findViewById(R.id.mCityGridView);
		mDistrictGridView=(GridView)findViewById(R.id.mDistrictGridView);
		
		mProvinceGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				mHotCityContainer.setVisibility(View.GONE);
				mCityGridView.setVisibility(View.GONE);
				mDistrictGridView.setVisibility(View.GONE);
				
				mProvinceBtn.setVisibility(View.VISIBLE);
				mProvinceBtn.setText(provinceItemList.get(position).getName());
				
				pid=provinceItemList.get(position).getId();
				pName=provinceItemList.get(position).getName();
				
				//获取所选择省份下的所有城市信息
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+provinceItemList.get(position).getId();
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
			}
		});
		
		mCityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				if(isFromParity){    //若是“比价”界面调用的该页面，则选择城市后已经完成目的，故返回
					
					Intent intent=new Intent();
					intent.putExtra("type", type);
					intent.putExtra("cid", cityItemList.get(position).getId());
					intent.putExtra("cname", cityItemList.get(position).getName());
					
					setResult(ActivityResultCode.CODE_PARITY_CITY_SELECTOR, intent);
					
					finish();
					
				}else{   //若不是“比价”界面调用的该页面，则需要继续选择区、县信息
					
					mCityBtn.setVisibility(View.VISIBLE);
					mCityBtn.setText(cityItemList.get(position).getName());
					
					
					cid=cityItemList.get(position).getId();
					cName=cityItemList.get(position).getName();
					
					//获取所选择城市下的所有区、县信息
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+cityItemList.get(position).getId();
					mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
					mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
					
					mAddressAsyncTaskDataLoader.execute();
				}
			}
		});
		
		mDistrictGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				//选择区、县信息后已经完成目的，故返回
				mDistrictBtn.setText(districtItemList.get(position).getName());
				
				did=districtItemList.get(position).getId();
				dName=districtItemList.get(position).getName();
				
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
				
                if(isFromParity){      //若是“比价”界面调用的该页面，则选择热门城市后已经完成目的，故返回
					
					Intent intent=new Intent();
					intent.putExtra("type", type);
					intent.putExtra("cid", hotCityItems.get(position).getcId());
					intent.putExtra("cname", hotCityItems.get(position).getcName());
					
					setResult(ActivityResultCode.CODE_PARITY_CITY_SELECTOR, intent);
					
					finish();
					
				}else{      //若不是“比价”界面调用的该页面，则需要继续选择区、县信息
					
					//需要清空城市列表，否则用户之前点击了省份信息后，会有城市缓存信息
					cityItemList.clear();
					
					pid=hotCityItems.get(position).getpId();
					pName=hotCityItems.get(position).getpName();
					
					cid=hotCityItems.get(position).getcId();
					cName=hotCityItems.get(position).getcName();
					
					//获取所选择热门城市下的所有区、县信息
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+hotCityItems.get(position).getcId();
					mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
					mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
					
					mAddressAsyncTaskDataLoader.execute();
				}
			}
		});
	}

	//初始化当前页面的标题
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_address_add));
	}

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_PROVINCE){     //开始获取省份信息
			
			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GETTING);
			
		}else if(flag==Flag.FLAG_GET_CITY){    //开始获取城市信息
			
			Utils.sendMessage(mHandler, ADDRESS_CITY_GETTING);
			
		}else if(flag==Flag.FLAG_GET_DISTRICT){      //开始获取区、县信息
			
			Utils.sendMessage(mHandler, ADDRESS_DISTRICT_GETTING);
			
		}else if(flag==Flag.FLAG_GET_HOT_CITY){      //开始获取热门城市信息
			
			Utils.sendMessage(mHandler, HOT_CITY_GETTIING);
			
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		Log.e("zyf","task completed......result: "+result);
		
		if(flag==Flag.FLAG_GET_HOT_CITY){      //获取热门城市返回结果
			
			Log.e("zyf","hot city: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //热门城市获取成功
					
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
		
		//获取省、市、区信息的返回结果
		try {
			JSONObject totalJsonObject=new JSONObject(result);
			
			Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
			Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
			
			String rc=totalJsonObject.getString("rc");
			
			if("0".equals(rc)){      //获取省、市、区信息成功
				
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
	            
	            if(flag==Flag.FLAG_GET_PROVINCE){    //保存省份信息
	            	
	            	provinceItemList.clear();
	            	
	    			for(int i=0;i<addressItemList.size();i++){
	    				provinceItemList.add(addressItemList.get(i));
	    			}
	    			
	    			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GET_SUCCESS);
	    			
	    		}else if(flag==Flag.FLAG_GET_CITY){      //保存城市信息
	    			
	    			cityItemList.clear();
	    			
	    			for(int i=0;i<addressItemList.size();i++){
	    				cityItemList.add(addressItemList.get(i));
	    			}
	    			
	    			Utils.sendMessage(mHandler, ADDRESS_CITY_GET_SUCCESS);
	    			
	    		}else if(flag==Flag.FLAG_GET_DISTRICT){      //保存区、县信息
	    			
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
			Utils.sendMessage(mHandler, ADDRESS_PROVINCE_GET_FAILED);      //获取省份信息失败
		}else if(flag==Flag.FLAG_GET_CITY){
			Utils.sendMessage(mHandler, ADDRESS_CITY_GET_FAILED);      //获取城市信息失败
		}else if(flag==Flag.FLAG_GET_DISTRICT){
			Utils.sendMessage(mHandler, ADDRESS_DISTRICT_GET_FAILED);      //获取区、县信息失败
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:   //标题栏的返回功能
			
			finish();
			break;
		/*case R.id.rightBtn:
			Log.e("zyf","save save save......");
			
			Intent data=new Intent();
			
			MyAddressItem item=new MyAddressItem();
			item.setpName(pName);
			item.setcName(cName);
			item.setdName(dName);
			item.setpId(pid);
			item.setcId(cid);
			item.setdId(did);
			data.putExtra("AddressItem", item);
			setResult(ActivityResultCode.CODE_ADDRESS_SELECTOR, data);
			
			finish();
			break;*/
		
		default:
			break;
		}
	}
	
    //省份信息GridView的适配器
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
   
   //城市信息GridView的适配器
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
   
   //区、县信息GridView的适配器
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
   
   //热门城市信息GridView的适配器
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
			return hotCityItems.size();
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
			
			HotCityItem item=hotCityItems.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			
			mTitleView.setText(item.getcName());
			
			return convertView;
		}	
	}

    //MyDeletableBtn的右上方删除回调函数
	@Override
	public void OnDeleted(int flag) {
		
        if(flag==MyDeletableBtn.TYPE_PROVINCE){      //删除已经选择的省份信息，返回到之前的热门城市列表+省份列表
        	
        	mTitleView.setText(getString(R.string.title_province));
			
        	mProvinceBtn.setVisibility(View.GONE);
			mCityBtn.setVisibility(View.GONE);
			mDistrictBtn.setVisibility(View.GONE);
			
			mHotCityTitleTv.setVisibility(View.VISIBLE);
			mPcdTitleTv.setVisibility(View.VISIBLE);
			mProvinceGridView.setVisibility(View.VISIBLE);
			mHotCityContainer.setVisibility(View.VISIBLE);
			
        	mCityGridView.setVisibility(View.GONE);
        	mDistrictGridView.setVisibility(View.GONE);
        	
            if(provinceItemList.size()==0){      //若省市列表无缓存，则重新获取
        		
        		Log.e("zyf","no province list,link to server......");
        		
        		mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.SERVER_URL+UrlConfigs.GET_PROVINCE_URL, null, null);
        		mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
        		mAddressAsyncTaskDataLoader.execute();
        	}
			
		}else if(flag==MyDeletableBtn.TYPE_CITY){  //删除已经选择的城市信息，返回到已选择的省份下的所有城市列表
			
			mTitleView.setText(getString(R.string.title_city));
			
			mProvinceGridView.setVisibility(View.GONE);
        	mCityGridView.setVisibility(View.VISIBLE);
        	mDistrictGridView.setVisibility(View.GONE);
        	
        	mDistrictBtn.setVisibility(View.INVISIBLE);
        	
        	if(cityItemList.size()==0){     //若城市列表无缓存，则重新获取
        		
        		Log.e("zyf","no city list,link to server......");
        		
        		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+pid;
				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressSelectorActivity.this);
				
				mAddressAsyncTaskDataLoader.execute();
        	}
		}else if(flag==MyDeletableBtn.TYPE_DISTRICT){     //ui改变，不会调用到此
			
			mProvinceGridView.setVisibility(View.GONE);
        	mCityGridView.setVisibility(View.GONE);
        	mDistrictGridView.setVisibility(View.VISIBLE);
		}
	}

}


