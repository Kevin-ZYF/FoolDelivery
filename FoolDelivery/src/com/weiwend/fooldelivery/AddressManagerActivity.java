package com.weiwend.fooldelivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.DelSlideListView;
import com.weiwend.fooldelivery.customviews.MyDeleteDialog;
import com.weiwend.fooldelivery.customviews.MyTitleTabView;
import com.weiwend.fooldelivery.customviews.MyTitleTabView.MyOnTitleClickLister;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.customviews.OnDeleteListioner;
import com.weiwend.fooldelivery.fragments.SenderHistoryFragment;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class AddressManagerActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOnTitleClickLister,OnRefreshListener<ScrollView>{
	
	//标题栏右侧的“添加”按钮
	//private Button mAddressAddBtn;
	
	//用于“寄件人地址”、“收件人地址”切换的view
	private MyTitleTabView mTitleTabView;
	
	//显示寄件人、发件人地址的listview
	private DelSlideListView mSendAddressListView,mRecipientAddressListView;
	
	//寄件人、发件人地址的listview的适配器
	private MyDeletedListViewBaseAdapter mSendAdapter,mRecipientAdapter;
	
	//主要用于解决listview中item侧滑功能与“下拉刷新”功能的冲突
	private PullToRefreshScrollView mRefreshScrollview;
	
	//寄件人信息的数据源
	private ArrayList<AddressItem> mSendAddressList=new ArrayList<AddressItem>();
	
	//发件人信息的数据源
	private ArrayList<AddressItem> mRecipientAddressList=new ArrayList<AddressItem>();
	
	//获取所有地址信息、删除指定地址信息的异步加载类
	private AsyncTaskDataLoader mGetAllAddressAsyncTaskDataLoader,mDeleteAddressAsyncTaskDataLoader;
	
	//调用获取所有地址接口的各种状态信息
	private final int ALL_ADDRESS_GETTING=0;
	private final int ALL_ADDRESS_GET_SUCCESS=1;
	private final int ALL_ADDRESS_GET_FAILED=2;
	
	//调用删除指定地址接口的各种状态信息
	private final int ADDRESS_DELETING=3;
	private final int ADDRESS_DELETE_SUCCESS=4;
	private final int ADDRESS_DELETE_FAILED=5;
	
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case ALL_ADDRESS_GETTING:
            	
				Log.e("zyf","all address getting......");
				
				mode=MODE_GET_ALL_ADDRESS;
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_address, AddressManagerActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case ALL_ADDRESS_GET_SUCCESS:
				
				Log.e("zyf","all address get success......");
				
				mRefreshScrollview.onRefreshComplete();
				
				mode=-1;
				
				if(mSendAdapter==null){
					mSendAdapter=new MyDeletedListViewBaseAdapter(AddressManagerActivity.this, mSendAddressList);
					mSendAddressListView.setAdapter(mSendAdapter);
					Utils.setListViewHeightBasedOnChildren(mSendAddressListView);
					mSendAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			            @Override
			            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			               
			                if("0".equals(isNeedReturn)){   //将选择的地址信息返回给调用该页面的activity
								AddressItem item=mSendAddressList.get(position);
								Intent intent=new Intent();
								intent.putExtra("AddressItem", item);
								if("0".equals(type)){    //用户调用该页面时为了选择寄件人地址
									setResult(ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR, intent);
								}else{     //用户调用该页面时为了选择收件人地址
									setResult(ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR, intent);
								}
								
								finish();
							}else{     //跳入地址编辑页面
								
								Intent intent=new Intent(AddressManagerActivity.this,AddressModifyActivity.class);
								AddressItem item=mSendAddressList.get(position);
								intent.putExtra("AddressItem", item);
								startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_MODIFY);
							}
			            }
			        });
					mSendAdapter.setOnDeleteListioner(mSendOnDeleteListioner);
					mSendAddressListView.setDeleteListioner(mSendOnDeleteListioner);
				}else{
					mSendAddressListView.refresh();
					mSendAdapter.notifyDataSetChanged();
					Utils.setListViewHeightBasedOnChildren(mSendAddressListView);
				}
				
				if(mRecipientAdapter==null){
					mRecipientAdapter=new MyDeletedListViewBaseAdapter(AddressManagerActivity.this, mRecipientAddressList);
					mRecipientAddressListView.setAdapter(mRecipientAdapter);
					Utils.setListViewHeightBasedOnChildren(mRecipientAddressListView);
					mRecipientAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			            @Override
			            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			                
			                if("0".equals(isNeedReturn)){   //将选择的地址信息返回给调用该页面的activity
								AddressItem item=mRecipientAddressList.get(position);
								Intent intent=new Intent();
								intent.putExtra("AddressItem", item);
								if("0".equals(type)){   //用户调用该页面时为了选择寄件人地址
									setResult(ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR, intent);
								}else{   //用户调用该页面时为了选择发件人地址
									setResult(ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR, intent);
								}
								
								finish();
							}else{     //跳入地址编辑页面
								
								Intent intent=new Intent(AddressManagerActivity.this,AddressModifyActivity.class);
								AddressItem item=mRecipientAddressList.get(position);
								intent.putExtra("AddressItem", item);
								startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_MODIFY);
							}
			            }
			        });
					mRecipientAdapter.setOnDeleteListioner(mRecipientOnDeleteListioner);
					mRecipientAddressListView.setDeleteListioner(mRecipientOnDeleteListioner);
				}else{
					mRecipientAddressListView.refresh();
					mRecipientAdapter.notifyDataSetChanged();
					Utils.setListViewHeightBasedOnChildren(mRecipientAddressListView);
				}
				
                MyToast.makeText(AddressManagerActivity.this, getString(R.string.address_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case ALL_ADDRESS_GET_FAILED:
				
				Log.e("zyf","all address get failed......");
				
				mRefreshScrollview.onRefreshComplete();
				
                MyToast.makeText(AddressManagerActivity.this, getString(R.string.address_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case ADDRESS_DELETING:
				
				Log.e("zyf","address deleting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_address_deleting, AddressManagerActivity.this);
				
				mProgressDialogUtils.showDialog();
				
				break;
			case ADDRESS_DELETE_SUCCESS:
							
				Log.e("zyf","address delete success......");
				
				if(deleteMode==0){  //删除发件人
					mSendAddressList.remove(deletePosition);
					mSendAddressListView.deleteItem();
					mSendAdapter.notifyDataSetChanged();
				}else{  //删除收件人
					
					mRecipientAddressList.remove(deletePosition);
					mRecipientAddressListView.deleteItem();
					mRecipientAdapter.notifyDataSetChanged();
				}
				
                MyToast.makeText(AddressManagerActivity.this, getString(R.string.address_delete_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case ADDRESS_DELETE_FAILED:
				
				Log.e("zyf","address delete failed......");
				
			    MyToast.makeText(AddressManagerActivity.this, getString(R.string.address_delete_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//用于记录用户进入该界面时，所要选择地址信息的类型(寄件人地址/发件人地址)
	private String type;
	
	//用于标识点击地址item时，是否需要返回
	private String isNeedReturn;
	
	//0:删除发件人地址  1:删除收件人地址
	private int deleteMode;
	
	//记录地址信息删除成功后的position,主要用于更新地址listview
	private int deletePosition;
	
	//主要用于sesn过期后，用户重新登录成功后，是否需要重新获取所有地址信息
	private int mode=-1;
	private int MODE_GET_ALL_ADDRESS=0;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_address_manager);
		
		isNeedReturn=getIntent().getStringExtra("isNeedReturn");
		
		type=getIntent().getStringExtra("type");
		
		initViews();
		
		//获取所有寄件人、发件人地址信息
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
		mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
		mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mGetAllAddressAsyncTaskDataLoader.execute();
		
	}
	
	//初始化ui信息
	private void initViews(){
		
		/*mAddressAddBtn=(Button)findViewById(R.id.mAddressAddBtn);
		mAddressAddBtn.setOnClickListener(this);*/
		
		mSendAddressListView=(DelSlideListView)findViewById(R.id.mSendAddressListView);
		mRecipientAddressListView=(DelSlideListView)findViewById(R.id.mRecipientAddressListView);
		
		mRefreshScrollview=(PullToRefreshScrollView)findViewById(R.id.mRefreshScrollview);
		mRefreshScrollview.setOnRefreshListener(this);
		
		mTitleTabView=(MyTitleTabView)findViewById(R.id.mTitleTabView);
		mTitleTabView.setTabNames(getString(R.string.address_info_sender), getString(R.string.address_info_recipient));
		mTitleTabView.setOnTitleClickListener(this);
		
		if(type!=null&&type.equals("1")){  //用户进入该界面用于选择“收件人地址”
			mTitleTabView.setCurItem(1);
		}else{
			mTitleTabView.setCurItem(0);
		}

	}
	
	//寄件人地址listview侧滑功能的监听器
	private OnDeleteListioner mSendOnDeleteListioner=new OnDeleteListioner() {
		
		//监听“删除”功能
		@Override
		public void onDelete(int position) {
			
			final int pos=position;
			
			Log.e("zyf","delete send position: "+position);
			
			MyDeleteDialog mDialog=new MyDeleteDialog(AddressManagerActivity.this, R.style.MyDialog);
			mDialog.setMySubmmitListener(new MyDeleteDialog.MySubmmitListener() {
				
				@Override
				public void delete() {
					
					deleteMode=0;
			        deletePosition=pos;
			        
			        //调用删除指定寄件人地址的接口
			        String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_DELETE_URL+"?sesn="+MyApplicaition.sesn+"&id="+mSendAddressList.get(deletePosition).getId();
			        mDeleteAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_ADDRESS_DELETE, url, null, null);
			        mDeleteAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressManagerActivity.this);
			        mDeleteAddressAsyncTaskDataLoader.execute();
				}
			});
			mDialog.show();
		}
		
		@Override
		public void onBack() {
		}
		
		@Override
		public boolean isCandelete(int position) {
			return true;
		}

		//监听“编辑”功能
		@Override
		public void onEdit(int position) {
			
			Log.e("zyf","edit edit edit......");
			
			Intent intent=new Intent(AddressManagerActivity.this,AddressModifyActivity.class);
			AddressItem item=mSendAddressList.get(position);
			intent.putExtra("AddressItem", item);
			startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_MODIFY);
		}
	};
	
	//收件人地址listview侧滑功能的监听器
    private OnDeleteListioner mRecipientOnDeleteListioner=new OnDeleteListioner() {
		
    	//监听"删除"功能
		@Override
		public void onDelete(int position) {
			
			final int pos=position;
			
			MyDeleteDialog mDialog=new MyDeleteDialog(AddressManagerActivity.this, R.style.MyDialog);
			mDialog.setMySubmmitListener(new MyDeleteDialog.MySubmmitListener() {
				
				@Override
				public void delete() {
					
					Log.e("zyf","delete recipient position: "+pos);
					
					deleteMode=1;
			        deletePosition=pos;
			        
			        String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_DELETE_URL+"?sesn="+MyApplicaition.sesn+"&id="+mRecipientAddressList.get(deletePosition).getId();
			        mDeleteAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_ADDRESS_DELETE, url, null, null);
			        mDeleteAddressAsyncTaskDataLoader.setOnDataLoaderListener(AddressManagerActivity.this);
			        mDeleteAddressAsyncTaskDataLoader.execute();
				}
			});
			mDialog.show();
		}
		
		@Override
		public void onBack() {
		}
		
		@Override
		public boolean isCandelete(int position) {
			return true;
		}

		//监听"编辑"功能
		@Override
		public void onEdit(int position) {
			
			Log.e("zyf","edit edit edit......");
			
			Intent intent=new Intent(AddressManagerActivity.this,AddressModifyActivity.class);
			AddressItem item=mRecipientAddressList.get(position);
			intent.putExtra("AddressItem", item);
			startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_MODIFY);
		}
	};

	//初始化当前页面的标题
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView); 
		mTitleView.setText(getString(R.string.title_address_manager));
		
		Button rigthBtn=(Button)findViewById(R.id.rightBtn);
		rigthBtn.setVisibility(View.VISIBLE);
		rigthBtn.setText(getString(R.string.add));
		rigthBtn.setOnClickListener(this);
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_ALL_ADDRESS){  //开始获取"寄件地址"、"发件地址"信息
			Utils.sendMessage(mHandler, ALL_ADDRESS_GETTING);
		}else if(flag==Flag.FLAG_ADDRESS_DELETE){   //开始删除指定地址信息
			Utils.sendMessage(mHandler, ADDRESS_DELETING);
		}
	}
	
	//用于记录默认的寄件人地址
	private AddressItem defaultSendAddressItem;
	
	//用于记录默认的发件人地址
	private AddressItem defaultRecipientAddressItem;

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_ALL_ADDRESS){
			
			try {
				
				Log.e("zyf","address: "+result);
				
				JSONObject totalJsonObject=new JSONObject(result);						
				
				String rc=totalJsonObject.getString("rc");
				
				Log.e("zyf: ","rc: "+rc);
				
				if("0".equals(rc)){    //寄件人、发件人地址获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, ALL_ADDRESS_GET_SUCCESS);
						
						return;
					}
					
					JSONArray addressJSONArray=dataJsonObject.getJSONArray("items");
					
					mSendAddressList.clear();
					mRecipientAddressList.clear();
					defaultSendAddressItem=null;
					defaultRecipientAddressItem=null;
					
					AddressItem item;
					JSONObject jsonObject;
					
					for(int i=0;i<addressJSONArray.length();i++){
						
						item=new AddressItem();
						
						jsonObject=addressJSONArray.getJSONObject(i);
						
						item.setId(jsonObject.getString("id"));
						item.setType(jsonObject.getString("typ"));
						item.setpId(jsonObject.getString("pid"));
						item.setcId(jsonObject.getString("cid"));
						item.setdId(jsonObject.getString("did"));
						item.setpName(jsonObject.getString("pname"));
						item.setcName(jsonObject.getString("cname"));
						item.setdName(jsonObject.getString("dname"));
						item.setAddress(jsonObject.getString("addr"));
						item.setZcode(jsonObject.getString("zcod"));
						item.setName(jsonObject.getString("name"));
						item.setTelp(jsonObject.getString("telp"));
						item.setDef(jsonObject.getInt("def"));
						
						if(item.getType().equals("0")){
							
							if(item.getDef()==0){  //非默认地址
								mSendAddressList.add(item);
							}else{
								defaultSendAddressItem=item;    //保存默认寄件人地址
							}
						}else{
							
							if(item.getDef()==0){  //非默认地址
								mRecipientAddressList.add(item);
							}else{
								defaultRecipientAddressItem=item;  //保存默认收件人地址
							}
							
						}
					}
					
					//默认寄件人地址显示在最前端
					if(defaultSendAddressItem!=null){
						mSendAddressList.add(0, defaultSendAddressItem);
					}
					
					//默认收件人地址显示在最前端
					if(defaultRecipientAddressItem!=null){
						mRecipientAddressList.add(0, defaultRecipientAddressItem);
					}
		            
		            Utils.sendMessage(mHandler, ALL_ADDRESS_GET_SUCCESS);
		            
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
			
			Utils.sendMessage(mHandler, ALL_ADDRESS_GET_FAILED);  //寄件人、发件人地址获取失败
			
            return;
            
		}else if(flag==Flag.FLAG_ADDRESS_DELETE){
			try {
				
				Log.e("zyf","address delete result: "+result);
				
				JSONObject totalJsonObject=new JSONObject(result);						
				
				String rc=totalJsonObject.getString("rc");
				
				Log.e("zyf: ","rc: "+rc);
				
				if("0".equals(rc)){  //指定地址删除成功
		            
		            Utils.sendMessage(mHandler, ADDRESS_DELETE_SUCCESS);
		            
		            return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf","address delete exception: "+e.toString());
			}
			
			Utils.sendMessage(mHandler, ADDRESS_DELETE_FAILED);  //指定地址删除失败
			
            return;
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:   //页面返回功能
			finish();
			break;
		case R.id.rightBtn:  //添加新地址
			Intent intent2=new Intent(this,AddressAddActivity.class);
			intent2.putExtra("type", type);
			startActivityForResult(intent2, ActivityResultCode.CODE_ADDRESS_ADD);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_ADDRESS_MODIFY){  //地址信息修改成功
			
			//地址信息编辑成功，刷新页面
			if(data!=null){
				
				if(data.getBooleanExtra("isModified", false)){
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
					mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
					mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
					mGetAllAddressAsyncTaskDataLoader.execute();
				}
			}
		}else if(requestCode==ActivityResultCode.CODE_ADDRESS_ADD){ 
			
			//地址信息添加成功，刷新页面
			if(data!=null){
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
				mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
				mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
				mGetAllAddressAsyncTaskDataLoader.execute();
			}
		}else if(requestCode==ActivityResultCode.CODE_LOGIN){
			
			if(data!=null){
				
				//用户登录成功，重新获取所有"寄件地址"、"发件地址"信息
				if(data.getBooleanExtra("loginStatus", false)){
					Log.e("zyf","login status : true");
					
					if(mode==MODE_GET_ALL_ADDRESS){
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
						mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
						mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
						mGetAllAddressAsyncTaskDataLoader.execute();
					}
				}
			}
		}
	}
	
	//寄件地址listview的适配器
    /*class MySendBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MySendBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mSendAddressList.size();
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
				convertView=mInflater.inflate(R.layout.item_swipe_listview_address, null);
			}
			
			AddressItem item;
			item=mSendAddressList.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mNameView);
			TextView mPhoneView=(TextView)convertView.findViewById(R.id.mPhoneView);
			
			TextView mPreAddressView=(TextView)convertView.findViewById(R.id.mPreAddressView);
			TextView mDetailAddressView=(TextView)convertView.findViewById(R.id.mDetailAddressView);
			
			mTitleView.setText(item.getName());
			mPhoneView.setText(item.getTelp());
			
			mPreAddressView.setText(item.getpName()+item.getcName()+item.getdName());
			mDetailAddressView.setText(item.getAddress());
			
			return convertView;
		}	
	}*/
    
    //发件地址listview的适配器
    class MyRecipientBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyRecipientBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mRecipientAddressList.size();
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
				convertView=mInflater.inflate(R.layout.item_address, null);
			}
			
			AddressItem item;
			item=mRecipientAddressList.get(position);
			
			TextView mTitleView=(TextView)convertView.findViewById(R.id.mTitleView);
			TextView mPhoneView=(TextView)convertView.findViewById(R.id.mPhoneView);
			
			TextView mPreAddressView=(TextView)convertView.findViewById(R.id.mPreAddressView);
			TextView mDetailAddressView=(TextView)convertView.findViewById(R.id.mDetailAddressView);
			
			mTitleView.setText(item.getName());
			mPhoneView.setText(item.getTelp());
			
			mPreAddressView.setText(item.getpName()+item.getcName()+item.getdName());
			mDetailAddressView.setText(item.getAddress());
			
			return convertView;
		}	
	}

    //MyTitleTabView的回调接口，用于"寄件地址"、"发件地址"间的切换显示
	@Override
	public void OnTitleClick(int index) {
		
		if(index==0){
			mSendAddressListView.setVisibility(View.VISIBLE);
			mRecipientAddressListView.setVisibility(View.GONE);
			
			type="0";
		}else if(index==1){
			mSendAddressListView.setVisibility(View.GONE);
			mRecipientAddressListView.setVisibility(View.VISIBLE);
			
			type="1";
		}
	}

	//"寄件地址"、"发件地址"listview的下拉刷新功能的回调接口
	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		
		Log.e("zyf","scroller refresh refresh refresh......");
		
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
		mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
		mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mGetAllAddressAsyncTaskDataLoader.execute();
	}

}

