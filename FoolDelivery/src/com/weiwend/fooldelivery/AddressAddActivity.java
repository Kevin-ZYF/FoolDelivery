package com.weiwend.fooldelivery;

import java.util.Iterator;

import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.customviews.MyToggleButton;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class AddressAddActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,AMapLocationListener,TextWatcher{
	
	//联系人姓名、手机号码、街道地址、邮政编码
	private EditText mUsernameEt,mPhoneNumberEt,mStreetEt,mZcodeEt;
	
	//省市区综合地址
	private TextView mPreAddressTv;
	
	//"保存"按钮、"自动定位"按钮
	private Button mSummitBtn,mLocationBtn;
	
	//默认地址设置按钮
	private MyToggleButton mToggleButton;
	
	//高德地图定位类
	private LocationManagerProxy mLocationManagerProxy;	
	
	//记录省、市、区的id
	private String pid,cid,did;
	
	//临时保存省、市、区的id，用于地址信息的模糊匹配
	private String pid1,cid1,did1;
	
	//记录省、市、区的名称
	private String pname,cname,dname;
	
	//临时保存省、市、区的名称，用于地址信息的模糊匹配
	private String pname1="",cname1="",dname1="";
	
	//标识“寄件地址”或者“发件地址”
	private String type;
	
	private AddressItem mAddressItem;
	
	//调用保存地址接口后的各种状态信息
	private final int ADDRESS_ADDING=1;
	private final int ADDRESS_ADD_SUCCESS=2;
	private final int ADDRESS_ADD_FAILED=3;
	
	//点击“自动定位”后进行地址模糊匹配的各种状态信息
	private final int ADDRESS_MATCHING=4;
	private final int ADDRESS_MATCH_SUCCESS=5;
	private final int ADDRESS_MATCH_FAILED=6;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//“添加地址”的异步加载类
	private AsyncTaskDataLoader mAddressAddAsyncTaskDataLoader;
	
	//获取省市区信息的异步加载类
	private AsyncTaskDataLoader mAddressAsyncTaskDataLoader;
	
	//根据各种状态信息更新ui等
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
		
            case ADDRESS_ADDING:   //添加地址的接口已经调用，正在等待服务器端的回复
            	
            	Log.e("zyf","address adding......");
            	
            	mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_address_add, AddressAddActivity.this);
				mProgressDialogUtils.showDialog();
            	
            	break;
			case ADDRESS_ADD_SUCCESS:    //添加地址成功
				
				Log.e("zyf","address add success......");
				
				MyToast.makeText(AddressAddActivity.this, getString(R.string.address_add_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				Intent intent=new Intent();
				intent.putExtra("AddressItem", mAddressItem);
				setResult(ActivityResultCode.CODE_ADDRESS_ADD, intent);
				
				finish();
			            	
			    break;
			case ADDRESS_ADD_FAILED:    //添加地址失败
				
				Log.e("zyf","address add failed......");
				
				MyToast.makeText(AddressAddActivity.this, getString(R.string.address_add_failed), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
            case ADDRESS_MATCHING:      //正在将通过定位sdk返回的地址信息与服务器端地址信息进行模糊匹配
				
				Log.e("zyf","address matching......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_location, AddressAddActivity.this);
				
				//用户取消自动定位操作
				mProgressDialogUtils.setProgressDialogCancelable();
				mProgressDialogUtils.setMyProgressDialogCanceledListener(new MyProgressDialogUtils.MyProgressDialogCanceledListener() {
					
					@Override
					public void canceled() {
						
						Log.e("zyf","cancel cancel cancel......");
						
						if(mLocationManagerProxy!=null){
							mLocationManagerProxy.removeUpdates(AddressAddActivity.this);
							mLocationManagerProxy.destroy();
						}
						
						if(mAddressAsyncTaskDataLoader!=null){
							mAddressAsyncTaskDataLoader.canceled();
						}
					}
				});
				
				mProgressDialogUtils.showDialog();
				
				break;
            case ADDRESS_MATCH_SUCCESS:   //自动定位模糊匹配成功
				
				Log.e("zyf","address match success......");
				
				pid=pid1;
				cid=cid1;
				did=did1;
				
				mPreAddressTv.setText(pname+cname+dname);
				
				updateSummitBtnStatus();
				
				MyToast.makeText(AddressAddActivity.this, getString(R.string.location_match_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case ADDRESS_MATCH_FAILED:      //自动定位模糊匹配失败
	
	            Log.e("zyf","address match failed......");
	            
	            MyToast.makeText(AddressAddActivity.this, getString(R.string.location_match_failed), MyToast.LENGTH_SHORT).show();
	
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
		
		setContentView(R.layout.activity_address_add);
		
		//初始化该条地址信息的type
		type=getIntent().getStringExtra("type");
		
		initViews();
		
		//开启定位功能
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.setGpsEnable(false);
	}
	
	//初始化ui界面
	private void initViews(){
		
		mPreAddressTv=(TextView)findViewById(R.id.mPreAddressTv);
		mPreAddressTv.setOnClickListener(this);
		
		mStreetEt=(EditText)findViewById(R.id.mStreetEt);
		mZcodeEt=(EditText)findViewById(R.id.mZcodeEt);
		mUsernameEt=(EditText)findViewById(R.id.mUsernameEt);
		mPhoneNumberEt=(EditText)findViewById(R.id.mPhoneNumberEt);
		
		mStreetEt.addTextChangedListener(this);
		mUsernameEt.addTextChangedListener(this);
		mPhoneNumberEt.addTextChangedListener(this);
		
		mStreetEt.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mSummitBtn.setOnClickListener(this);
		
		mLocationBtn=(Button)findViewById(R.id.mLocationBtn);
		mLocationBtn.setOnClickListener(this);
		
		mToggleButton=(MyToggleButton)findViewById(R.id.mToggleButton);
	}

	//初始化当前页面的标题，以及监听标题左端的返回功能
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_address_add));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_ADDRESS_ADD){   //开始添加地址信息
			Log.e("zyf","address adding......");
			Utils.sendMessage(mHandler, ADDRESS_ADDING);
		}else if(flag==Flag.FLAG_GET_PROVINCE){  //开始获取省份信息
			Log.e("zyf","get province......");
		}else if(flag==Flag.FLAG_GET_CITY){   //开始获取城市信息
			Log.e("zyf","get city......");
		}else if(flag==Flag.FLAG_GET_DISTRICT){  //开始获取区、县信息
			Log.e("zyf","get district......");
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		Log.e("zyf","task completed......result: "+result);
		
		if(flag==Flag.FLAG_ADDRESS_ADD){  //获取地址添加返回结果
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){     //地址添加成功
					
					Utils.sendMessage(mHandler, ADDRESS_ADD_SUCCESS);
					
					return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, ADDRESS_ADD_FAILED);  //地址添加成功
			
		}else if(flag==Flag.FLAG_GET_PROVINCE){   //获取省份信息
			Log.e("zyf","get province result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){         //获取省份信息成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //省份id
		                value=areaJsonObject.getString(key); //省份name
		                
		                if(value.contains(pname1)){    //省份信息的模糊匹配成功
		                	
		                	Log.e("zyf","privince match success......");
		                	
		                	pid1=key;
		                	
		                	//进行下一步的城市信息的模糊匹配
		                	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+key;
		    				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
		    				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressAddActivity.this);
		        			mAddressAsyncTaskDataLoader.execute();
		        			
		                	return;
		                }
		            }
		            
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","privince match failed......");
			
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);  //自动定位返回的地址信息与服务器端的地址信息模糊匹配失败
			
			return;
			
		}else if(flag==Flag.FLAG_GET_CITY){  //获取城市信息
			
            Log.e("zyf","get city result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //获取城市信息成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //城市id
		                value=areaJsonObject.getString(key); //城市name
		                
		                if(value.contains(cname1)){  //城市信息的模糊匹配成功
		                	
		                	Log.e("zyf","city match success......");
		                	
		                	cid1=key;
		                	
		                	//进行下一步的区/县信息的模糊匹配
		                	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+key;
		    				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
		    				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressAddActivity.this);
		        			mAddressAsyncTaskDataLoader.execute();
		        			
		                	return;
		                }
		            }
		            
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","city match failed......");
			
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);  //自动定位返回的地址信息与服务器端的地址信息模糊匹配失败
			
			return;
		}else if(flag==Flag.FLAG_GET_DISTRICT){   //获取区、县信息
			
            Log.e("zyf","get district result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){   //获取区、县信息成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //区、县id
		                value=areaJsonObject.getString(key); //区、县name
		                
		                if(value.contains(dname1)){  //区、县信息模糊匹配成功
		                	Log.e("zyf","district match success......");
		                	
		                	did1=key;
		                	
		                	Utils.sendMessage(mHandler, ADDRESS_MATCH_SUCCESS);
		                	
		                	return;
		                }
		            }
		            
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","district match failed......");
			
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);  //自动定位返回的地址信息与服务器端的地址信息模糊匹配失败
			
			return;
		}
		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:  //标题栏的返回功能
			finish();
			break;
		case R.id.mSummitBtn:  //地址信息添加，调用地址添加的接口
			
			String addr=mStreetEt.getText().toString();
			String zcod=mZcodeEt.getText().toString();
			String name=mUsernameEt.getText().toString();
			String telp=mPhoneNumberEt.getText().toString();
			
			if(addr.length()==0||name.length()==0||telp.length()==0){
				MyToast.makeText(AddressAddActivity.this, getString(R.string.please_input_info_completely), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(telp.length()!=11){
				MyToast.makeText(AddressAddActivity.this, getString(R.string.prompt_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(cid==null){
				
				MyToast.makeText(AddressAddActivity.this, getString(R.string.prompt_pcd_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			mAddressItem=new AddressItem();
			mAddressItem.setpId(pid);
			mAddressItem.setcId(cid);
			mAddressItem.setdId(did);
			mAddressItem.setpName(pname);
			mAddressItem.setcName(cname);
			mAddressItem.setdName(dname);
			mAddressItem.setAddress(addr);
			mAddressItem.setZcode(zcod);
			mAddressItem.setName(name);
			mAddressItem.setTelp(telp);
			
			String content=formatContent(type, addr, zcod, name, telp);
			
			if(mAddressAddAsyncTaskDataLoader!=null){
				mAddressAddAsyncTaskDataLoader.canceled();
			}
			
			mAddressAddAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_ADDRESS_ADD, UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_ADD_URL, null, content);
			mAddressAddAsyncTaskDataLoader.setOnDataLoaderListener(this);
			mAddressAddAsyncTaskDataLoader.execute();
			
			break;
       case R.id.mLocationBtn:      //自动定位功能
			
			Utils.sendMessage(mHandler, ADDRESS_MATCHING);
			
			mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, AddressAddActivity.this);
			
			break;
	
       case R.id.mPreAddressTv:     //编辑省市区信息
    	   
    	   Intent intent=new Intent(this,AddressSelectorActivity.class);
   		   startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_SELECTOR);
   		   
    	   break;

		default:
			break;
		}
	}
	
	//格式化“地址添加”接口的json数据
	private String formatContent(String type,String addr,String zcod,String name,String telp){
		int def;
		if(mToggleButton.getToggleStatus()){
			def=1;
		}else{
			def=0;
		}
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
	               +"\"type\":"+"\""+type+"\""+","
	               +"\"pid\":"+"\""+pid+"\""+","
	               +"\"cid\":"+"\""+cid+"\""+","
	               +"\"did\":"+"\""+did+"\""+","
	               +"\"addr\":"+"\""+addr+"\""+","
	               +"\"zcod\":"+"\""+zcod+"\""+","
	               +"\"def\":"+"\""+def+"\""+","
	               +"\"name\":"+"\""+name+"\""+","
				   +"\"telp\":"+"\""+telp+"\""+"}";
		return str;
	}
   
   //获取地址编辑后的省市区信息
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_ADDRESS_SELECTOR){
			
			if(data!=null){
				AddressItem item=(AddressItem) data.getSerializableExtra("AddressItem");
				Log.e("zyf",item.getpName()+item.getcName()+item.getdName());
				
				pid=item.getpId();
				cid=item.getcId();
				did=item.getdId();
				
				pname=item.getpName();
				cname=item.getcName();
				dname=item.getdName();
				
				mPreAddressTv.setText(pname+" "+cname+" "+dname);
				
				updateSummitBtnStatus();
			}
			
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
	
	//监听定位sdk的返回结果
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		
		//定位成功
		if(amapLocation!=null&&amapLocation.getAMapException().getErrorCode() == 0) {
        	
        	pname=amapLocation.getProvince();
        	cname=amapLocation.getCity();
        	dname=amapLocation.getDistrict();
        	
        	pname1=pname.replace(getString(R.string.province), "");
        	cname1=cname.replace(getString(R.string.city), "");
        	dname1=dname.replace(getString(R.string.county), "").replace(getString(R.string.district), "");
			
			Log.e("zyf","province: "+pname1);
			Log.e("zyf","city: "+cname1);
			Log.e("zyf","district: "+dname1);
			
			//开始进行省市区信息的模糊匹配
			mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_PROVINCE, UrlConfigs.SERVER_URL+UrlConfigs.GET_PROVINCE_URL, null, null);
			mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressAddActivity.this);
			mAddressAsyncTaskDataLoader.execute();
			
		}else{
			Log.e("zyf","get amp location failed......");
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);
		}
		
	}
	
	//停止定位
	@Override
	protected void onPause() {
		super.onPause();
		mLocationManagerProxy.removeUpdates(this);
		mLocationManagerProxy.destroy();
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
		updateSummitBtnStatus();
		
	}
	
	//用于更新“保存”按钮的状态，只有当所有信息都合法时，用户才可点击"保存"按钮
    private void updateSummitBtnStatus(){
		
		if(mUsernameEt.getText().toString().length()>0&&mPhoneNumberEt.getText().toString().length()==11
				&&mStreetEt.getText().toString().length()>0
				&&pid!=null
				&&pid.length()>0){
			mSummitBtn.setEnabled(true);
		}else{
			mSummitBtn.setEnabled(false);
		}
	}

}
