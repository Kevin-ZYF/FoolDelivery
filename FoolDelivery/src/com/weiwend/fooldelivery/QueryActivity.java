package com.weiwend.fooldelivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyDeleteDialog;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyTabView;
import com.weiwend.fooldelivery.customviews.MyTabView.MyOnTabClickLister;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.DeliveryCompanyItem;
import com.weiwend.fooldelivery.items.DeliveryQueryHistoryItem;
import com.weiwend.fooldelivery.items.SendInfoItem;
import com.weiwend.fooldelivery.scan.MipcaCaptureActivity;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.ExpressUtils;
import com.weiwend.fooldelivery.utils.FileUtils;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class QueryActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOnTabClickLister,SensorEventListener{
	
	//查询历史列表删除按钮
	private Button mDeleteBtn;
	
	//页面底部tab控件
	private MyTabView mTabView;
	
	//“查询历史”的列表Listview
	private PullToRefreshListView mPullToRefreshListView;
	
	//“查询历史”Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//"扫一扫"按钮
	private Button mScanBtn;
	
	//单号输入框
	private EditText mCodeShowEt;
	
	//"扫一扫"按钮、单号输入框的Container，主要用于确定mExpressCompanyPopupWindow的显示位置
	private LinearLayout mScanContainer;
	
	//private LinearLayout mCompanySelectContainer;
	//private Button mDeliveryCompanySelectBtn,mQueryBtn,mArrowBtn;
	//private TextView mDeliveryCompanyTv;
	
	//快递公司模糊匹配弹出窗口
	private PopupWindow mExpressCompanyPopupWindow;
	
	//快递公司模糊匹配列表
	private ListView mExpressCompanyListview;
	
	//快递公司模糊匹配Listview的适配器
	private MyExpressCompanyBaseAdapter mExpressCompanyBaseAdapter;
	
	//快递公司模糊匹配窗口的“选择其他快递公司”
	private LinearLayout mSelectExpressCompLayout;
	
	//用于保存用户选择的快递公司信息
	private DeliveryCompanyItem deliveryCompanyItem;
	
	//页面底部tab控件的小图标
	private int mTabViewNormalIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};
	private int mTabViewActiveIcons[]={R.drawable.menu_home,R.drawable.menu_query,R.drawable.menu_send,R.drawable.menu_message};
	
	//获取用户收藏列表的异步加载类
	private AsyncTaskDataLoader mCollectQueryAsyncTaskDataLoader;
	
	//取消指定收藏的异步加载类
	private AsyncTaskDataLoader mCancelCollectAsyncTaskDataLoader;
	
	//保存本地查询历史记录
	private ArrayList<DeliveryQueryHistoryItem> mDeliveryLocalHistoryItems=new ArrayList<DeliveryQueryHistoryItem>();
	
	//保存用户云端的收藏记录
	private ArrayList<DeliveryQueryHistoryItem> mDeliveryCollectHistoryItems=new ArrayList<DeliveryQueryHistoryItem>();
	
	//读取本地查询历史记录的各种状态信息
	private final int DELIVERY_QUERY_LOCAL_HISTROY_GETTING=1;
	private final int DELIVERY_QUERY_LOCAL_HISTROY_GET_SUCCESS=2;
	private final int DELIVERY_QUERY_LOCAL_HISTROY_GET_FAILED=3;
	
	//删除指定的本地查询历史记录的各种状态信息
	private final int DELIVERY_LOCAl_HISTROY_DELETING=4;
	private final int DELIVERY_LOCAl_HISTROY_DELETE_SUCCESS=5;
	private final int DELIVERY_LOCAl_HISTROY_DELETE_FAILED=6;
	
	//获取用户云端收藏列表的各种状态信息
	private final int DELIVERY_QUERY_COLLECT_GETTING=7;
	private final int DELIVERY_QUERY_COLLECT_GET_SUCCESS=8;
	private final int DELIVERY_QUERY_COLLECT_GET_FAILED=9;
	
	//取消用户云端指定收藏的各种状态信息
	private final int DELIVERY_CANCEL_COLLECTING=10;
	private final int DELIVERY_CANCEL_COLLECT_SUCCESS=11;
	private final int DELIVERY_CANCEL_COLLECT_FAILED=12;
	
	//保存用户点击删除的查询历史记录位置，用于删除成功后更新ui
	private int deletePosition;
	
	//根据单号的不同，动态保存模糊匹配成功的快递公司
	private String [] expressCompanys;
	
	//记录单号是否经“扫一扫”功能获取
	private boolean isCodeFromCamera;
	
	//获取所有的查询记录，用于sesn过期后，重新登录成功后自动刷新数据
	private int mode=-1;
	private int MODE_GET_DELIVERY_HISTORY=0;  
	
	//用于控制显示查询历史列表中的“删除”按钮
	private boolean isDeleteMode=false;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            
			case DELIVERY_QUERY_LOCAL_HISTROY_GETTING:    //正在获取本地查询历史记录
				
				mode=MODE_GET_DELIVERY_HISTORY;
							
				Log.e("zyf","delivery query history getting......");
				
				mDeliveryLocalHistoryItems=FileUtils.readDeliveryQueryHistory();     //历史查询记录以文件的形式保存在sdcard中
				
				Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GET_SUCCESS);
							
				break;
			case DELIVERY_QUERY_LOCAL_HISTROY_GET_SUCCESS:        //本地查询历史记录获取成功
				
				mode=-1;
				
				needDetect=true;
				
				Log.e("zyf","delivery query history get success......");
				
				//如果用户已登录，且收藏列表不为空，则对本地查询历史记录进行更新操作
				int index=-1;
                for(int i=0;i<mDeliveryCollectHistoryItems.size();i++){
        			DeliveryQueryHistoryItem item=mDeliveryCollectHistoryItems.get(i);
        			
        			for(int j=0;j<mDeliveryLocalHistoryItems.size();j++){
        				if(item.getSnum().equals(mDeliveryLocalHistoryItems.get(j).getSnum())){
        					index=j;
        					break;
        				}
        			}
        			
        			if(index!=-1){
        				mDeliveryLocalHistoryItems.remove(index);
        				mDeliveryLocalHistoryItems.add(index,item);
        			}else{
        				mDeliveryLocalHistoryItems.add(item);
        			}
        			
        			index=-1;
        		}
                FileUtils.updateDeliveryQueryHistory(mDeliveryLocalHistoryItems);
                
                isDeleteMode=false;
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(QueryActivity.this);
					mPullToRefreshListView.setAdapter(mBaseAdapter);
					mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
													
							Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
							intent.putExtra("snum", mDeliveryLocalHistoryItems.get(position-1).getSnum());
							intent.putExtra("cid", mDeliveryLocalHistoryItems.get(position-1).getCid());
							intent.putExtra("name", mDeliveryLocalHistoryItems.get(position-1).getName());
							intent.putExtra("logo", mDeliveryLocalHistoryItems.get(position-1).getLogo());
							intent.putExtra("rmrk", mDeliveryLocalHistoryItems.get(position-1).getRemark());
							startActivity(intent);
						}
					});
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(QueryActivity.this, getString(R.string.query_history_get_success), MyToast.LENGTH_SHORT).show();
				
				break;
			case DELIVERY_QUERY_LOCAL_HISTROY_GET_FAILED:   //本地查询历史记录获取失败
				
				needDetect=true;
				
				Log.e("zyf","delivery query history get failed......");
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(QueryActivity.this, getString(R.string.query_history_get_failed), MyToast.LENGTH_SHORT).show();
				
				
				break;
			case DELIVERY_LOCAl_HISTROY_DELETING:     //正在删除指定的本地查询历史记录
							
				Log.e("zyf","local history item deleting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_history_item_delete, QueryActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_LOCAl_HISTROY_DELETE_SUCCESS:      //指定的本地查询历史记录删除成功，更新ui
				
				Log.e("zyf","local history item delete success......");
				
				mDeliveryLocalHistoryItems.remove(deletePosition);
				
				FileUtils.updateDeliveryQueryHistory(mDeliveryLocalHistoryItems);
				
				//如果删除了全部，则下次刷新出来的时候隐藏“删除”按钮
				if(mDeliveryLocalHistoryItems.size()==0){
					isDeleteMode=false;
				}
				
				if(mBaseAdapter!=null){
					mBaseAdapter.notifyDataSetChanged();
				}
				
				MyToast.makeText(QueryActivity.this, getString(R.string.query_history_item_delete_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			case DELIVERY_LOCAl_HISTROY_DELETE_FAILED:     //指定的本地查询历史记录删除失败
				
				Log.e("zyf","local history item delete failed......");
				
				MyToast.makeText(QueryActivity.this, getString(R.string.query_history_item_delete_failed), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			case DELIVERY_QUERY_COLLECT_GETTING:      //正在获取用户云端的收藏记录
				
                Log.e("zyf","collect gettting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_collect_list, QueryActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_QUERY_COLLECT_GET_SUCCESS:      //用户云端的收藏记录获取成功，下一步获取本地的查询历史记录
				
                Log.e("zyf","collect get success......");
				
				MyToast.makeText(QueryActivity.this, getString(R.string.delivery_collect_get_success), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
				
				break;
			case DELIVERY_QUERY_COLLECT_GET_FAILED:      //用户云端的收藏记录获取失败，下一步获取本地的查询历史记录
				
                Log.e("zyf","collect get failed......");
				
				MyToast.makeText(QueryActivity.this, getString(R.string.delivery_collect_get_failed), MyToast.LENGTH_SHORT).show();
				mProgressDialogUtils.dismissDialog();
				
				Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
				
				break;
           case DELIVERY_CANCEL_COLLECTING:      //正在取消指定的云端收藏记录
				
				Log.e("zyf","delivery cancel collecting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_cancel_collect, QueryActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_CANCEL_COLLECT_SUCCESS:      //指定的云端收藏记录取消成功，下一步将该记录从本地查询记录中删除
				
				Log.e("zyf","delivery cancel collect success......");
				
                mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryActivity.this, getString(R.string.delivery_cancel_collect_success), MyToast.LENGTH_SHORT).show();
				
				Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETING);
				
				Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETE_SUCCESS);

				break;
			case DELIVERY_CANCEL_COLLECT_FAILED:      //指定的云端收藏记录取消失败
				
				Log.e("zyf","delivery cancel collect success......");

				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryActivity.this, getString(R.string.delivery_cancel_collect_failed), MyToast.LENGTH_SHORT).show();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_query);
		
		mDeleteBtn=(Button)findViewById(R.id.mDeleteBtn);
		mDeleteBtn.setOnClickListener(this);
		
		mTabView=(MyTabView)findViewById(R.id.mTabView);
		mTabView.setDatas(mTabViewNormalIcons, mTabViewActiveIcons);
		mTabView.setOnTabClickListener(this);
		mTabView.setCurItem(1);
		
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				Log.e("zyf","onRefresh onRefresh onRefresh...");
				
				if(MyApplicaition.sesn.length()!=0){   //用户已登录，首先获取用户云端的收藏
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
					mCollectQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_COLLECT_LIST, url, null, null);
					mCollectQueryAsyncTaskDataLoader.setOnDataLoaderListener(QueryActivity.this);
					mCollectQueryAsyncTaskDataLoader.execute();
					
				}else{      //用户未登录，仅获取本地的历史查询记录
					
					Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
				}
				
			}
		});
		
		
		mScanBtn=(Button)findViewById(R.id.mScanBtn);
		mScanBtn.setOnClickListener(this);
		
		/*mDeliveryCompanySelectBtn=(Button)findViewById(R.id.mDeliveryCompanySelectBtn);
		mQueryBtn=(Button)findViewById(R.id.mQueryBtn);
		mArrowBtn=(Button)findViewById(R.id.mArrowBtn);
		
		mQueryBtn.setOnClickListener(this);
		mDeliveryCompanySelectBtn.setOnClickListener(this);
		mArrowBtn.setOnClickListener(this);*/
		
		mCodeShowEt=(EditText)findViewById(R.id.mCodeShowEt);
		//mDeliveryCompanyTv=(TextView)findViewById(R.id.mDeliveryCompanyTv);
		
		/*mCompanySelectContainer=(LinearLayout)findViewById(R.id.mCompanySelectContainer);
		mCompanySelectContainer.setOnClickListener(this);*/
		
		mScanContainer=(LinearLayout)findViewById(R.id.mScanContainer);
		
		/*mSelectExpressCompLayout=(LinearLayout)findViewById(R.id.mSelectExpressCompLayout);
		mSelectExpressCompLayout.setOnClickListener(this);
		
		mExpressCompanyListview=(ListView)findViewById(R.id.mExpressCompanyListview);
		mExpressCompanyBaseAdapter=new MyExpressCompanyBaseAdapter(this);
		mExpressCompanyListview.setAdapter(mExpressCompanyBaseAdapter);
		mExpressCompanyListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
				intent.putExtra("cid", ExpressUtils.getExpressCid(expressCompanys[position]));
				intent.putExtra("snum", mCodeShowEt.getText().toString());
				//intent.putExtra("logo", deliveryCompanyItem.getLogo());
				
				Log.e("zyf","MyApplicaition.deliveryCompanyItems.size(): "+MyApplicaition.deliveryCompanyItems.size());
				for(int i=0;i<MyApplicaition.deliveryCompanyItems.size();i++){
					
					if(MyApplicaition.deliveryCompanyItems.get(i).getName().equals(expressCompanys[position])){
						intent.putExtra("logo", MyApplicaition.deliveryCompanyItems.get(i).getLogo());
					}
				}
				
				intent.putExtra("name", expressCompanys[position]);
				startActivity(intent);
			}
		});*/
		
		View contentView=getLayoutInflater().inflate(R.layout.popup_window_express_company, null);
		mExpressCompanyPopupWindow=new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
		
		mSelectExpressCompLayout=(LinearLayout)contentView.findViewById(R.id.mSelectExpressCompLayout);
		mSelectExpressCompLayout.setOnClickListener(this);
		
		mExpressCompanyListview=(ListView)contentView.findViewById(R.id.mExpressCompanyListview);
		mExpressCompanyBaseAdapter=new MyExpressCompanyBaseAdapter(this);
		mExpressCompanyListview.setAdapter(mExpressCompanyBaseAdapter);
		
		mCodeShowEt.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean focus) {
				
				if(focus){      //当单号输入框获取焦点时，判断是否需要显示快递公司列表
					
					if(expressCompanys==null||expressCompanys.length==0){
						
						if(mExpressCompanyPopupWindow.isShowing()){
							
							mExpressCompanyPopupWindow.dismiss();
						}
						
					}else{	
						
						mExpressCompanyPopupWindow.showAsDropDown(mScanContainer, 0, 0);
					}
					
				}else{      //当单号输入框失去焦点时，快递公司列表消失
					
					mExpressCompanyPopupWindow.dismiss();
				}
			}
		});
		
		mCodeShowEt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
				String number=mCodeShowEt.getText().toString();
				expressCompanys=ExpressUtils.getExpressNoForRule(number);    //根据单号的格式，获取模糊匹配成功的快递公司列表
				
				if(expressCompanys==null||expressCompanys.length==0){
				
					mExpressCompanyPopupWindow.dismiss();
					
				}else{	
					
					mExpressCompanyPopupWindow.showAsDropDown(mScanContainer, 0, 0);
				}
				mExpressCompanyBaseAdapter.notifyDataSetChanged();
				
				if(isCodeFromCamera){    //此时说明用户的单号已完整
					
					if(expressCompanys!=null&&expressCompanys.length==1){    //此时说明只要一家快递公司匹配成功，则直接选择该家快递公司进行查询
						
						clearEdittextFocus();   //使快递公司列表消失，用户重新返回到该界面时，点击单号输入框，则快递公司列表重新显示
						
						Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
						intent.putExtra("cid", ExpressUtils.getExpressCid(expressCompanys[0]));
						intent.putExtra("snum", mCodeShowEt.getText().toString());
						//intent.putExtra("logo", deliveryCompanyItem.getLogo());
						
						/*Log.e("zyf","MyApplicaition.deliveryCompanyItems.size(): "+MyApplicaition.deliveryCompanyItems.size());
						for(int i=0;i<MyApplicaition.deliveryCompanyItems.size();i++){
							
							if(MyApplicaition.deliveryCompanyItems.get(i).getName().equals(expressCompanys[0])){
								intent.putExtra("logo", MyApplicaition.deliveryCompanyItems.get(i).getLogo());
							}
						}*/
						
						intent.putExtra("name", expressCompanys[0]);
						startActivity(intent);
						
					}
					
					isCodeFromCamera=false;
				}
			}
		});
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		if(MyApplicaition.sesn.length()!=0){    //用户已登录，首先获取云端的收藏列表
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
			mCollectQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_COLLECT_LIST, url, null, null);
			mCollectQueryAsyncTaskDataLoader.setOnDataLoaderListener(QueryActivity.this);
			mCollectQueryAsyncTaskDataLoader.execute();
			
		}else{      //用户未登录，仅获取本地的历史查询记录
			
			Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
		}
		
	}
	
	//注册重力感应器的监听器
	@Override  
	protected void onResume(){  
	  super.onResume();

	  mSensorManager.registerListener(this,  
	  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
	  SensorManager.SENSOR_DELAY_NORMAL);
	  
	  //刷新本地查询历史列表
	  mDeliveryLocalHistoryItems=FileUtils.readDeliveryQueryHistory();
	  if(mBaseAdapter!=null){
		  mBaseAdapter.notifyDataSetChanged();
	  }
	}  
	
	//注销重力感应器的监听器
	@Override  
	protected void onStop(){  
	  mSensorManager.unregisterListener(this);  
	  super.onStop();  
	}  
	  
	@Override  
	protected void onPause(){  
	  //mSensorManager.unregisterListener(this);  
	  super.onPause();  
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			finish();
			break;
		case R.id.mDeleteBtn:       //控制显示查询历史列表中的“删除”按钮
			
			if(isDeleteMode){
				isDeleteMode=false;
			}else{
				isDeleteMode=true;
			}
			
			if(mBaseAdapter!=null){
				mBaseAdapter.notifyDataSetChanged();
			}
			
			break;
		case R.id.userCenterBtn:   //标题栏右侧的“用户中心”快捷入口
			
			Intent intent2=new Intent(this, UserCenterActivity.class);
			startActivity(intent2);
			
			finish();
			break;
		case R.id.mScanBtn:      //“扫一扫”
			
			Intent intent3 = new Intent(this, MipcaCaptureActivity.class);
			startActivityForResult(intent3, ActivityResultCode.CODE_SCAN);
			
			break;
		case R.id.mSelectExpressCompLayout:    //“选择其他快递公司”
			
			/*mScanBtn.setFocusable(true);
			mScanBtn.setFocusableInTouchMode(true);
			mScanBtn.requestFocus();  // 初始不让EditText得焦点
			mScanBtn.requestFocusFromTouch();*/
			
			clearEdittextFocus();
			 
			Intent intent4=new Intent(QueryActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent4, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
			 
			break;
			
		/*case R.id.mDeliveryCompanySelectBtn:
        	Intent intent4=new Intent(QueryActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent4, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
			break;
        case R.id.mArrowBtn:
        	Intent intent5=new Intent(QueryActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent5, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
        	break;
        case R.id.mCompanySelectContainer:
        	
        	Intent intent6=new Intent(QueryActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent6, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
			
        	break;*/
        /*case R.id.mQueryBtn:
        	
        	if(mCodeShowEt.getText().toString().length()==0){ 
        		
        		MyToast.makeText(QueryActivity.this, getString(R.string.prompt_delivery_query_input_snum), MyToast.LENGTH_SHORT).show();
        		return;
        	}
        
	        if(mDeliveryCompanyTv.getText().toString().length()==0){ 
	    		
	    		MyToast.makeText(QueryActivity.this, getString(R.string.prompt_delivery_query_select_company), MyToast.LENGTH_SHORT).show();
	    		return;
	    	}
        	
        	Intent intent7=new Intent(QueryActivity.this,QueryResultActivity.class);
			intent7.putExtra("cid", deliveryCompanyItem.getId());
			intent7.putExtra("snum", mCodeShowEt.getText().toString());
			intent7.putExtra("logo", deliveryCompanyItem.getLogo());
			intent7.putExtra("name", deliveryCompanyItem.getName());
			startActivity(intent7);
			break;*/

		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_SCAN){    //“扫一扫”返回结果
			if(data!=null){
				
				isCodeFromCamera=true;
				
				Bundle bundle = data.getExtras();
				Log.e("zyf","scan code: "+bundle.getString("result"));
				
				mCodeShowEt.setText(bundle.getString("result"));
				
				mCodeShowEt.setSelection(mCodeShowEt.getText().toString().length());
			
			}
		}else if(requestCode==ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR){
			if(data!=null){
				deliveryCompanyItem = (DeliveryCompanyItem) data.getSerializableExtra("DeliveryCompanyItem");
				
				Log.e("zyf",deliveryCompanyItem.getName());
				
				//mDeliveryCompanyTv.setText(deliveryCompanyItem.getName());
				
				clearEdittextFocus();
				
				Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
				intent.putExtra("cid", deliveryCompanyItem.getId());
				intent.putExtra("snum", mCodeShowEt.getText().toString());
				intent.putExtra("logo", deliveryCompanyItem.getLogo());
				intent.putExtra("name", deliveryCompanyItem.getName());
				startActivity(intent);
			}
		}
		/*else if(requestCode==ActivityResultCode.CODE_LOGIN){
			
			if(data!=null){
				
				if(data.getBooleanExtra("loginStatus", false)){
					Log.e("zyf","login status : true");
					
					if(mode==MODE_GET_DELIVERY_HISTORY){
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_QUERY_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
						mHistoryQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_QUERY_HISTORY, url, null, null);
						mHistoryQueryAsyncTaskDataLoader.setOnDataLoaderListener(this);
						mHistoryQueryAsyncTaskDataLoader.execute();
					}
				}
			}
		}*/
	}

	//初始化当前页面的标题
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		leftBtn.setVisibility(View.INVISIBLE);
		
		Button userCenterBtn=(Button)findViewById(R.id.userCenterBtn);
		userCenterBtn.setOnClickListener(this);
		userCenterBtn.setVisibility(View.VISIBLE);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_query));
	}
	
	//本地历史查询记录列表Listview的适配器
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
			return mDeliveryLocalHistoryItems.size();
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
			
			final DeliveryQueryHistoryItem item=mDeliveryLocalHistoryItems.get(position);
			
			final int pos=position;
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_delivery_query_history, null);
			}
			
			ImageView mCollectView = (ImageView) convertView.findViewById(R.id.mCollectView);
			TextView mDeliveryInfoTv = (TextView) convertView.findViewById(R.id.mDeliveryInfoTv);
			TextView mStatusTv = (TextView) convertView.findViewById(R.id.mStatusTv);
			TextView mOrderTimeTv = (TextView) convertView.findViewById(R.id.mOrderTimeTv);
			TextView mRemarkTv = (TextView) convertView.findViewById(R.id.mRemarkTv);
			MyLoaderImageView mLoaderImageView= (MyLoaderImageView) convertView.findViewById(R.id.mLoaderImageView);
			
			Button mDeleteBtn = (Button) convertView.findViewById(R.id.mDeleteBtn);
			Button mArrowBtn = (Button) convertView.findViewById(R.id.mArrowBtn);
			
			if(isDeleteMode){
				mDeleteBtn.setVisibility(View.VISIBLE);
				mArrowBtn.setVisibility(View.GONE);
				mDeleteBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
						MyDeleteDialog mDialog=new MyDeleteDialog(QueryActivity.this, R.style.MyDialog);
						mDialog.setMySubmmitListener(new MyDeleteDialog.MySubmmitListener() {
							
							@Override
							public void delete() {
								
								//用户已登录，且在云端收藏了该记录，则首先取消云端的收藏，取消收藏成功后，再删除该本地历史查询记录
								if(MyApplicaition.sesn.length()>0&&item.isOffical()){  
									
									String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_CANCEL_COLLECT_URL
											+"?sesn="+MyApplicaition.sesn
											+"&id="+item.getId();
									mCancelCollectAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_CANCEL_COLLECT, url, null, null);
									mCancelCollectAsyncTaskDataLoader.setOnDataLoaderListener(QueryActivity.this);
									mCancelCollectAsyncTaskDataLoader.execute();
									
									deletePosition=pos;    //为删除本地记录做准备
									
								}else{  //用户未登录，只是单纯删除本地历史查询记录
									
									Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETING);
									
									deletePosition=pos;
									
									Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETE_SUCCESS);
								}
							}
						});
						mDialog.show();
						
					}
				});
			}else{
				mDeleteBtn.setVisibility(View.GONE);
				mArrowBtn.setVisibility(View.VISIBLE);
				mArrowBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
						Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
						intent.putExtra("id", item.getId());
						startActivity(intent);
					}
				});
			}
			
			mDeliveryInfoTv.setText(item.getName()+"  "+item.getSnum());
			mOrderTimeTv.setText(item.getStm());
			mLoaderImageView.setURL(UrlConfigs.SERVER_URL+item.getLogo());
			
			if(item.getStat()!=null){
				mStatusTv.setText("状态: "+item.getStat());
			}else{
				mStatusTv.setText("状态: "+"未知");
			}
			
			if(item.getRemark()!=null){
				mRemarkTv.setText("备注: "+item.getRemark());
			}else{
				mRemarkTv.setText("备注: ");
			}
			
			if(item.isOffical()&&MyApplicaition.sesn.length()>0){   //收藏了并且用户已经登录
				mCollectView.setBackgroundResource(R.drawable.collect);
			}else{
				mCollectView.setBackgroundResource(R.drawable.collect_no);
			}
			
			return convertView;
			
		}
	
	}
    
    //模糊匹配成功后的快递公司列表Listview的适配器
    class MyExpressCompanyBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyExpressCompanyBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			
			if(expressCompanys!=null){
				
				return expressCompanys.length;
			}else{
				return 0;
			}
			
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
			
			final int pos=position;
			
			if(convertView==null){
				convertView=mInflater.inflate(R.layout.item_express_company, null);
			}
			
			LinearLayout mTotalContainer=(LinearLayout)convertView.findViewById(R.id.mTotalContainer);
			mTotalContainer.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					clearEdittextFocus();

					Intent intent=new Intent(QueryActivity.this,QueryResultActivity.class);
					intent.putExtra("cid", ExpressUtils.getExpressCid(expressCompanys[pos]));
					intent.putExtra("snum", mCodeShowEt.getText().toString());
					//intent.putExtra("logo", deliveryCompanyItem.getLogo());
					
					intent.putExtra("name", expressCompanys[pos]);
					startActivity(intent);
				}
			});
			
			TextView mExpressCompanyTv = (TextView) convertView.findViewById(R.id.mExpressCompanyTv);
			
			mExpressCompanyTv.setText(expressCompanys[position]+"      "+mCodeShowEt.getText().toString());
			
			if(pos==0){
				mExpressCompanyTv.setTextColor(Color.parseColor("#73bad6"));
			}else{
				mExpressCompanyTv.setTextColor(Color.parseColor("#000000"));
			}
			
			return convertView;
			
		}
	
	}

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_DELIVERY_QUERY_HISTORY){    //开始获取本地查询历史记录
			
			Log.e("zyf","delivery query history......");
			
			Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
			
		}
		/*else if(flag==Flag.FLAG_DELIVERY_HISTORY_DELETE){   //开始删除指定的本地查询历史记录
			
			Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETING);
			
		}*/
		else if(flag==Flag.FLAG_DELIVERY_COLLECT_LIST){      //开始获取用户云端的收藏列表
			
			Utils.sendMessage(mHandler, DELIVERY_QUERY_COLLECT_GETTING);
			
		}else if(flag==Flag.FLAG_DELIVERY_CANCEL_COLLECT){      //开始取消指定的云端收藏
			
			Utils.sendMessage(mHandler, DELIVERY_CANCEL_COLLECTING);
			
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_DELIVERY_COLLECT_LIST){      //获取云端收藏列表返回结果
			
			Log.e("zyf","query collect: "+result);
			
			try {
				
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //云端收藏列表获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						mDeliveryCollectHistoryItems.clear();
						Utils.sendMessage(mHandler, DELIVERY_QUERY_COLLECT_GET_SUCCESS);
						
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
				    	deliveryQueryHistoryItem.setLogo(jsonObject.getString("logo"));
				    	deliveryQueryHistoryItem.setStat(jsonObject.getString("stat"));
				    	deliveryQueryHistoryItem.setName(jsonObject.getString("name"));
				    	deliveryQueryHistoryItem.setStm(jsonObject.getString("stm"));
				    	deliveryQueryHistoryItem.setRemark(jsonObject.getString("rmrk"));
				    	deliveryQueryHistoryItem.setOffical(true);   //记录是否已经收藏
				    	
				    	mDeliveryCollectHistoryItems.add(deliveryQueryHistoryItem);
				    }
				    
				    Utils.sendMessage(mHandler, DELIVERY_QUERY_COLLECT_GET_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_QUERY_COLLECT_GET_FAILED);      //云端收藏列表获取失败
			
		}
		/*else if(flag==Flag.FLAG_DELIVERY_HISTORY_DELETE){  
			
			Log.e("zyf","history item delete result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){
					
				    Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETE_SUCCESS);
				    
				    return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, DELIVERY_LOCAl_HISTROY_DELETE_FAILED);
			
		}*/
		else if(flag==Flag.FLAG_DELIVERY_CANCEL_COLLECT){  //取消收藏返回结果
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //取消收藏成功
				    
				    Utils.sendMessage(mHandler, DELIVERY_CANCEL_COLLECT_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_CANCEL_COLLECT_FAILED);      //取消收藏失败
		}
	}
	
	//监听返回键，如果快递公司列表在显示，则将其移除
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			
			if(!removeExpressCompanyPopupwindow()){
				
				finish();
			}
		}
		return false;
	}
	
	//移除快递公司列表
	private boolean removeExpressCompanyPopupwindow(){
		
		if(mExpressCompanyPopupWindow!=null&&mExpressCompanyPopupWindow.isShowing()){
			
			mExpressCompanyPopupWindow.dismiss();
			
			clearEdittextFocus();
			
			return true;
		}
		
		return false;
	}
	
	//使EditText失去焦点
	private void clearEdittextFocus(){
		
		mScanBtn.setFocusable(true);
		mScanBtn.setFocusableInTouchMode(true);
		mScanBtn.requestFocus();
		mScanBtn.requestFocusFromTouch();
	}

	//页面底部MyTabView控件点击回调函数
	@Override
	public void OnTabClick(int choice) {
		
		switch (choice) {
		case 0:   //回首页
			finish();
			break;
		case 1:   //回查件页面，即当前界面
			break;
		case 2:   //回寄件界面
			
			Intent intent=new Intent(this,SenderActivity.class);
			startActivity(intent);
			
			finish();
			
			break;
		case 3:   //回消息界面
			
			Intent intent2=new Intent(this,MessageCenterActivity.class);
			startActivity(intent2);
			
			finish();
			break;
		default:
			break;
		}
	}
	
	//标记前一次“摇一摇”是否已经处理完毕，若未处理完毕，则不响应本次的“摇一摇”
	private boolean needDetect = true;
	
	//用于“摇一摇”功能的重力感应器
	private SensorManager mSensorManager; 
	
	//用于“摇一摇”功能的震动器
	private Vibrator vibrator;

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(!needDetect){    //上次“摇一摇”尚未处理完毕，所以不响应本次“摇一摇”
			return;
		}
		
		int sensorType = event.sensor.getType();
		
		float[] values = event.values; 
		
		if(sensorType == Sensor.TYPE_ACCELEROMETER){    //values[0]:X轴加速度，values[1]：Y轴加速度，values[2]：Z轴加速度  
			
			if((Math.abs(values[0])>16||Math.abs(values[1])>16||Math.abs(values[2])>16)){
				
				Log.e("zyf","vibrate vibrate vibrate...");
				
				vibrator.vibrate(500);     //手机震动
				
				if(MyApplicaition.sesn.length()!=0){   //用户已登录,首先获取云端的收藏列表
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_HISTORY_URL+"?sesn="+MyApplicaition.sesn;
					mCollectQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_COLLECT_LIST, url, null, null);
					mCollectQueryAsyncTaskDataLoader.setOnDataLoaderListener(QueryActivity.this);
					mCollectQueryAsyncTaskDataLoader.execute();
					
				}else{     //用户未登录,只刷新本地的查询记录
					
					Utils.sendMessage(mHandler, DELIVERY_QUERY_LOCAL_HISTROY_GETTING);
					
				}
				
				needDetect=false;
			}
		}  
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}
}
