package com.weiwend.fooldelivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyPhonePromptDialog;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.ParityCompanyItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class ParityActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,AMapLocationListener{
    
    //“出发地”、“目的地”、“-”、“+”、“确定”按钮
    private Button mFromCityBtn,mToCityBtn,mMinusBtn,mPlusBtn,mSummitBtn;
    
    //比价后的网点列表Listview
    private PullToRefreshListView mPullToRefreshListView;
	
    //网点列表Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//重量输入框
	private EditText mWeightEt;
	
	//保存比价后的网点信息
	private ArrayList<ParityCompanyItem> parityCompanyItems=new ArrayList<ParityCompanyItem>();
	
	//通过比价获取网点信息的各种状态信息
	private final int PARITY_COMPANY_GETTING=0;
	private final int PARITY_COMPANY_GET_SUCCESS=1;
	private final int PARITY_COMPANY_GET_FAILED=2;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
            case PARITY_COMPANY_GETTING:      //开始定位；或者，之前定位已成功，此时开始调用获取比价的网点信息
            	
				Log.e("zyf","parity company list getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_parity_company, ParityActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case PARITY_COMPANY_GET_SUCCESS:      //比价后的网点信息获取成功
				
				Log.e("zyf","parity company list get success......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(ParityActivity.this);
					
					mPullToRefreshListView.setAdapter(mBaseAdapter);
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				MyToast.makeText(ParityActivity.this, getString(R.string.parity_company_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case PARITY_COMPANY_GET_FAILED:      //比价后的网点信息获取失败
				
				Log.e("zyf","parity company list get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
                MyToast.makeText(ParityActivity.this, getString(R.string.parity_company_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//获取比价后的网点信息的异步加载类
	private AsyncTaskDataLoader mGetParityCompanyAsyncTaskDataLoader;
	
	//高德地图定位类
	private LocationManagerProxy mLocationManagerProxy;
	
	//出发地城市id、目的地城市id
	private String sdid,rdid;
	
	//出发地城市名称、目的地城市名称
	private String sname,rname;
	
	//物件的重量
	private String weight;
	
	//标识是否是首页“快递比价”模块调用的该页面
	private boolean isFromHome;
	
	//高德地图定位返回的经纬度
	private String lon,lat;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_parity);
		
		sdid=getIntent().getStringExtra("sdid");
		rdid=getIntent().getStringExtra("rdid");
		weight=getIntent().getStringExtra("weight");
		sname=getIntent().getStringExtra("sname");
		rname=getIntent().getStringExtra("rname");
		
		if(sdid==null){        //此时说明用户是通过首页中的“快递比价”调用的该页面
			isFromHome=true;
		}
		
		initViews();
		
		if(sdid!=null&&rdid!=null){      //说明用户是通过寄件页面中的“去比价”调用的该页面，此时只能刷新操作，其他信息均不可编辑
			
			mSummitBtn.setVisibility(View.GONE);
			mFromCityBtn.setEnabled(false);
			mToCityBtn.setEnabled(false);
			mMinusBtn.setEnabled(false);
			mPlusBtn.setEnabled(false);
			mWeightEt.setEnabled(false);
			
			Utils.sendMessage(mHandler, PARITY_COMPANY_GETTING);
			
			//开始定位
			mLocationManagerProxy = LocationManagerProxy.getInstance(this);
			mLocationManagerProxy.setGpsEnable(false);
			mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, ParityActivity.this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		//停止定位
		if(mLocationManagerProxy!=null){
			mLocationManagerProxy.removeUpdates(this);
			mLocationManagerProxy.destroy();
		}
	}
	
	//初始化界面
	private void initViews(){
		
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				String label = DateUtils.formatDateTime(ParityActivity.this, System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){      //下拉刷新
					
					if(sdid==null){
						
						MyToast.makeText(ParityActivity.this, getString(R.string.prompt_from_city_must_not_null), MyToast.LENGTH_SHORT).show();
						
						refreshView.onRefreshComplete();
						
						return;
					}
					
					if(rdid==null){
						
						MyToast.makeText(ParityActivity.this, getString(R.string.prompt_to_city_must_not_null), MyToast.LENGTH_SHORT).show();
						
						refreshView.onRefreshComplete();
						
						return;
					}
					
					if(Float.parseFloat(mWeightEt.getText().toString())==0){
						
						MyToast.makeText(ParityActivity.this, getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
						
						refreshView.onRefreshComplete();
						
						return;
					}
					
					if(lon==null){  //之前定位失败，则重新进行定位
						
						Utils.sendMessage(mHandler, PARITY_COMPANY_GETTING);
						
						mLocationManagerProxy = LocationManagerProxy.getInstance(ParityActivity.this);
						mLocationManagerProxy.setGpsEnable(false);
						mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, ParityActivity.this);
						
					}else{      //之前定位已成功，则上传必要信息获取比价后的网点信息
						
						Utils.sendMessage(mHandler, PARITY_COMPANY_GETTING);
						
						float weight=Float.parseFloat(mWeightEt.getText().toString());
						
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_PARITY_URL+"?lon="+lon+"&lat="
					        	+lat+"&s_did="+sdid+"&r_did="+rdid+"&weight="+weight+"&size="+"100";
					        	
			        	mGetParityCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PARITY_COMPANY, url, null, null);
			    		mGetParityCompanyAsyncTaskDataLoader.setOnDataLoaderListener(ParityActivity.this);
			    		
			    		mGetParityCompanyAsyncTaskDataLoader.execute();
					}
					
				}else{
					
				}
				
			}
		});
		
		mFromCityBtn=(Button)findViewById(R.id.mFromCityBtn);
		mToCityBtn=(Button)findViewById(R.id.mToCityBtn);
		mMinusBtn=(Button)findViewById(R.id.mMinusBtn);
		mPlusBtn=(Button)findViewById(R.id.mPlusBtn);
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		
		mFromCityBtn.setOnClickListener(this);
		mToCityBtn.setOnClickListener(this);
		mMinusBtn.setOnClickListener(this);
		mPlusBtn.setOnClickListener(this);
		mSummitBtn.setOnClickListener(this);
		
		mWeightEt=(EditText)findViewById(R.id.mWeightEt);
		
		if(weight!=null){
		    mWeightEt.setText(weight);
		}
        mWeightEt.setSelection(mWeightEt.getText().toString().length());
		
        //控制输入框最多只能输入一位小数
		mWeightEt.addTextChangedListener(new TextWatcher() {
			 
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            	
            	String number=s.toString();
            	int len=number.length();
                int dotIndex=number.indexOf(".");
 
                if(dotIndex==-1){   //没有小数点
                	if(len==2){    //防止前一位为0，第二位不为零
                		if(number.substring(0, 1).equals("0")){
                			mWeightEt.setText(number.substring(1, 2));
                    		mWeightEt.setSelection(mWeightEt.getText().toString().length());
                		}
                	}
                	
                }else if(dotIndex==0){    //限制第一位不可以输入小数点
                	mWeightEt.setText("");
                }else{    //限制小数点之后只能输入一位
                	if(len>dotIndex+2){
                		mWeightEt.setText(number.substring(0, dotIndex+2));
                		mWeightEt.setSelection(mWeightEt.getText().toString().length());
                	}
                }
            }
 
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
 
            }
 
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                 
            }
 
        });
		
		if(sname!=null){
			mFromCityBtn.setText(sname);
			mFromCityBtn.setTextColor(Color.BLACK);
		}
		
		if(rname!=null){
			mToCityBtn.setText(rname);
			mToCityBtn.setTextColor(Color.BLACK);
		}
	}

	//初始化页面标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_parity));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			finish();
			break;
        case R.id.mMinusBtn:    //"-"
        	
            String str=mWeightEt.getText().toString();
			
			if(str.length()==0){
				MyToast.makeText(ParityActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			str=mWeightEt.getText().toString();
			float weight=Float.parseFloat(str);
			
			if(weight>=1){
				weight--;
			}
			
			mWeightEt.setText(weight+"");
			mWeightEt.setSelection(mWeightEt.getText().toString().length());
			
			break;
		case R.id.mPlusBtn:      //"+"
			
            String str2=mWeightEt.getText().toString();
			
			if(str2.length()==0){
				MyToast.makeText(ParityActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			float weight2=Float.parseFloat(str2);
			
			weight2++;
			
			mWeightEt.setText(weight2+"");
			mWeightEt.setSelection(mWeightEt.getText().toString().length());
			
			break;
		case R.id.mFromCityBtn:      //编辑“出发地”
			
			Intent intent=new Intent(ParityActivity.this,AddressSelectorActivity.class);
			intent.putExtra("isFromParity", true);
			intent.putExtra("type", 0);
			startActivityForResult(intent, ActivityResultCode.CODE_PARITY_CITY_SELECTOR);
			
			break;
		case R.id.mToCityBtn:      //编辑“目的地”
			
			Intent intent2=new Intent(ParityActivity.this,AddressSelectorActivity.class);
			intent2.putExtra("isFromParity", true);
			intent2.putExtra("type", 1);
			startActivityForResult(intent2, ActivityResultCode.CODE_PARITY_CITY_SELECTOR);
			
			break;
		case R.id.mSummitBtn:      //"确定"
			
			if(sdid==null){
				
				MyToast.makeText(ParityActivity.this, getString(R.string.prompt_from_city_must_not_null), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(rdid==null){
				
				MyToast.makeText(ParityActivity.this, getString(R.string.prompt_to_city_must_not_null), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(Float.parseFloat(mWeightEt.getText().toString())==0){
				MyToast.makeText(ParityActivity.this, getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			Utils.sendMessage(mHandler, PARITY_COMPANY_GETTING);
			
			if(lon==null){
				
				mLocationManagerProxy = LocationManagerProxy.getInstance(this);
				mLocationManagerProxy.setGpsEnable(false);
				mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, ParityActivity.this);
				
			}else{
				
				weight=Float.parseFloat(mWeightEt.getText().toString());
				
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_PARITY_URL+"?lon="+lon+"&lat="
			        	+lat+"&s_did="+sdid+"&r_did="+rdid+"&weight="+weight+"&size="+"100";
			        	
	        	mGetParityCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PARITY_COMPANY, url, null, null);
	    		mGetParityCompanyAsyncTaskDataLoader.setOnDataLoaderListener(this);
	    		
	    		mGetParityCompanyAsyncTaskDataLoader.execute();
			}
			
			break;

		default:
			break;
		}
	}
	
	//网点列表Listview的适配器
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
			return parityCompanyItems.size();
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
				convertView=mInflater.inflate(R.layout.item_parity_company, null);
			}
			
			final ParityCompanyItem item;
			item=parityCompanyItems.get(position);
			
			TextView mNameView=(TextView)convertView.findViewById(R.id.mNameView);
			TextView mPriceView=(TextView)convertView.findViewById(R.id.mPriceView);
			TextView mArriveTimeView=(TextView)convertView.findViewById(R.id.mArriveTimeView);
			TextView mPhoneView=(TextView)convertView.findViewById(R.id.mPhoneView);
			TextView mAddrView=(TextView)convertView.findViewById(R.id.mAddrView);
			MyLoaderImageView mLoaderImageView=(MyLoaderImageView)convertView.findViewById(R.id.mLoaderImageView);
			
			Button mPhoneBtn=(Button)convertView.findViewById(R.id.mPhoneBtn);
			mPhoneBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					MyPhonePromptDialog mDialog=new MyPhonePromptDialog(ParityActivity.this, R.style.MyDialog, item.getMobi());
					mDialog.setMySubmmitListener(new MyPhonePromptDialog.MySubmmitListener() {
						
						@Override
						public void summit(String phoneNumber) {
							
							Intent phoneIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+item.getMobi()));
							startActivity(phoneIntent);
						}
					});
					mDialog.show();
				}
			});
			
			Button mSelectBtn=(Button)convertView.findViewById(R.id.mSelectBtn);
			
			//对于已经加入到我们平台的网点，并且此时用户是通过寄件页面调用的本页面，则显示“选择”按钮，其他情况都不显示
			if(item.getOfficial().equals("1")&&isFromHome==false){
				mSelectBtn.setVisibility(View.VISIBLE);
				
				mSelectBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {     //选择完毕，返回到之前的寄件页面
						
						Intent intent=new Intent();
						intent.putExtra("ParityCompanyItem", item);
						setResult(ActivityResultCode.CODE_PARITY_SELECTOR,intent);
						finish();
					}
				});
				
				mAddrView.setVisibility(View.VISIBLE);
				mAddrView.setText(item.getAddr());
			}else{
				mSelectBtn.setVisibility(View.GONE);
				mAddrView.setVisibility(View.GONE);
			}
			
			mNameView.setText(item.getCname());
			mPriceView.setText("预计价格: "+item.getPrice()+getString(R.string.rmb));
			mArriveTimeView.setText("预计时间: "+item.getArrive()+getString(R.string.arrive_with_some_day)+"(不含当天)");
			mPhoneView.setText(item.getMobi());
			
			mLoaderImageView.setURL(item.getLogo());
			
			return convertView;
		}	
	}

    //AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		/*if(flag==Flag.FLAG_GET_PARITY_COMPANY){
			
			Utils.sendMessage(mHandler, PARITY_COMPANY_GETTING);
		}*/
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_PARITY_COMPANY){      //正在获取比价后的网点信息
			
			Log.e("zyf","parity company list: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //比价后的网点信息获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray deliveryCompJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					ParityCompanyItem parityCompanyItem;
					parityCompanyItems.clear();
				    for(int i=0;i<deliveryCompJsonArray.length();i++){
				    	jsonObject=deliveryCompJsonArray.getJSONObject(i);
				    	
				    	parityCompanyItem=new ParityCompanyItem();
				    	parityCompanyItem.setArrive(jsonObject.getString("arrive"));
				    	parityCompanyItem.setPrice(jsonObject.getString("price"));
				    	parityCompanyItem.setMobi(jsonObject.getString("mobi"));
				    	parityCompanyItem.setOfficial(jsonObject.getString("official"));
				    	parityCompanyItem.setCname(jsonObject.getString("cname"));
				    	parityCompanyItem.setLogo(UrlConfigs.SERVER_URL+jsonObject.getString("logo"));
				    	parityCompanyItem.setAddr(jsonObject.getString("addr"));
				    	parityCompanyItem.setCid(jsonObject.getString("cid"));
				    	parityCompanyItem.setWid(jsonObject.getString("wid"));
				    	
				    	parityCompanyItems.add(parityCompanyItem);
				    }
				    
				    Utils.sendMessage(mHandler, PARITY_COMPANY_GET_SUCCESS);
				    
				    return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, PARITY_COMPANY_GET_FAILED);      //比价后的网点信息获取失败
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	//高德地图定位回调接口
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		
      if(amapLocation!=null&&amapLocation.getAMapException().getErrorCode() == 0) {    //定位成功
        	
        	Log.e("zyf",amapLocation.getLongitude()+","+amapLocation.getLatitude());
        	
        	lon=amapLocation.getLongitude()+"";
        	lat=amapLocation.getLatitude()+"";
        	
        	//调用获取比价后的网点信息接口
        	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_PARITY_URL+"?lon="+lon+"&lat="
        	+lat+"&s_did="+sdid+"&r_did="+rdid+"&weight="+weight+"&size="+"100";
        	
        	/*String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_PARITY_URL+"?lon="+347.66+"&lat="
                	+52.10+"&s_did="+1+"&r_did="+1+"&weight="+10+"&size="+"15";*/
        	
        	mGetParityCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PARITY_COMPANY, url, null, null);
    		mGetParityCompanyAsyncTaskDataLoader.setOnDataLoaderListener(this);
    		
    		mGetParityCompanyAsyncTaskDataLoader.execute();
			
		}else{
			Utils.sendMessage(mHandler, PARITY_COMPANY_GET_FAILED);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		super.onActivityResult(requestCode, resultCode, intent);
		
		if(requestCode==ActivityResultCode.CODE_PARITY_CITY_SELECTOR){
			if(intent!=null){
				
				int type=intent.getIntExtra("type", 0);
				
				if(type==0){   //获取编辑后的出发城市
					
					sdid=intent.getStringExtra("cid");
					sname=intent.getStringExtra("cname");
					
					mFromCityBtn.setText(sname);
					
					mFromCityBtn.setTextColor(Color.BLACK);
			
				}else{   //获取编辑后的目的地城市
					rdid=intent.getStringExtra("cid");
					rname=intent.getStringExtra("cname");

					mToCityBtn.setText(rname);
					
					mToCityBtn.setTextColor(Color.BLACK);
				}
			}
		}
	}

}
