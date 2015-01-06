package com.weiwend.invalid;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.weiwend.fooldelivery.AddressManagerActivity;
import com.weiwend.fooldelivery.BaseActivity;
import com.weiwend.fooldelivery.MyApplicaition;
import com.weiwend.fooldelivery.ParityActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.UserLoginActivity;
import com.weiwend.fooldelivery.R.drawable;
import com.weiwend.fooldelivery.R.id;
import com.weiwend.fooldelivery.R.layout;
import com.weiwend.fooldelivery.R.string;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyTabView;
import com.weiwend.fooldelivery.customviews.MyTabView.MyOnTabClickLister;
import com.weiwend.fooldelivery.customviews.MyReboundScrollView;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.customviews.TimeSelectorView;
import com.weiwend.fooldelivery.customviews.TimeSelectorView.MyOnTimeSelectedClickLister;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.DeliveryCompanyItem;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.ParityCompanyItem;
import com.weiwend.fooldelivery.items.SendInfoItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

//******************************该界面由于需求问题，暂时已经作废*******************************
public class SenderModifyOldActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOnTimeSelectedClickLister,MyOnTabClickLister{
	
	private RelativeLayout mReserveTimeContainer;
	
	private RelativeLayout mSenderInfoContainer,mRecipientAddressInfoContainer;
	
	private TextView mSenderNameTv,mSenderPhoneTv,mSelectedTimeShowView;
	
	private TextView mRecipientNameTv,mRecipientPhoneTv;
	
	private Button mMinusBtn,mPlusBtn;
	
	private RelativeLayout mAddSenderAddressContainer,mAddRecipientAddressContainer,mParityContainer;
	
	private Button mSummitBtn;
	
	private EditText mRemarkEt,mWeightEt,mSenderNameEt;
	
	private TextView mSenderTotalAddressTv,mRecipientTotalAddressTv,mParityComapnyShowView;
	private TextView mSenderTimePromptTv;
	
	private TimeSelectorView mTimeSelectorView;
	
	private MyTabView mTabView;
	
	private MyReboundScrollView mScrollView;
	
	private int mTabViewNormalIcons[]={R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher};
	private int mTabViewActiveIcons[]={R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher};
	
	private ArrayList<DeliveryCompanyItem> mDeliveryCompanyList=new ArrayList<DeliveryCompanyItem>();
	
	private AddressItem mSenderAddressItem,mRecipientAddressItem;
	
	private ParityCompanyItem mParityCompanyItem;
	
	private AsyncTaskDataLoader mSendAddAsyncTaskDataLoader,mSendModifyAsyncTaskDataLoader;
	
	private boolean isDeliveryInfoModify;
	private SendInfoItem mDeliveryInfoItem;
	
	private String reserveDate="",reserveTime="";
	
	private final int SEND_ADDING=0;
	private final int SEND_ADD_SUCCESS=1;
	private final int SEND_ADD_FAILED=2;
	
	private final int SEND_MODIFYING=3;
	private final int SEND_MODIFY_SUCCESS=4;
	private final int SEND_MODIFY_FAILED=5;
	
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case SEND_ADDING:
            	
				Log.e("zyf","send adding......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_add, SenderModifyOldActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_ADD_SUCCESS:
				
				Log.e("zyf","send add success......");
				
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.send_add_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case SEND_ADD_FAILED:
				
				Log.e("zyf","send add failed......");
				
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.send_add_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case SEND_MODIFYING:
            	
				Log.e("zyf","send modifying......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_modify, SenderModifyOldActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_MODIFY_SUCCESS:
				
				Log.e("zyf","send modify success......");
				
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.send_modify_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent intent=new Intent();
				intent.putExtra("isModified", true);
				setResult(ActivityResultCode.CODE_SEND_INFO_MODIFY,intent);
				finish();
            	
				break;
			case SEND_MODIFY_FAILED:
				
				Log.e("zyf","send modify failed......");
				
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.send_modify_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	private int status=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fragment_sender);
		
		status=getIntent().getIntExtra("status", 0);
		
		isDeliveryInfoModify=getIntent().getBooleanExtra("isDeliveryInfoModify", false);
		mDeliveryInfoItem=(SendInfoItem) getIntent().getSerializableExtra("DeliveryInfoItem");
		
		initViews();
	}
	
