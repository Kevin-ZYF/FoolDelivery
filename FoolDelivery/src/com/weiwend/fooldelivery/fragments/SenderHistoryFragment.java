package com.weiwend.fooldelivery.fragments;

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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.weiwend.fooldelivery.MyApplicaition;
import com.weiwend.fooldelivery.QueryActivity;
import com.weiwend.fooldelivery.QueryResultActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.SenderModifyActivity;
import com.weiwend.fooldelivery.UserLoginActivity;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.DelSlideListView;
import com.weiwend.fooldelivery.customviews.MyDeleteDialog;
import com.weiwend.fooldelivery.customviews.MyLoaderImageView;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.customviews.OnDeleteListioner;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.SendInfoItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;
import com.weiwend.invalid.SenderModifyOldActivity;

public class SenderHistoryFragment extends Fragment implements onDataLoaderListener,OnRefreshListener<ScrollView>{
	
	//fragment的上下文
	private Context mContext;
	
	//解决下拉刷新与侧滑删除的冲突
	private PullToRefreshScrollView mRefreshScrollview;
	
	//“历史寄件”Listview，支持侧滑删除
	private DelSlideListView mDelSlideListView;
	
	//“历史寄件”Listview的适配器
	private MyDeletedListViewBaseAdapter mDeletedListViewBaseAdapter;
	
	//调用获取历史寄件接口后的各种状态信息
	private final int SEND_HISTORY_GETTING=0;
	private final int SEND_HISTORY_GET_SUCCESS=1;
	private final int SEND_HISTORY_GET_FAILED=2;
	
	//调用取消指定历史寄件接口后的各种状态信息
	private final int SEND_CANCELLING=3;
	private final int SEND_CANCEL_SUCCESS=4;
	private final int SEND_CANCEL_FAILED=5;
	
	//保存所有的历史寄件
	private ArrayList<SendInfoItem> mSendInfoItems=new ArrayList<SendInfoItem>();
	
	//寄件的四种状态信息
	private String[] statusTexts;
	
	//记录用户删除寄件的列表位置
	private int cancelledPositon;
	
	//耗时网络操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case SEND_HISTORY_GETTING:       //获取历史寄件的接口已经调用，正在等待服务器端的回复
            	
            	mode=MODE_GET_SEND_HISTORY;
            	
