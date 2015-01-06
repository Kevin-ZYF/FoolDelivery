package com.weiwend.fooldelivery.fragments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.weiwend.fooldelivery.AddressManagerActivity;
import com.weiwend.fooldelivery.AddressModifyActivity;
import com.weiwend.fooldelivery.MyApplicaition;
import com.weiwend.fooldelivery.MyDeletedListViewBaseAdapter;
import com.weiwend.fooldelivery.ParityActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.SendSuccessActivity;
import com.weiwend.fooldelivery.UserLoginActivity;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyReboundScrollView;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.customviews.TimeSelectorView;
import com.weiwend.fooldelivery.customviews.TimeSelectorView.MyOnTimeSelectedClickLister;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.ParityCompanyItem;
import com.weiwend.fooldelivery.items.SendInfoItem;
import com.weiwend.fooldelivery.scan.FinishListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;
import com.weiwend.invalid.SenderModifyOldActivity;

public class SenderFragment extends Fragment implements OnClickListener,MyOnTimeSelectedClickLister,onDataLoaderListener{
	
	//fragment的上下文
	private Context mContext;
	
	//初始化界面时,“点击添加联系人”功能
	private RelativeLayout mAddSenderAddressContainer,mAddRecipientAddressContainer;
	
	//“点击添加联系人” 完成后，寄件人信息、收件人信息的显示Container
	private RelativeLayout mSenderInfoContainer,mRecipientAddressInfoContainer;
	
	//“去比价”Container、“预约取件”Container
	private RelativeLayout mParityContainer,mReserveTimeContainer;
	
	//寄件人的姓名、联系方式
	private TextView mSenderNameTv,mSenderPhoneTv;
	
	//发件人的姓名、联系方式
	private TextView mRecipientNameTv,mRecipientPhoneTv;
	
	//寄件人、发件人的全部地址信息 :省+市+区+街道详细地址
	private TextView mSenderTotalAddressTv,mRecipientTotalAddressTv;
	
	//预约取件时间、通过“比价”选择的快递公司
	private TextView mSelectedTimeShowView,mParityComapnyShowView;
	
	//提交按钮，两种情况:订单的新增，订单的修改
	private Button mSummitBtn;
	
	//重量"-"、“+”按钮
	private Button mMinusBtn,mPlusBtn;
	
	//物品名称、备注、重量
	private EditText mSenderNameEt,mRemarkEt,mWeightEt;
	
	//点击“预约取件”弹出的是日期时间选择窗口
	private TimeSelectorView mTimeSelectorView;
	
	//用户选择“马上寄出”时的提示语：“(2小时内，我们将安排取件)”
	private TextView mSenderTimePromptTv;
	
	//主要用于优化用户的体验，可用ScrollView代替
	private MyReboundScrollView mScrollView;
	
	//寄件人信息、收件人信息
	private AddressItem mSenderAddressItem,mRecipientAddressItem;
	
	//比价后选择的网点信息
	private ParityCompanyItem mParityCompanyItem;
	
	//“添加寄件”的异步加载类
	private AsyncTaskDataLoader mSendAddAsyncTaskDataLoader;
	
	//“编辑寄件”的异步加载类
	private AsyncTaskDataLoader mSendModifyAsyncTaskDataLoader;
	
	//获取用户常用地址的异步加载类
	private AsyncTaskDataLoader mGetAllAddressAsyncTaskDataLoader;
	
	//标识是“新增寄件”或者“编辑寄件”
	private boolean isDeliveryInfoModify;
	
	//订单的所有信息，仅在“编辑寄件”时不为空，主要用于初始化界面
	private SendInfoItem mDeliveryInfoItem;
	
	//预约取件的日期、具体时间
	private String reserveDate="",reserveTime="";
	
	//调用“新增寄件”接口后的各种状态信息
	private final int SEND_ADDING=0;
	private final int SEND_ADD_SUCCESS=1;
	private final int SEND_ADD_FAILED=2;
	
	//调用“编辑寄件”接口后的各种状态信息
	private final int SEND_MODIFYING=3;
	private final int SEND_MODIFY_SUCCESS=4;
	private final int SEND_MODIFY_FAILED=5;
	
	//调用“常用地址”接口后的各种状态信息
	private final int ALL_ADDRESS_GETTING=6;
	private final int ALL_ADDRESS_GET_SUCCESS=7;
	private final int ALL_ADDRESS_GET_FAILED=8;
	
	//主要为了处理sesn过期后,用户重新登录成功，页面重新获取用户的常用地址来更新ui
	private int MODE_NORMAL=-1;
	private int MODE_GET_ADDRESS=0;
	private int mode;
	
	//该字段预先保留，主要处理预约取件时，用户选择日期后，界面上显示具体的日期值还是显示“今天、明天、后天”？
	private String dateShow;
	
