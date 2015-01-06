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
import android.view.View;
import android.view.View.OnClickListener;
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

public class AddressModifyActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,AMapLocationListener,TextWatcher{
	
	//联系人姓名、手机号码、街道地址、邮政编码
	private EditText mNameEt,mPhoneEt,mAddressEt,mZcodeEt;
	
	//省市区综合地址
	private TextView mPreAddressTv;
	
	//"保存"按钮、"自动定位"按钮
	private Button mSummitBtn,mLocationBtn;
	
	//默认地址设置按钮
	private MyToggleButton mToggleButton;
	
	//索要编辑的地址信息对象
	private AddressItem mAddressItem;
	
	//记录省、市、区的id
	private String pid,cid,did;
	
	//临时保存省、市、区的id，用于地址信息的模糊匹配
	private String pid1,cid1,did1;
	
	//记录省、市、区的名称
	private String pname="",cname="",dname="";
	
	//临时保存省、市、区的名称，用于地址信息的模糊匹配
	private String pname1="",cname1="",dname1="";
	
	//调用编辑地址接口后的各种状态信息
	private final int ADDRESS_MODIFYING=0;
	private final int ADDRESS_MODIFY_SUCCESS=1;
	private final int ADDRESS_MODIFY_FAILED=2;
	
	//点击“自动定位”后进行地址模糊匹配的各种状态信息
	private final int ADDRESS_MATCHING=3;
	private final int ADDRESS_MATCH_SUCCESS=4;
	private final int ADDRESS_MATCH_FAILED=5;
	
	//“编辑地址”的异步加载类
	private AsyncTaskDataLoader mAddressModifyAsyncTaskDataLoader;
	
	//获取省市区信息的异步加载类
	private AsyncTaskDataLoader mAddressAsyncTaskDataLoader;
	
	//高德地图定位类
	private LocationManagerProxy mLocationManagerProxy;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//根据各种状态信息更新ui等
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case ADDRESS_MODIFYING:    //编辑地址的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","address modifying......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_address_modify, AddressModifyActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case ADDRESS_MODIFY_SUCCESS:  //编辑地址成功
				
				Log.e("zyf","address modify success......");
				