				Log.e("zyf","send history getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_history_get, mContext);
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_HISTORY_GET_SUCCESS:      //历史寄件获取成功
				
				mode=-1;
				
				mRefreshScrollview.onRefreshComplete();
				
				Log.e("zyf","send history get success......");
				
				if(mDeletedListViewBaseAdapter==null){
					mDeletedListViewBaseAdapter=new MyDeletedListViewBaseAdapter(getActivity());
					mDelSlideListView.setAdapter(mDeletedListViewBaseAdapter);
					Utils.setListViewHeightBasedOnChildren(mDelSlideListView);
				}else{
					mDeletedListViewBaseAdapter.notifyDataSetChanged();
					Utils.setListViewHeightBasedOnChildren(mDelSlideListView);
				}
				
				MyToast.makeText(mContext, getString(R.string.send_history_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case SEND_HISTORY_GET_FAILED:      //历史寄件获取失败
				
				Log.e("zyf","send history get failed......");
				
				mRefreshScrollview.onRefreshComplete();
				
				MyToast.makeText(mContext, getString(R.string.send_history_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
           case SEND_CANCELLING:      //取消指定寄件的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","send cancel......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_send_cancel, mContext);
				mProgressDialogUtils.showDialog();
				
				break;
			case SEND_CANCEL_SUCCESS:      //取消指定寄件成功
				
				Log.e("zyf","send cancel success......");
				
				mSendInfoItems.remove(cancelledPositon);
				
				if(mDeletedListViewBaseAdapter!=null){
					
					mDelSlideListView.deleteItem();
					mDeletedListViewBaseAdapter.notifyDataSetChanged();
					Utils.setListViewHeightBasedOnChildren(mDelSlideListView);
				}
				
				MyToast.makeText(mContext, getString(R.string.send_cancel_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case SEND_CANCEL_FAILED:      //取消指定寄件失败
				
				Log.e("zyf","send cancel failed......");
				
				MyToast.makeText(mContext, getString(R.string.send_cancel_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//获取历史寄件的异步加载类
	private AsyncTaskDataLoader mSendHistoryAsyncTaskDataLoader;
	
	//取消指定寄件的异步加载类
	private AsyncTaskDataLoader mSendDeleteAsyncTaskDataLoader;
	
	//用于sesn过期，用户重新登陆成功后，页面重新刷新历史寄件
	private int mode=-1;
	private int MODE_GET_SEND_HISTORY=0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mContext=getActivity();
		
		statusTexts=getResources().getStringArray(R.array.status_text);
		
		View contentView=inflater.inflate(R.layout.fragment_sender_history, container, false);
		
		mRefreshScrollview=(PullToRefreshScrollView)contentView.findViewById(R.id.mRefreshScrollview);
		mRefreshScrollview.setOnRefreshListener(this);
		
		mDelSlideListView=(DelSlideListView)contentView.findViewById(R.id.mDelSlideListView);
		mDelSlideListView.setDeleteListioner(new OnDeleteListioner() {
			
			@Override
			public void onDelete(int ID) {
			}
			
			@Override
			public void onBack() {
			}
			
			@Override
			public boolean isCandelete(int position) {
				return true;
			}

			@Override
			public void onEdit(int position) {
				// TODO Auto-generated method stub
				
			}
		});
		mDelSlideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				/*SendInfoItem item=mSendInfoItems.get(position);
				
				if(item.getStat()==1||item.getStat()==2||item.getStat()==3){
					
					Intent intent=new Intent(mContext,SenderModifyActivity.class);
					intent.putExtra("isDeliveryInfoModify", true);
					intent.putExtra("status", item.getStat());
					intent.putExtra("DeliveryInfoItem", mSendInfoItems.get(position));
					startActivityForResult(intent, ActivityResultCode.CODE_SEND_INFO_MODIFY);
				}else{*/
					
				    //寄件信息的查看/修改
					Intent intent=new Intent(mContext,SenderModifyActivity.class);
					intent.putExtra("isDeliveryInfoModify", true);
					intent.putExtra("DeliveryInfoItem", mSendInfoItems.get(position));
					startActivityForResult(intent, ActivityResultCode.CODE_SEND_INFO_MODIFY);
				//}
				
			}
		});
		
		
		//获取历史寄件列表
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_LIST_URL+"?sesn="+MyApplicaition.sesn;
		
		Log.e("zyf","获取历史寄件url: "+url);
		
		mSendHistoryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_SEND_LIST, url, null, null);
		mSendHistoryAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mSendHistoryAsyncTaskDataLoader.execute();
		
		return contentView;
	}

	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_SEND_LIST){      //正在获取历史寄件列表
			
			Log.e("zyf","get send list......");
			
			Utils.sendMessage(mHandler, SEND_HISTORY_GETTING);
			
		}else if(flag==Flag.FLAG_SEND_CANCEL){      //正在取消指定的历史寄件
			
			Log.e("zyf","send cancel......");
			
			Utils.sendMessage(mHandler, SEND_CANCELLING);
		}
	}

	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_SEND_LIST){      //获取历史寄件接口的返回结果
			
			Log.e("zyf","send list: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){       //历史寄件获取成功
					
					//JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					JSONObject dataJsonObject=totalJsonObject.optJSONObject("data");
					if(dataJsonObject==null){
						Utils.sendMessage(mHandler, SEND_HISTORY_GET_SUCCESS);
						
						return;
					}
					
					JSONArray deliveryInfoJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					SendInfoItem sendInfoItem;
					AddressItem senderAddressItem,recipientAddressItem;
					mSendInfoItems.clear();
				    for(int i=0;i<deliveryInfoJsonArray.length();i++){
				    	
				    	jsonObject=deliveryInfoJsonArray.getJSONObject(i);
				    	
				    	sendInfoItem=new SendInfoItem();
				    	senderAddressItem=sendInfoItem.getSenderAddressItem();
				    	recipientAddressItem=sendInfoItem.getRecipientAddressItem();
				    	
				    	sendInfoItem.setId(jsonObject.getString("id"));
				    	
				    	senderAddressItem.setName(jsonObject.getString("s_nam"));
				    	senderAddressItem.setTelp(jsonObject.getString("s_tel"));
				    	senderAddressItem.setpId(jsonObject.getString("s_pid"));
				    	senderAddressItem.setcId(jsonObject.getString("s_cid"));
				    	senderAddressItem.setdId(jsonObject.getString("s_did"));
				    	senderAddressItem.setpName(jsonObject.getString("s_pname"));
				    	senderAddressItem.setcName(jsonObject.getString("s_cname"));
				    	senderAddressItem.setdName(jsonObject.getString("s_dname"));
				    	senderAddressItem.setAddress(jsonObject.getString("s_adr"));
				    	senderAddressItem.setZcode(jsonObject.getString("s_zcod"));
				    	
				    	recipientAddressItem.setName(jsonObject.getString("r_nam"));
				    	recipientAddressItem.setTelp(jsonObject.getString("r_tel"));
				    	recipientAddressItem.setpId(jsonObject.getString("r_pid"));
				    	recipientAddressItem.setcId(jsonObject.getString("r_cid"));
				    	recipientAddressItem.setdId(jsonObject.getString("r_did"));
				    	recipientAddressItem.setpName(jsonObject.getString("r_pname"));
				    	recipientAddressItem.setcName(jsonObject.getString("r_cname"));
				    	recipientAddressItem.setdName(jsonObject.getString("r_dname"));
				    	recipientAddressItem.setAddress(jsonObject.getString("r_adr"));
				    	recipientAddressItem.setZcode(jsonObject.getString("r_zcod"));
				    	
				    	sendInfoItem.setSize(jsonObject.getString("size"));
				    	sendInfoItem.setWeight(Integer.parseInt(jsonObject.getString("weight"))/1000.0+"");
				    	sendInfoItem.setGtype(jsonObject.getString("gtype"));
				    	sendInfoItem.setComet(jsonObject.getString("comet"));
				    	sendInfoItem.setCid(jsonObject.getString("cid"));
				    	sendInfoItem.setWid(jsonObject.getString("wid"));
				    	sendInfoItem.setQjtm(jsonObject.getString("qjtm"));
				    	sendInfoItem.setGnam(jsonObject.getString("gnam"));
				    	
				    	sendInfoItem.setStat(jsonObject.getInt("stat"));
				    	sendInfoItem.setLogo(UrlConfigs.SERVER_URL+jsonObject.getString("logo"));
				    	sendInfoItem.setSnum(jsonObject.getString("snum"));
				    	sendInfoItem.setCname(jsonObject.getString("cname"));
				    	
				    	mSendInfoItems.add(sendInfoItem);
				    }
				    
				    Utils.sendMessage(mHandler, SEND_HISTORY_GET_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, SEND_HISTORY_GET_FAILED);       //历史寄件获取失败
			
		}else if(flag==Flag.FLAG_SEND_CANCEL){      //取消指定寄件返回结果
			
			Log.e("zyf","send cancel result: "+result);
			
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){      //指定寄件取消成功
					
					Utils.sendMessage(mHandler, SEND_CANCEL_SUCCESS);
					
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
			
			Utils.sendMessage(mHandler, SEND_CANCEL_FAILED);       //指定寄件取消失败
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_SEND_INFO_MODIFY){      //寄件编辑成功后返回该页面，重新刷新该页面
			
			if(data!=null){
				
				boolean isModified=data.getBooleanExtra("isModified", false);
				
				if(isModified){
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_LIST_URL+"?sesn="+MyApplicaition.sesn;
					mSendHistoryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_SEND_LIST, url, null, null);
					mSendHistoryAsyncTaskDataLoader.setOnDataLoaderListener(this);
					mSendHistoryAsyncTaskDataLoader.execute();
				}
			}
		}else if(requestCode==ActivityResultCode.CODE_LOGIN){      //用户重新登录成功，重新刷新页面信息
			
			if(data!=null){
				
				if(data.getBooleanExtra("loginStatus", false)){
					
					Log.e("zyf","login status : true");
					
					if(mode==MODE_GET_SEND_HISTORY){
						String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_LIST_URL+"?sesn="+MyApplicaition.sesn;
						mSendHistoryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_SEND_LIST, url, null, null);
						mSendHistoryAsyncTaskDataLoader.setOnDataLoaderListener(this);
						mSendHistoryAsyncTaskDataLoader.execute();
					}
				}
			}
		}
	}
    
	//“历史寄件”Listview的适配器
    public class MyDeletedListViewBaseAdapter extends BaseAdapter {

    	private Context mContext;

    	public MyDeletedListViewBaseAdapter(Context mContext) {
    		this.mContext = mContext;
    	}

    	public int getCount() {

    		return mSendInfoItems.size();
    	}

    	public Object getItem(int pos) {
    		return mSendInfoItems.get(pos);
    	}

    	public long getItemId(int pos) {
    		return pos;
    	}

    	public View getView(final int pos, View convertView, ViewGroup p) {
    		
    		final SendInfoItem item=mSendInfoItems.get(pos);

    		if(convertView == null) {
    			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_del_listview_send_history, null);
    		}
    		
    		MyLoaderImageView mLoaderImageView=(MyLoaderImageView)convertView.findViewById(R.id.mLoaderImageView);
    		
    		TextView mCNameView=(TextView)convertView.findViewById(R.id.mCNameView);
    		TextView mSnumView=(TextView)convertView.findViewById(R.id.mSnumView);
    		TextView mStatusView=(TextView)convertView.findViewById(R.id.mStatusView);
    		TextView mOrderTimeView=(TextView)convertView.findViewById(R.id.mOrderTimeView);
    		
    		LinearLayout mRightContainer=(LinearLayout)convertView.findViewById(R.id.mRightContainer);
    		mRightContainer.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					MyDeleteDialog mDialog=new MyDeleteDialog(getActivity(), R.style.MyDialog);
					mDialog.setMySubmmitListener(new MyDeleteDialog.MySubmmitListener() {
						
						@Override
						public void delete() {      //历史寄件的侧滑删除功能
							
							Log.e("zyf","delete delete postion: "+pos);
							
		                    cancelledPositon=pos;
							
							String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_CANCEL_URL+"?sesn="+MyApplicaition.sesn+"&id="+item.getId();
							mSendDeleteAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_SEND_CANCEL, url, null, null);
							mSendDeleteAsyncTaskDataLoader.setOnDataLoaderListener(SenderHistoryFragment.this);
							mSendDeleteAsyncTaskDataLoader.execute();
						}
					});
					mDialog.show();
					
				}
			});
    		
    		mLoaderImageView.setURL(item.getLogo());
    		
    		mCNameView.setText(item.getCname());
    		mSnumView.setText(item.getSnum());
    		
    		
    		mStatusView.setText(statusTexts[item.getStat()]);
    		
    		mOrderTimeView.setVisibility(View.GONE);
    		
    		return convertView;
    	}
    }

    //“历史寄件”下拉刷新功能
	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		
		Log.e("zyf","ScrollerView refresh refresh refresh......");
		
		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_SEND_LIST_URL+"?sesn="+MyApplicaition.sesn;
		
		Log.e("zyf","获取历史寄件url: "+url);
		
		mSendHistoryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_SEND_LIST, url, null, null);
		mSendHistoryAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mSendHistoryAsyncTaskDataLoader.execute();
	}
}