	//用于标识预约取件时间是否是“马上寄出”
	private boolean isSendImmediately;
	
	//耗时网络操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case SEND_ADDING:          //“新增寄件”的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","send adding......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_add, mContext);
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_ADD_SUCCESS:     //“新增寄件”成功，跳转到“寄件成功”页面
				
				Log.e("zyf","send add success......");
				
				MyToast.makeText(mContext, getString(R.string.send_add_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent intent=new Intent(getActivity(),SendSuccessActivity.class);
				startActivity(intent);
				
				getActivity().finish();
            	
				break;
			case SEND_ADD_FAILED:    //“新增寄件”失败
				
				Log.e("zyf","send add failed......");
				
				MyToast.makeText(mContext, getString(R.string.send_add_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
           case SEND_MODIFYING:    //“编辑寄件”的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","send modifying......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_modify, getActivity());
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_MODIFY_SUCCESS:   //“编辑寄件”成功，返回“寄件历史”界面，“寄件历史”重新刷新界面
				
				Log.e("zyf","send modify success......");
				
				MyToast.makeText(getActivity(), getString(R.string.send_modify_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent modifiedSuccessIntent=new Intent();
				modifiedSuccessIntent.putExtra("isModified", true);
				getActivity().setResult(ActivityResultCode.CODE_SEND_INFO_MODIFY,modifiedSuccessIntent);
				getActivity().finish();
            	
				break;
			case SEND_MODIFY_FAILED:   //“编辑寄件”失败
				
				Log.e("zyf","send modify failed......");
				
				MyToast.makeText(getActivity(), getString(R.string.send_modify_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
           case ALL_ADDRESS_GETTING:    //“常用地址”的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","all address getting......");
				
				mode=MODE_GET_ADDRESS;
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_get_address, getActivity());
				mProgressDialogUtils.showDialog();
				
				break;
			case ALL_ADDRESS_GET_SUCCESS:  //“常用地址”获取成功，主要用于“新增寄件”时，将“寄件人信息”、“收件人信息”设置为用户的默认寄件地址、默认收件地址
				
				Log.e("zyf","all address get success......");
				
				mode=MODE_NORMAL;
				
				if(mSenderAddressItem!=null){   //默认寄件地址
					
					mSenderNameTv.setText(mSenderAddressItem.getName());
					mSenderPhoneTv.setText(mSenderAddressItem.getTelp());
					
					mSenderTotalAddressTv.setText(mSenderAddressItem.getpName()+mSenderAddressItem.getcName()+mSenderAddressItem.getdName()+mSenderAddressItem.getAddress());
					
					mAddSenderAddressContainer.setVisibility(View.GONE);
					mSenderInfoContainer.setVisibility(View.VISIBLE);
				}
				
				if(mRecipientAddressItem!=null){  //默认收件地址
					
					mRecipientNameTv.setText(mRecipientAddressItem.getName());
					mRecipientPhoneTv.setText(mRecipientAddressItem.getTelp());
					
					mRecipientTotalAddressTv.setText(mRecipientAddressItem.getpName()+mRecipientAddressItem.getcName()+mRecipientAddressItem.getdName()+mRecipientAddressItem.getAddress());
					
					mAddRecipientAddressContainer.setVisibility(View.GONE);
					mRecipientAddressInfoContainer.setVisibility(View.VISIBLE);
				}
				
                MyToast.makeText(getActivity(), getString(R.string.address_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case ALL_ADDRESS_GET_FAILED:   //“常用地址”获取失败
				
				Log.e("zyf","all address get failed......");
				
                MyToast.makeText(getActivity(), getString(R.string.address_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View contentView=inflater.inflate(R.layout.fragment_sender, container, false);
		
		mContext=getActivity();
		
		if(getArguments()!=null){
			
			isDeliveryInfoModify=getArguments().getBoolean("isDeliveryInfoModify");   //判断是“新增寄件”还是“编辑寄件”
			
			if(isDeliveryInfoModify){   //“编辑寄件”
				
				Log.e("zyf","send modify...");
				
				mDeliveryInfoItem=(SendInfoItem) getArguments().getSerializable("DeliveryInfoItem");   //获取需要编辑的订单的所有信息，用于初始化界面
			}
		}else{
			Log.e("zyf","send add...");
		}
		
		initViews(contentView);
		
		if(!isDeliveryInfoModify){   //“新增寄件”时，获取用户的默认寄件地址、默认发件地址，用于初始化界面
			
			if(MyApplicaition.sesn==null||MyApplicaition.sesn.length()==0){   //用户未登陆，跳转到登陆界面
				
				Intent intent=new Intent(getActivity(), UserLoginActivity.class);
				startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				
			}else{
				
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
				Log.e("zyf","address url: "+url);
				mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
				mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
				mGetAllAddressAsyncTaskDataLoader.execute();
			}
		}
		
		return contentView;
	}
	
	//初始化ui
	private void initViews(View contentView){
		
		/*RelativeLayout mTitleContainer=(RelativeLayout)contentView.findViewById(R.id.mTitleContainer);
		mTitleContainer.setVisibility(View.GONE);*/
		
		mSenderInfoContainer=(RelativeLayout)contentView.findViewById(R.id.mSenderInfoContainer);
		mRecipientAddressInfoContainer=(RelativeLayout)contentView.findViewById(R.id.mRecipientAddressInfoContainer);
		
		mReserveTimeContainer=(RelativeLayout)contentView.findViewById(R.id.mReserveTimeContainer);
		mAddSenderAddressContainer=(RelativeLayout)contentView.findViewById(R.id.mAddSenderAddressContainer);
		mAddRecipientAddressContainer=(RelativeLayout)contentView.findViewById(R.id.mAddRecipientAddressContainer);
		mParityContainer=(RelativeLayout)contentView.findViewById(R.id.mParityContainer);
		
		mSenderInfoContainer.setOnClickListener(this);
		mRecipientAddressInfoContainer.setOnClickListener(this);
		mReserveTimeContainer.setOnClickListener(this);
		mAddSenderAddressContainer.setOnClickListener(this);
		mAddRecipientAddressContainer.setOnClickListener(this);
		mParityContainer.setOnClickListener(this);
		
		mSenderNameTv=(TextView)contentView.findViewById(R.id.mSenderNameTv);
		mSenderPhoneTv=(TextView)contentView.findViewById(R.id.mSenderPhoneTv);
		mParityComapnyShowView=(TextView)contentView.findViewById(R.id.mParityComapnyShowView);
		
		mRecipientNameTv=(TextView)contentView.findViewById(R.id.mRecipientNameTv);
		mRecipientPhoneTv=(TextView)contentView.findViewById(R.id.mRecipientPhoneTv);
		mSenderTotalAddressTv=(TextView)contentView.findViewById(R.id.mSenderTotalAddressTv);
		mRecipientTotalAddressTv=(TextView)contentView.findViewById(R.id.mRecipientTotalAddressTv);
		mSenderTimePromptTv=(TextView)contentView.findViewById(R.id.mSenderTimePromptTv);
		
		mRemarkEt=(EditText)contentView.findViewById(R.id.mRemarkEt);
		mWeightEt=(EditText)contentView.findViewById(R.id.mWeightEt);
		mSenderNameEt=(EditText)contentView.findViewById(R.id.mSenderNameEt);
		
		mWeightEt.setSelection(mWeightEt.getText().toString().length());
		
		mWeightEt.addTextChangedListener(new TextWatcher() {   //主要实现weight的“+”、“-”功能，且weight值至多保留一位小数
			 
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            	
            	String number=s.toString();
            	int len=number.length();
                int dotIndex=number.indexOf(".");
                if(dotIndex==-1){   //没有小数点时
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
                
            	updateSummitBtnStatus();
            }
 
        });
		
		mRemarkEt.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
		
		mSummitBtn=(Button)contentView.findViewById(R.id.mSummitBtn);
		mMinusBtn=(Button)contentView.findViewById(R.id.mMinusBtn);
		mPlusBtn=(Button)contentView.findViewById(R.id.mPlusBtn);
		
		mSummitBtn.setOnClickListener(this);
		mMinusBtn.setOnClickListener(this);
		mPlusBtn.setOnClickListener(this);
		
        mScrollView=(MyReboundScrollView)contentView.findViewById(R.id.mScrollView);
		
		mTimeSelectorView=(TimeSelectorView)contentView.findViewById(R.id.mTimeSelectorView);
		mTimeSelectorView.setScrollView(mScrollView);
		mTimeSelectorView.setOnTimeSelectedListener(this);
		
		mSelectedTimeShowView=(TextView)contentView.findViewById(R.id.mSelectedTimeShowView);
		
		if(isDeliveryInfoModify){  //“编辑发件”时，初始化界面信息
			
			mSenderAddressItem=mDeliveryInfoItem.getSenderAddressItem();       //获取订单的寄件人信息
			mRecipientAddressItem=mDeliveryInfoItem.getRecipientAddressItem();       //获取订单的收件人信息
			
			String totalTimes[]=mDeliveryInfoItem.getQjtm().split(" ");    //获取并且显示订单的预约取件日期、具体时间
			String time=totalTimes[1];
			if(time.equals("00:00")){  //具体时间为“00:00”时，代表用户先前选择的是“马上寄出"
				
				isSendImmediately=true;
				reserveDate=mDeliveryInfoItem.getQjtm().split(" ")[0];
				dateShow=getResources().getStringArray(R.array.date_items)[0];
				mSelectedTimeShowView.setText(dateShow);
				mSenderTimePromptTv.setVisibility(View.VISIBLE);
			}else{
				reserveDate=mDeliveryInfoItem.getQjtm().split(" ")[0];
				reserveTime=mDeliveryInfoItem.getQjtm().split(" ")[1];
				mSelectedTimeShowView.setText(reserveDate+" "+reserveTime);
				mSenderTimePromptTv.setVisibility(View.GONE);
			}
			
			mAddSenderAddressContainer.setVisibility(View.GONE);
			mSenderInfoContainer.setVisibility(View.VISIBLE);
			
			mAddRecipientAddressContainer.setVisibility(View.GONE);
			mRecipientAddressInfoContainer.setVisibility(View.VISIBLE);
			
			mSenderNameTv.setText(mSenderAddressItem.getName());
			mSenderPhoneTv.setText(mSenderAddressItem.getTelp());
			mSenderTotalAddressTv.setText(mSenderAddressItem.getpName()+mSenderAddressItem.getcName()+mSenderAddressItem.getdName()+mSenderAddressItem.getAddress());
			
			mRecipientNameTv.setText(mRecipientAddressItem.getName());
			mRecipientPhoneTv.setText(mRecipientAddressItem.getTelp());
			mRecipientTotalAddressTv.setText(mRecipientAddressItem.getpName()+mRecipientAddressItem.getcName()+mRecipientAddressItem.getdName()+mRecipientAddressItem.getAddress());
			
            mRemarkEt.setText(mDeliveryInfoItem.getComet());
			
			mSenderNameEt.setText(mDeliveryInfoItem.getGnam());
			
			mWeightEt.setText(mDeliveryInfoItem.getWeight());
			
			mParityComapnyShowView.setText(mDeliveryInfoItem.getCname());
			
			mParityCompanyItem=new ParityCompanyItem();
			mParityCompanyItem.setCid(mDeliveryInfoItem.getCid());
			mParityCompanyItem.setCname(mDeliveryInfoItem.getCname());
			mParityCompanyItem.setWid(mDeliveryInfoItem.getWid());
			
			if(mDeliveryInfoItem.getStat()!=0){   //status为0时，代表该订单只是提交了，这种情况下，订单可编辑，否则只能查看订单信息
				
				mRemarkEt.setEnabled(false);
				mWeightEt.setEnabled(false);
				mSenderNameEt.setEnabled(false);
				
				mAddSenderAddressContainer.setEnabled(false);
				mAddRecipientAddressContainer.setEnabled(false);
				mSenderInfoContainer.setEnabled(false);
				mRecipientAddressInfoContainer.setEnabled(false);
				mPlusBtn.setEnabled(false);
				mMinusBtn.setEnabled(false);
				mParityContainer.setEnabled(false);
				mReserveTimeContainer.setEnabled(false);
		
				mSummitBtn.setVisibility(View.GONE);
			}
			
		}else{    //“新增发件”时，初始化预约取件的时间，当前时间在(6:00-18:00)时，显示"马上寄出"，否则显示隔天日期+"6:00"
			
			if(Utils.isTodayAvailable()){   //当前时间在(6:00-18:00)内
				
				isSendImmediately=true;
				
				dateShow=getResources().getStringArray(R.array.date_items)[0];
				reserveDate=Utils.getFuture4Date()[0];
				reserveTime="";
				
				Log.e("zyf","send init dateShow: "+dateShow+"  reserveDate: "+reserveDate);
				
				mSelectedTimeShowView.setText(dateShow);
				mSenderTimePromptTv.setVisibility(View.VISIBLE);
				
			}else{   //当前时间在(6:00-18:00)外，今天不可以发件
				
				isSendImmediately=false;
				
				
				dateShow=getResources().getStringArray(R.array.date_items_today_not_available)[0];
				reserveDate=Utils.getFuture4Date()[2];
				reserveTime="6:00";
				
				Log.e("zyf","send init dateShow: "+dateShow+"  reserveDate: "+reserveDate+"  reserveTime: "+reserveTime);
				
				//mSelectedTimeShowView.setText(dateShow+"_"+reserveTime);
				mSelectedTimeShowView.setText(reserveDate+"  "+reserveTime);
				
				mSenderTimePromptTv.setVisibility(View.GONE);
			}
		}
		
		//更新“订单提交”按钮的状态
		updateSummitBtnStatus();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {	
		case R.id.mReserveTimeContainer:         //显示预约日期时间选择窗口
			
			if(mTimeSelectorView.getVisibility()==View.GONE){
				mTimeSelectorView.refresh();
				mTimeSelectorView.setVisibility(View.VISIBLE);
				
				mHandler.post(new Runnable() {  //用于使mTimeSelectorView完全显示在用户的可见窗口
				    @Override
				    public void run() {
				    	mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				    }
				});  
			}else{
				mTimeSelectorView.cancel();
			}
			
			break;
		case R.id.mSummitBtn:
			
			String content;
			
			if(!isDeliveryInfoModify){  //“新增寄件”
				
				/*if(mSenderAddressItem==null){
					MyToast.makeText(getActivity(), getString(R.string.prompt_select_sender), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(mRecipientAddressItem==null){
					MyToast.makeText(getActivity(), getString(R.string.prompt_select_recipient), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(mWeightEt.getText().toString().length()==0){
					MyToast.makeText(getActivity(), getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(Float.parseFloat(mWeightEt.getText().toString())==0){
					MyToast.makeText(getActivity(), getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
					
					return;
				}
				
				if(mParityCompanyItem==null){
					MyToast.makeText(getActivity(), getString(R.string.prompt_select_parity), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(reserveDate.length()==0){
					
					MyToast.makeText(mContext, getString(R.string.prompt_select_date), MyToast.LENGTH_SHORT).show();
					
					return;
				}
				
				if(!isSendImmediately){
					
					if(reserveTime.length()==0){
						
						MyToast.makeText(mContext, getString(R.string.prompt_select_time), MyToast.LENGTH_SHORT).show();
						
						return;
					}
					
				}*/
				
				content=formatSendAddContent();
				
				if(mSendAddAsyncTaskDataLoader!=null){
					mSendAddAsyncTaskDataLoader.canceled();
				}
				
				mSendAddAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_SEND_ADD, UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_ADD_URL, null, content);
				mSendAddAsyncTaskDataLoader.setOnDataLoaderListener(this);
				
				mSendAddAsyncTaskDataLoader.execute();
				
			}else{    //"编辑寄件"
				
				content=formatSendModifyContent();
				
				if(mSendModifyAsyncTaskDataLoader!=null){
					mSendModifyAsyncTaskDataLoader.canceled();
				}
				
				mSendModifyAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_SEND_MODIFY, UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_MODIFY_URL, null, content);
				mSendModifyAsyncTaskDataLoader.setOnDataLoaderListener(this);
				
				mSendModifyAsyncTaskDataLoader.execute();
			}
			
			break;
		case R.id.mSenderInfoContainer:      //编辑"寄件人信息"
		case R.id.mAddSenderAddressContainer:  
			
			Intent intent=new Intent(mContext,AddressManagerActivity.class);
			intent.putExtra("type", "0");
			intent.putExtra("isNeedReturn", "0");
			startActivityForResult(intent, ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR);
			
			break;
		case R.id.mRecipientAddressInfoContainer:      //编辑“收件人信息”
		case R.id.mAddRecipientAddressContainer:  
			
			Intent intent1=new Intent(mContext,AddressManagerActivity.class);
			intent1.putExtra("type", "1");
			intent1.putExtra("isNeedReturn", "0");
			startActivityForResult(intent1, ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR);
			
			break;
		/*case R.id.mSenderInfoContainer:
			Intent intent5=new Intent(mContext,AddressManagerActivity.class);
			intent5.putExtra("type", "0");
			intent5.putExtra("isNeedReturn", "0");
			startActivityForResult(intent5, ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR);
			break;
		case R.id.mRecipientAddressInfoContainer:
			Intent intent6=new Intent(mContext,AddressManagerActivity.class);
			intent6.putExtra("type", "1");
			intent6.putExtra("isNeedReturn", "0");
			startActivityForResult(intent6, ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR);
			break;*/
		case R.id.mParityContainer:   //“比价”
			
			if(mSenderAddressItem==null){
				MyToast.makeText(getActivity(), getString(R.string.prompt_select_sender), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mRecipientAddressItem==null){
				MyToast.makeText(getActivity(), getString(R.string.prompt_select_recipient), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mWeightEt.getText().toString().length()==0){
				MyToast.makeText(getActivity(), getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(Float.parseFloat(mWeightEt.getText().toString())==0){
				MyToast.makeText(getActivity(), getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			Intent intent2=new Intent(mContext,ParityActivity.class);
			intent2.putExtra("sdid", mSenderAddressItem.getdId());
			intent2.putExtra("sname", mSenderAddressItem.getcName());
			intent2.putExtra("rdid", mRecipientAddressItem.getdId());
			intent2.putExtra("rname", mRecipientAddressItem.getcName());
			intent2.putExtra("weight", mWeightEt.getText().toString());
			startActivityForResult(intent2, ActivityResultCode.CODE_PARITY_SELECTOR);
			break;
		case R.id.mMinusBtn:  //weight的"-"功能
			
			String str=mWeightEt.getText().toString();
			
			if(str.length()==0){
				MyToast.makeText(getActivity(), getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			float weight=Float.parseFloat(str);
			
			if(weight>=1){
				weight--;
			}
			
			mWeightEt.setText(weight+"");
			mWeightEt.setSelection(mWeightEt.getText().toString().length());
			
			updateSummitBtnStatus();
			
			break;
		case R.id.mPlusBtn:   //weight的"+"功能
			
			String str2=mWeightEt.getText().toString();
			
			if(str2.length()==0){
				MyToast.makeText(getActivity(), getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			float weight2=Float.parseFloat(str2);
			
			weight2++;
			
			mWeightEt.setText(weight2+"");
			mWeightEt.setSelection(mWeightEt.getText().toString().length());
			
			updateSummitBtnStatus();
			
			break;
		default:
			break;
		}
	}
	
	//用于自动更新"订单提交"按钮的enable属性
	private void updateSummitBtnStatus(){
		
		mSummitBtn.setEnabled(false);
		
		if(mSenderAddressItem==null){
			return;
		}
		
		if(mRecipientAddressItem==null){
			return;
		}
		
		if(mWeightEt.getText().toString().length()==0){
			return;
		}
		
		if(Float.parseFloat(mWeightEt.getText().toString())==0){
			return;
		}
		
		if(mParityCompanyItem==null){
			return;
		}
		
		if(reserveDate.length()==0){		
			return;
		}
		
		if(!isSendImmediately){
			
			if(reserveTime.length()==0){
				
				return;
			}
		}
		
		mSummitBtn.setEnabled(true);
	}

	//格式化“新增寄件”接口的json数据
	private String formatSendAddContent(){

		String weight=(int)(Float.parseFloat(mWeightEt.getText().toString())*1000)+"";    //防止最后出现一个小数点
		
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
	               +"\"s_nam\":"+"\""+mSenderAddressItem.getName()+"\""+","
	               +"\"s_tel\":"+"\""+mSenderAddressItem.getTelp()+"\""+","
	               +"\"s_pid\":"+"\""+mSenderAddressItem.getpId()+"\""+","
	               +"\"s_cid\":"+"\""+mSenderAddressItem.getcId()+"\""+","
	               +"\"s_did\":"+"\""+mSenderAddressItem.getdId()+"\""+","
	               +"\"s_zcod\":"+"\""+mSenderAddressItem.getZcode()+"\""+","  
	               +"\"s_adr\":"+"\""+mSenderAddressItem.getAddress()+"\""+","
	               +"\"r_nam\":"+"\""+mRecipientAddressItem.getName()+"\""+","
	               +"\"r_tel\":"+"\""+mRecipientAddressItem.getTelp()+"\""+","
	               +"\"r_pid\":"+"\""+mRecipientAddressItem.getpId()+"\""+","
	               +"\"r_cid\":"+"\""+mRecipientAddressItem.getcId()+"\""+","
	               +"\"r_did\":"+"\""+mRecipientAddressItem.getdId()+"\""+","
	               +"\"r_adr\":"+"\""+mRecipientAddressItem.getAddress()+"\""+","
	               +"\"r_zcod\":"+"\""+mRecipientAddressItem.getZcode()+"\""+","
	               +"\"size\":"+"\""+"100"+"\""+","   //体积
	               +"\"gnam\":"+"\""+mSenderNameEt.getText().toString()+"\""+","   //名称
	               +"\"weight\":"+"\""+weight+"\""+","  //重量
	               +"\"gtype\":"+"\""+"liquid"+"\""+","  //货物状态
	               +"\"qjtm\":"+"\""+reserveDate+"_"+reserveTime+"\""+","
	               +"\"comet\":"+"\""+mRemarkEt.getText().toString()+"\""+","
	               +"\"cid\":"+"\""+mParityCompanyItem.getCid()+"\""+","
				   +"\"wid\":"+"\""+mParityCompanyItem.getWid()+"\""+"}";
		
		Log.e("zyf","add send: "+str);
		return str; 
	}
	
	//格式化“编辑寄件”接口的json数据
	private String formatSendModifyContent(){
		
		String weight=(int)(Float.parseFloat(mWeightEt.getText().toString())*1000)+"";   //防止最后出现一个小数点
		
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
				   +"\"id\":"+"\""+mDeliveryInfoItem.getId()+"\""+","
	               +"\"s_nam\":"+"\""+mSenderAddressItem.getName()+"\""+","
	               +"\"s_tel\":"+"\""+mSenderAddressItem.getTelp()+"\""+","
	               +"\"s_pid\":"+"\""+mSenderAddressItem.getpId()+"\""+","
	               +"\"s_cid\":"+"\""+mSenderAddressItem.getcId()+"\""+","
	               +"\"s_did\":"+"\""+mSenderAddressItem.getdId()+"\""+","
	               +"\"s_adr\":"+"\""+mSenderAddressItem.getAddress()+"\""+","
	               +"\"s_zcod\":"+"\""+mSenderAddressItem.getZcode()+"\""+","  
	               +"\"r_nam\":"+"\""+mRecipientAddressItem.getName()+"\""+","
	               +"\"r_tel\":"+"\""+mRecipientAddressItem.getTelp()+"\""+","
	               +"\"r_pid\":"+"\""+mRecipientAddressItem.getpId()+"\""+","
	               +"\"r_cid\":"+"\""+mRecipientAddressItem.getcId()+"\""+","
	               +"\"r_did\":"+"\""+mRecipientAddressItem.getdId()+"\""+","
	               +"\"r_adr\":"+"\""+mRecipientAddressItem.getAddress()+"\""+","
	               +"\"r_zcod\":"+"\""+mRecipientAddressItem.getZcode()+"\""+"," 
	               +"\"size\":"+"\""+"100"+"\""+","   //体积
	               +"\"gnam\":"+"\""+mSenderNameEt.getText().toString()+"\""+","   //名称
	               +"\"weight\":"+"\""+weight+"\""+","  //重量
	               +"\"gtype\":"+"\""+"liquid"+"\""+","  //货物状态
	               +"\"qjtm\":"+"\""+reserveDate+"_"+reserveTime+"\""+","
	               +"\"comet\":"+"\""+mRemarkEt.getText().toString()+"\""+","
	               +"\"cid\":"+"\""+mParityCompanyItem.getCid()+"\""+","
				   +"\"wid\":"+"\""+mParityCompanyItem.getWid()+"\""+"}";
		return str; 
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR){   //获取编辑成功后的寄件人信息
			
			if(data!=null){
				
				mSenderAddressItem = (AddressItem) data.getSerializableExtra("AddressItem");
				Log.e("zyf","sender name: "+mSenderAddressItem.getName());
				
				mSenderNameTv.setText(mSenderAddressItem.getName());
				mSenderPhoneTv.setText(mSenderAddressItem.getTelp());
				
				mSenderTotalAddressTv.setText(mSenderAddressItem.getpName()+mSenderAddressItem.getcName()+mSenderAddressItem.getdName()+mSenderAddressItem.getAddress());
				
				mAddSenderAddressContainer.setVisibility(View.GONE);
				mSenderInfoContainer.setVisibility(View.VISIBLE);
				
				if(isDeliveryInfoModify){
					mDeliveryInfoItem.setSenderAddressItem(mSenderAddressItem);
				}
				
				updateSummitBtnStatus();
			}
			
		}else if(requestCode==ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR){   //获取编辑成功后的发件人信息
			
			if(data!=null){
				
				mRecipientAddressItem = (AddressItem) data.getSerializableExtra("AddressItem");
				Log.e("zyf","recipient name: "+mRecipientAddressItem.getName());
				
				mRecipientNameTv.setText(mRecipientAddressItem.getName());
				mRecipientPhoneTv.setText(mRecipientAddressItem.getTelp());
				
				mRecipientTotalAddressTv.setText(mRecipientAddressItem.getpName()+mRecipientAddressItem.getcName()+mRecipientAddressItem.getdName()+mRecipientAddressItem.getAddress());
				
				mAddRecipientAddressContainer.setVisibility(View.GONE);
				mRecipientAddressInfoContainer.setVisibility(View.VISIBLE);
				
				if(isDeliveryInfoModify){
					mDeliveryInfoItem.setRecipientAddressItem(mRecipientAddressItem);
				}
				
				updateSummitBtnStatus();
			}
			
		}else if(requestCode==ActivityResultCode.CODE_PARITY_SELECTOR){  //获取比价后的网点信息
			
			if(data!=null){
				
				mParityCompanyItem = (ParityCompanyItem) data.getSerializableExtra("ParityCompanyItem");
				Log.e("zyf","parity company name: "+mParityCompanyItem.getCname());
				
				mParityComapnyShowView.setText(mParityCompanyItem.getCname());
				
				updateSummitBtnStatus();
			}
		}else if(requestCode==ActivityResultCode.CODE_LOGIN){   //用户重新登录成功后
			
			if(data!=null){
				
				if(data.getBooleanExtra("loginStatus", false)){
					
					Log.e("zyf","login status : true");
					
					if(mode==MODE_GET_ADDRESS){   //若是“新增寄件”，则更新用户默认“寄件人信息”、“收件人信息”
						
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_ADDRESS_LIST_URL+"?sesn="+MyApplicaition.sesn;
						mGetAllAddressAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_ALL_ADDRESS, url, null, null);
						mGetAllAddressAsyncTaskDataLoader.setOnDataLoaderListener(this);
						mGetAllAddressAsyncTaskDataLoader.execute();
					}
				}
			}else{
				
				getActivity().finish();    //用户登录失败，直接退出页面

			}
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_SEND_ADD){    //开始调用“新增寄件”接口
			
			Log.e("zyf","adding new send......");
			
			Utils.sendMessage(mHandler, SEND_ADDING);
			
		}else if(flag==Flag.FLAG_SEND_MODIFY){   //开始调用“编辑寄件”接口
			
			Log.e("zyf","send modifying......");
			
			Utils.sendMessage(mHandler, SEND_MODIFYING);
			
		}else if(flag==Flag.FLAG_GET_ALL_ADDRESS){   //开始调用“常用地址”接口
			
			Utils.sendMessage(mHandler, ALL_ADDRESS_GETTING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_SEND_ADD){      //获取“新增寄件”返回结果
			
			Log.e("zyf","add new send result: "+result);
			
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){       //“新增寄件”成功
					
					Utils.sendMessage(mHandler, SEND_ADD_SUCCESS);
					
					return;
					
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(getActivity(), UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, SEND_ADD_FAILED);      //“新增寄件”失败
			
		}else if(flag==Flag.FLAG_GET_ALL_ADDRESS){      //获取“常用地址”返回结果
			
			try {
				
				Log.e("zyf","address: "+result);
				
				JSONObject totalJsonObject=new JSONObject(result);						
				
				String rc=totalJsonObject.getString("rc");
				
				Log.e("zyf: ","rc: "+rc);
				 
				if("0".equals(rc)){       //获取“常用地址”成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, ALL_ADDRESS_GET_SUCCESS);
						
						return;
					}
					
					JSONArray addressJSONArray=dataJsonObject.getJSONArray("items");
					
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
							
							if(item.getDef()==1){  //保存默认发件人地址
								
								mSenderAddressItem=item;
							}
						}else{
							
							if(item.getDef()==1){  //保存默认收件人地址
								
								mRecipientAddressItem=item;
							}
							
						}
					}
					
		            Utils.sendMessage(mHandler, ALL_ADDRESS_GET_SUCCESS);
		            
		            return;
		            
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(getActivity(), UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, ALL_ADDRESS_GET_FAILED);    //获取“常用地址”失败
			
            return;
		}else if(flag==Flag.FLAG_SEND_MODIFY){      //获取“编辑寄件”返回结果
			
			Log.e("zyf","send modify result: "+result);
			
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){      //“编辑寄件”成功
					
					Utils.sendMessage(mHandler, SEND_MODIFY_SUCCESS);
					
					return;
					
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(getActivity(), UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, SEND_MODIFY_FAILED);      //“编辑寄件”失败
			
		}
	}

	//预约取件时间TimeSelectorView的回调函数，当用户选择具体时间后回调该接口
	@Override
	public void OnTimeSelected(String time) {
		
		Log.e("zyf","selected time: "+time);
		
		reserveTime=time;
		
		/*if(reserveDate.length()>1){
			mSelectedTimeShowView.setText(dateShow+" "+reserveTime);
		}else{
			mSelectedTimeShowView.setText(dateShow);
		}*/
		
		if(reserveDate.length()>1){
			mSelectedTimeShowView.setText(reserveDate+"  "+reserveTime);
		}else{
			mSelectedTimeShowView.setText(reserveDate);
		}
		
		updateSummitBtnStatus();
	}
	

	//预约取件时间TimeSelectorView的回调函数，当用户选择日期后回调该接口
	@Override
	public void OnDateSelected(String dateShow2, String realDate, boolean immediately) {
		
        Log.e("zyf","selected date: "+realDate);
        
        isSendImmediately=immediately;
		
        dateShow=dateShow2;
		reserveDate=realDate;
		
		if(!immediately){
			/*if(reserveTime.length()>1){
				mSelectedTimeShowView.setText(dateShow+" "+reserveTime);
			}else{
				mSelectedTimeShowView.setText(dateShow);
			}*/
			
			if(reserveTime.length()>1){
				mSelectedTimeShowView.setText(reserveDate+"  "+reserveTime);
			}else{
				mSelectedTimeShowView.setText(reserveDate);
			}
			
			mSenderTimePromptTv.setVisibility(View.GONE);
		}else{
			mSelectedTimeShowView.setText(dateShow);
			reserveTime="";
			
			mSenderTimePromptTv.setVisibility(View.VISIBLE);
		}
		
		updateSummitBtnStatus();
	}
}