	private void initViews(){
		
		mReserveTimeContainer=(RelativeLayout)findViewById(R.id.mReserveTimeContainer);
		
		mSenderInfoContainer=(RelativeLayout)findViewById(R.id.mSenderInfoContainer);
		mRecipientAddressInfoContainer=(RelativeLayout)findViewById(R.id.mRecipientAddressInfoContainer);
		
		mSenderInfoContainer.setOnClickListener(this);
		mRecipientAddressInfoContainer.setOnClickListener(this);
		mReserveTimeContainer.setOnClickListener(this);
		
		mSenderNameTv=(TextView)findViewById(R.id.mSenderNameTv);
		mSenderPhoneTv=(TextView)findViewById(R.id.mSenderPhoneTv);
		
		mRecipientNameTv=(TextView)findViewById(R.id.mRecipientNameTv);
		mRecipientPhoneTv=(TextView)findViewById(R.id.mRecipientPhoneTv);
		
		mSenderTotalAddressTv=(TextView)findViewById(R.id.mSenderTotalAddressTv);
		mRecipientTotalAddressTv=(TextView)findViewById(R.id.mRecipientTotalAddressTv);
		mSenderTimePromptTv=(TextView)findViewById(R.id.mSenderTimePromptTv);
		
		mParityComapnyShowView=(TextView)findViewById(R.id.mParityComapnyShowView);
		
		mRemarkEt=(EditText)findViewById(R.id.mRemarkEt);
		mWeightEt=(EditText)findViewById(R.id.mWeightEt);
		mSenderNameEt=(EditText)findViewById(R.id.mSenderNameEt);
		
        mWeightEt.setSelection(mWeightEt.getText().toString().length());
		
		mWeightEt.addTextChangedListener(new TextWatcher() {
			 
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            	
            	String number=s.toString();
            	int len=number.length();
                int dotIndex=number.indexOf(".");
                //Log.e("zyf","小数点index: "+dotIndex);
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
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mSummitBtn.setOnClickListener(this);
		
		mMinusBtn=(Button)findViewById(R.id.mMinusBtn);
		mPlusBtn=(Button)findViewById(R.id.mPlusBtn);
		
		mMinusBtn.setOnClickListener(this);
		mPlusBtn.setOnClickListener(this);
		
		mAddSenderAddressContainer=(RelativeLayout)findViewById(R.id.mAddSenderAddressContainer);
		mAddRecipientAddressContainer=(RelativeLayout)findViewById(R.id.mAddRecipientAddressContainer);
		mParityContainer=(RelativeLayout)findViewById(R.id.mParityContainer);
		
		mAddSenderAddressContainer.setOnClickListener(this);
		mAddRecipientAddressContainer.setOnClickListener(this);
		mParityContainer.setOnClickListener(this);
		
		mSelectedTimeShowView=(TextView)findViewById(R.id.mSelectedTimeShowView);
		
		if(isDeliveryInfoModify){  //订单的修改功能
			
			mSenderAddressItem=mDeliveryInfoItem.getSenderAddressItem();
			mRecipientAddressItem=mDeliveryInfoItem.getRecipientAddressItem();
			
			String totalTimes[]=mDeliveryInfoItem.getQjtm().split(" ");
			String time=totalTimes[1];
			if(time.equals("00:00")){  //立即寄出
				
				isSendImmediately=true;
				reserveDate=mDeliveryInfoItem.getQjtm().split(" ")[0];
				dateShow="马上寄出";
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
		}else{
			mTabView=(MyTabView)findViewById(R.id.mTabView);
			mTabView.setDatas(mTabViewNormalIcons, mTabViewActiveIcons);
			mTabView.setOnTabClickListener(this);
			mTabView.setCurItem(2);
		}
		
		mScrollView=(MyReboundScrollView)findViewById(R.id.mScrollView);
		
		mTimeSelectorView=(TimeSelectorView)findViewById(R.id.mTimeSelectorView);
		mTimeSelectorView.setScrollView(mScrollView);
		mTimeSelectorView.setOnTimeSelectedListener(this);
		
		if(status!=0){   //不可以编辑
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
		
		updateSummitBtnStatus();
	}

	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		if(!isDeliveryInfoModify){
			mTitleView.setText(getString(R.string.title_sender));
		}else{
			mTitleView.setText(getString(R.string.title_send_modify));
		}
	}
	
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:
			finish();
			break;
		case R.id.mReserveTimeContainer:
			
			if(status!=0){
				return;
			}
			
			if(mTimeSelectorView.getVisibility()==View.GONE){
				mTimeSelectorView.refresh();
				mTimeSelectorView.setVisibility(View.VISIBLE);
				
				mHandler.post(new Runnable() {  //滚动到底部,使其完全显示
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
			
			if(status!=0){
				return;
			}
			
			String content;
			
			if(!isDeliveryInfoModify){  //新增快递订单
				
				content=formatSendAddContent();
				
				if(mSendAddAsyncTaskDataLoader!=null){
					mSendAddAsyncTaskDataLoader.canceled();
				}
				
				mSendAddAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_SEND_ADD, UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_ADD_URL, null, content);
				mSendAddAsyncTaskDataLoader.setOnDataLoaderListener(this);
				
				mSendAddAsyncTaskDataLoader.execute();
			}else{  //修改快递订单
				
				if(mSenderAddressItem==null){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_sender), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(mRecipientAddressItem==null){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_recipient), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(mWeightEt.getText().toString().length()==0){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(Float.parseFloat(mWeightEt.getText().toString())==0){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
					
					return;
				}
				
				if(mParityCompanyItem==null){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_parity), MyToast.LENGTH_SHORT).show();
					return;
				}
				
				if(!isSendImmediately){
					
					if(reserveDate.length()==0){
						
						MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_date), MyToast.LENGTH_SHORT).show();
						
						return;
					}
					
					if(reserveTime.length()==0){
						
						MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_time), MyToast.LENGTH_SHORT).show();
						
						return;
					}
				}else{
					
					if(reserveDate.length()==0){
						
						MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_date), MyToast.LENGTH_SHORT).show();
						
						return;
					}
				}
				
				if(Float.parseFloat(mWeightEt.getText().toString())==0){
					MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
					
					return;
				}
				
				content=formatSendModifyContent();
				
				if(mSendModifyAsyncTaskDataLoader!=null){
					mSendModifyAsyncTaskDataLoader.canceled();
				}
				
				mSendModifyAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_SEND_MODIFY, UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_MODIFY_URL, null, content);
				mSendModifyAsyncTaskDataLoader.setOnDataLoaderListener(this);
				
				mSendModifyAsyncTaskDataLoader.execute();
			}
			
			break;
		case R.id.mAddSenderAddressContainer:
			
			if(status!=0){
				return;
			}
			
			Intent intent3=new Intent(SenderModifyOldActivity.this,AddressManagerActivity.class);
			intent3.putExtra("type", "0");
			intent3.putExtra("isNeedReturn", "0");
			startActivityForResult(intent3, ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR);
			break;
		case R.id.mAddRecipientAddressContainer:
			
			if(status!=0){
				return;
			}
			
			Intent intent4=new Intent(SenderModifyOldActivity.this,AddressManagerActivity.class);
			intent4.putExtra("type", "1");
			intent4.putExtra("isNeedReturn", "0");
			startActivityForResult(intent4, ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR);
			break;
		case R.id.mParityContainer:
			
			if(status!=0){
				return;
			}
			
			if(mSenderAddressItem==null){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_sender), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mRecipientAddressItem==null){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_recipient), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mWeightEt.getText().toString().length()==0){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(Float.parseFloat(mWeightEt.getText().toString())==0){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_weight_must_max_zone), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			Intent intent7=new Intent(SenderModifyOldActivity.this,ParityActivity.class);
			intent7.putExtra("sdid", mSenderAddressItem.getdId());
			intent7.putExtra("sname", mSenderAddressItem.getcName());
			intent7.putExtra("rdid", mRecipientAddressItem.getdId());
			intent7.putExtra("rname", mRecipientAddressItem.getcName());
			intent7.putExtra("weight", mWeightEt.getText().toString());
			startActivityForResult(intent7, ActivityResultCode.CODE_PARITY_SELECTOR);
			break;
		case R.id.mSenderInfoContainer:
			
			if(status!=0){
				return;
			}
			
			Intent intent5=new Intent(SenderModifyOldActivity.this,AddressManagerActivity.class);
			intent5.putExtra("type", "0");
			intent5.putExtra("isNeedReturn", "0");
			startActivityForResult(intent5, ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR);
			break;
		case R.id.mRecipientAddressInfoContainer:
			
			if(status!=0){
				return;
			}
			
			Intent intent6=new Intent(SenderModifyOldActivity.this,AddressManagerActivity.class);
			intent6.putExtra("type", "1");
			intent6.putExtra("isNeedReturn", "0");
			startActivityForResult(intent6, ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR);
			break;
       case R.id.mMinusBtn:
    	   
    	    if(status!=0){
				return;
			}
			
			String str=mWeightEt.getText().toString();
			
			if(str.length()==0){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
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
		case R.id.mPlusBtn:
			
			if(status!=0){
				return;
			}
			
			String str2=mWeightEt.getText().toString();
			
			if(str2.length()==0){
				MyToast.makeText(SenderModifyOldActivity.this, getString(R.string.prompt_select_weight), MyToast.LENGTH_SHORT).show();
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
	
	private String formatSendAddContent(){
		
		String weight=(int)(Float.parseFloat(mWeightEt.getText().toString())*1000)+"";
		
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
		return str; 
	}
	
	private String formatSendModifyContent(){
		
		String weight=(int)(Float.parseFloat(mWeightEt.getText().toString())*1000)+"";
		
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
		
		if(requestCode==ActivityResultCode.CODE_ADDRESS_SENDER_SELECTOR){
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
		}else if(requestCode==ActivityResultCode.CODE_ADDRESS_RECIPIENT_SELECTOR){
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
		}else if(requestCode==ActivityResultCode.CODE_PARITY_SELECTOR){
			if(data!=null){
				mParityCompanyItem = (ParityCompanyItem) data.getSerializableExtra("ParityCompanyItem");
				Log.e("zyf","parity company name: "+mParityCompanyItem.getCname());
				
				mParityComapnyShowView.setText(mParityCompanyItem.getCname());
				
				updateSummitBtnStatus();
			}
		}
	}

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_DELIVERY_COMPANY){
			Log.e("zyf","get delivery comp......");
		}else if(flag==Flag.FLAG_SEND_ADD){
			
			Utils.sendMessage(mHandler, SEND_ADDING);
		}else if(flag==Flag.FLAG_SEND_MODIFY){
			
			Utils.sendMessage(mHandler, SEND_MODIFYING);
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_DELIVERY_COMPANY){
			Log.e("zyf","result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				if("0".equals(rc)){
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray deliveryCompJsonArray=dataJsonObject.getJSONArray("items");
					JSONArray stationJsonArray=dataJsonObject.getJSONArray("station");
					
					JSONObject jsonObject;
					
					DeliveryCompanyItem deliveryCompanyItem;
					mDeliveryCompanyList.clear();
				    for(int i=0;i<deliveryCompJsonArray.length();i++){
				    	jsonObject=deliveryCompJsonArray.getJSONObject(i);
				    	
				    	deliveryCompanyItem=new DeliveryCompanyItem();
				    	deliveryCompanyItem.setId(jsonObject.getString("id"));
				    	deliveryCompanyItem.setName(jsonObject.getString("name"));
				    	deliveryCompanyItem.setTelp(jsonObject.getString("telp"));
				    	deliveryCompanyItem.setDesp(jsonObject.getString("desp"));
				    	deliveryCompanyItem.setLogo(jsonObject.getString("logo"));
				    	
				    	mDeliveryCompanyList.add(deliveryCompanyItem);
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(flag==Flag.FLAG_SEND_ADD){
			Log.e("zyf","add new send result: "+result);
			
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){
					
					Utils.sendMessage(mHandler, SEND_ADD_SUCCESS);
					
					return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, SEND_ADD_FAILED);
			
		}else if(flag==Flag.FLAG_SEND_MODIFY){
			
			Log.e("zyf","send modify result: "+result);
			
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){
					
					Utils.sendMessage(mHandler, SEND_MODIFY_SUCCESS);
					
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
			
			Utils.sendMessage(mHandler, SEND_MODIFY_FAILED);
			
		}
	}

	/*@Override
	public void OnDateSelected(String date) {
		
		Log.e("zyf","selected date: "+date);
		
		reserveDate=date;
		
		if(reserveTime.length()>1){
			mSelectedTimeShowView.setText(reserveDate+"_"+reserveTime);
		}else{
			mSelectedTimeShowView.setText(reserveDate);
		}
	}*/
	

	private String dateShow;
	private boolean isSendImmediately;
	
	@Override
	public void OnDateSelected(String dateShow2, String realDate, boolean immediately) {
		
        Log.e("zyf","selected date: "+realDate);
        
        isSendImmediately=immediately;
		
        dateShow=dateShow2;
		reserveDate=realDate;
		
		if(!immediately){
			/*if(reserveTime.length()>1){
				mSelectedTimeShowView.setText(dateShow+"_"+reserveTime);
			}else{
				mSelectedTimeShowView.setText(dateShow);
			}*/
			
			if(reserveTime.length()>1){
				mSelectedTimeShowView.setText(reserveDate+"   "+reserveTime);
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

	@Override
	public void OnTimeSelected(String time) {
		
		Log.e("zyf","selected time: "+time);
		
		reserveTime=time;
		
		/*if(reserveDate.length()>1){
			mSelectedTimeShowView.setText(dateShow+"_"+reserveTime);
		}else{
			mSelectedTimeShowView.setText(reserveTime);
		}*/
		
		if(reserveDate.length()>1){
			mSelectedTimeShowView.setText(reserveDate+"  "+reserveTime);
		}else{
			mSelectedTimeShowView.setText(reserveTime);
		}
		
		updateSummitBtnStatus();
	}

	@Override
	public void OnTabClick(int choice) {
		
		switch (choice) {
		case 0:   //回首页
			finish();
			break;
		case 1:   //回查件界面
			/*Intent intent=new Intent();
			intent.putExtra("choice", 1);
			setResult(ActivityResultCode.CODE_QUICK_SELECTOR, intent);
			
			finish();*/
			break;
		case 2:   //当前界面
			break;
		case 3:   //回消息界面
			/*Intent intent2=new Intent();
			intent2.putExtra("choice", 3);
			setResult(ActivityResultCode.CODE_QUICK_SELECTOR, intent2);*/
			
			finish();
			break;
		default:
			break;
		}
	}

}