				MyToast.makeText(AddressModifyActivity.this, getString(R.string.address_modify_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent intent=new Intent();        //返回调用该页面的activity
				intent.putExtra("isModified", true);
				setResult(ActivityResultCode.CODE_ADDRESS_MODIFY,intent);
				
				finish();
            	
				break;
			case ADDRESS_MODIFY_FAILED:   //编辑地址失败
				
				Log.e("zyf","address modify failed......");
				
				MyToast.makeText(AddressModifyActivity.this, getString(R.string.address_modify_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
		    case ADDRESS_MATCHING:      //正在将通过定位sdk返回的地址信息与服务器端地址信息进行模糊匹配
				
				Log.e("zyf","address matching......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_location, AddressModifyActivity.this);
				
				//用户取消自动定位操作
				mProgressDialogUtils.setProgressDialogCancelable();
				mProgressDialogUtils.setMyProgressDialogCanceledListener(new MyProgressDialogUtils.MyProgressDialogCanceledListener() {
					
					@Override
					public void canceled() {
						
						Log.e("zyf","cancel cancel cancel......");
						
						if(mLocationManagerProxy!=null){
							mLocationManagerProxy.removeUpdates(AddressModifyActivity.this);
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
				
				MyToast.makeText(AddressModifyActivity.this, getString(R.string.location_match_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case ADDRESS_MATCH_FAILED:   //自动定位模糊匹配失败
	
	            Log.e("zyf","address match failed......");
	            
	            MyToast.makeText(AddressModifyActivity.this, getString(R.string.location_match_failed), MyToast.LENGTH_SHORT).show();
	
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
		
		//获取传递给该页面的地址信息，用于初始化界面
		mAddressItem=(AddressItem)getIntent().getSerializableExtra("AddressItem");
		if(mAddressItem==null){
			return;
		}
		
		pid=mAddressItem.getpId();
		cid=mAddressItem.getcId();
		did=mAddressItem.getdId();
		
		initViews();
		
		updateSummitBtnStatus();
		
		//开启定位功能
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.setGpsEnable(false);
	}
	
	//初始化ui
	private void initViews(){
		
		mPreAddressTv=(TextView)findViewById(R.id.mPreAddressTv);
		mPreAddressTv.setOnClickListener(this);
		
		mPreAddressTv.setText(mAddressItem.getpName()+mAddressItem.getcName()+mAddressItem.getdName());
		
		mNameEt=(EditText)findViewById(R.id.mUsernameEt);
		mAddressEt=(EditText)findViewById(R.id.mStreetEt);
		mZcodeEt=(EditText)findViewById(R.id.mZcodeEt);
		mPhoneEt=(EditText)findViewById(R.id.mPhoneNumberEt);
		
		mNameEt.setText(mAddressItem.getName());
		mAddressEt.setText(mAddressItem.getAddress());
		mPhoneEt.setText(mAddressItem.getTelp());
		mZcodeEt.setText(mAddressItem.getZcode());
		
		mNameEt.addTextChangedListener(this);
		mAddressEt.addTextChangedListener(this);
		mPhoneEt.addTextChangedListener(this);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mSummitBtn.setOnClickListener(this);
		
		mLocationBtn=(Button)findViewById(R.id.mLocationBtn);
		mLocationBtn.setOnClickListener(this);
		
		mToggleButton=(MyToggleButton)findViewById(R.id.mToggleButton);
		
		if(mAddressItem.getDef()==0){  //非默认地址
			mToggleButton.setToggleStatus(false);
		}else{    //默认地址
			mToggleButton.setToggleStatus(true);
		}
	}

	//初始化当前页面的标题，以及监听标题左端的返回功能
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_address_modify));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_ADDRESS_MODIFY){      //开始调用地址编辑接口
			Utils.sendMessage(mHandler, ADDRESS_MODIFYING);
		}else if(flag==Flag.FLAG_GET_PROVINCE){  //开始调用省份信息接口
			Log.e("zyf","get province......");
		}else if(flag==Flag.FLAG_GET_CITY){     //开始调用城市信息接口
			Log.e("zyf","get city......");
		}else if(flag==Flag.FLAG_GET_DISTRICT){    //开始调用区、县信息接口
			Log.e("zyf","get district......");
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_ADDRESS_MODIFY){     //获取地址编辑返回结果
			Log.e("zyf","address modify result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){       //地址编辑成功
					
					Utils.sendMessage(mHandler, ADDRESS_MODIFY_SUCCESS);
					
					return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, ADDRESS_MODIFY_FAILED);   //地址编辑失败
			
		}else if(flag==Flag.FLAG_GET_PROVINCE){   //获取省份信息返回结果
			
			Log.e("zyf","get province result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){        //省份信息获取成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //省份id
		                value=areaJsonObject.getString(key); //省份name
		                
		                if(value.contains(pname1)){   //省份信息模糊匹配成功
		                	
		                	Log.e("zyf","province match success......");
		                	
		                	pid1=key;
		                	
		                	//进行城市信息的模糊匹配
		                	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CITY_URL+"?prov_id="+key;
		    				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CITY, url, null, null);
		    				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressModifyActivity.this);
		        			mAddressAsyncTaskDataLoader.execute();
		        			
		                	return;
		                }
		            }
		            
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","province match failed......");
			
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);    //自动定位返回的地址信息与服务器端的地址信息模糊匹配失败
			
			return;
			
		}else if(flag==Flag.FLAG_GET_CITY){   //获取城市信息返回结果
			
            Log.e("zyf","get city result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){     //城市信息获取成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //城市id
		                value=areaJsonObject.getString(key); //城市name
		                
		                if(value.contains(cname1)){    //城市信息模糊匹配成功
		                	Log.e("zyf","city match success......");
		                	
		                	cid1=key;
		                	
		                	//进行区、县信息的模糊匹配
		                	String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DISTRICT_URL+"?city_id="+key;
		    				mAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DISTRICT, url, null, null);
		    				mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressModifyActivity.this);
		        			mAddressAsyncTaskDataLoader.execute();
		        			
		                	return;
		                }
		            }
		            
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","city match failed......");
			
			Utils.sendMessage(mHandler, ADDRESS_MATCH_FAILED);   //自动定位返回的地址信息与服务器端的地址信息模糊匹配失败
			
			return;
		}else if(flag==Flag.FLAG_GET_DISTRICT){  //获取区、县信息返回结果
			
            Log.e("zyf","get district result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				Log.e("zyf: ","msg: "+totalJsonObject.getString("msg"));
				Log.e("zyf: ","rc: "+totalJsonObject.getString("rc"));
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){     //区、县信息获取成功
					
					JSONObject areaJsonObject=totalJsonObject.getJSONObject("data");
					Iterator it = areaJsonObject.keys();
					String key,value;
		            while(it.hasNext()) {  
		                key=(String)it.next();   //区、县id
		                value=areaJsonObject.getString(key); //区、县name
		                
		                if(value.contains(dname1)){   //区、县信息模糊匹配成功
		                	Log.e("zyf","district match success......");
		                	
		                	did1=key;
		                	
		                	Utils.sendMessage(mHandler, ADDRESS_MATCH_SUCCESS);   //自动定位返回的地址信息与服务器端的地址信息模糊匹配成功
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
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.mSummitBtn:   //地址信息编辑完毕，调用地址编辑的接口
			
			String addr=mAddressEt.getText().toString();
			String zcod=mZcodeEt.getText().toString();
			String name=mNameEt.getText().toString();
			String telp=mPhoneEt.getText().toString();
			
			if(addr.length()==0||name.length()==0||telp.length()==0){
				MyToast.makeText(AddressModifyActivity.this, getString(R.string.please_input_info_completely), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(telp.length()!=11){
				MyToast.makeText(AddressModifyActivity.this, getString(R.string.prompt_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
            String content=formatContent();
			
			if(mAddressModifyAsyncTaskDataLoader!=null){
				mAddressModifyAsyncTaskDataLoader.canceled();
			}
			
			mAddressModifyAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_ADDRESS_MODIFY, UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_MODIFY_URL, null, content);
			mAddressModifyAsyncTaskDataLoader.setOnDataLoaderListener(this);
			mAddressModifyAsyncTaskDataLoader.execute();
			
			break;
		case R.id.mPreAddressTv:    //编辑省市区信息
			
			Intent intent=new Intent(AddressModifyActivity.this,AddressSelectorActivity.class);
			startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_SELECTOR);
			
			break;
		case R.id.mLocationBtn:    //自动定位功能
			
			Utils.sendMessage(mHandler, ADDRESS_MATCHING);
			
			mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, AddressModifyActivity.this);
			
			break;
		default:
			break;
		}
	}
	
	 //获取地址编辑后的省市区信息
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_ADDRESS_SELECTOR){
			if(data==null){
				return;
			}
			AddressItem item=(AddressItem) data.getSerializableExtra("AddressItem");
			Log.e("zyf",item.getpName()+item.getcName()+item.getdName());
			
			mPreAddressTv.setText(item.getpName()+item.getcName()+item.getdName());
		}
	}
	
	//格式化“地址编辑”接口的json数据
	private String formatContent(){
		int def;
		if(mToggleButton.getToggleStatus()){
			def=1;
		}else{
			def=0;
		}
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
				   +"\"id\":"+"\""+mAddressItem.getId()+"\""+","
	               +"\"type\":"+"\""+mAddressItem.getType()+"\""+","
	               +"\"pid\":"+"\""+pid+"\""+","
	               +"\"cid\":"+"\""+cid+"\""+","
	               +"\"did\":"+"\""+did+"\""+","
	               +"\"def\":"+"\""+def+"\""+","
	               +"\"addr\":"+"\""+mAddressEt.getText().toString()+"\""+","
	               +"\"zcod\":"+"\""+mZcodeEt.getText().toString()+"\""+","
	               +"\"name\":"+"\""+mNameEt.getText().toString()+"\""+","
				   +"\"telp\":"+"\""+mPhoneEt.getText().toString()+"\""+"}";
		return str;
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
			mAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressModifyActivity.this);
			mAddressAsyncTaskDataLoader.execute();
			
		}else{
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
	
	//用于更新“确定”按钮的状态，只有当所有信息都合法时，用户才可点击"确定"按钮
    private void updateSummitBtnStatus(){
		
		if(mNameEt.getText().toString().length()>0&&mPhoneEt.getText().toString().length()==11
				&&mAddressEt.getText().toString().length()>0
				&&pid!=null
				&&pid.length()>0){
			mSummitBtn.setEnabled(true);
		}else{
			mSummitBtn.setEnabled(false);
		}
	}

}
