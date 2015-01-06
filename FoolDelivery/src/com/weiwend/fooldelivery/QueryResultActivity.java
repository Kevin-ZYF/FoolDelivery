package com.weiwend.fooldelivery;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyAddRemarkDialog;
import com.weiwend.fooldelivery.customviews.MyAddRemarkDialog.MySubmmitListener;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.DeliveryQueryHistoryItem;
import com.weiwend.fooldelivery.items.DeliveryQueryResultInfoItem;
import com.weiwend.fooldelivery.items.DeliveryQueryResultItem;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.ParityCompanyItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.CacheHandler;
import com.weiwend.fooldelivery.utils.FileUtils;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class QueryResultActivity extends BaseActivity implements onDataLoaderListener,OnClickListener,MySubmmitListener{
	
	//显示查询详情的Listview
	private PullToRefreshListView mPullToRefreshListView;
	
	//“订单编号”、“下单时间”，“添加备注”的外层Container
	private LinearLayout mTitleInfoContainer;
	
	//“收藏”，“分享”Container
	private LinearLayout mCollectContainer,mShareContainer;
	
	//标题栏左侧“更多”按钮
	private Button moreBtn;
	
	//单号、下单时间、添加备注
	private TextView mSumTv,mOrderTimeTv,mAddRemarkTv;
	
	//“添加备注”图标
	private Button mAddRemarkBtn;
	
	//调用查询接口后的各种状态信息
	private final int DELIVERY_QUERYING=0;
	private final int DELIVERY_QUERY_SUCCESS=1;
	private final int DELIVERY_QUERY_FAILED=2;
	
	//调用添加备注后的各种状态信息
	private final int DELIVERY_ADD_REMARKING=3;
	private final int DELIVERY_ADD_REMARK_SUCCESS=4;
	private final int DELIVERY_ADD_REMARK_FAILED=5;
	
	//调用收藏接口后的各种状态信息
	private final int DELIVERY_COLLECTING=6;
	private final int DELIVERY_COLLECT_SUCCESS=7;
	private final int DELIVERY_COLLECT_FAILED=8;
	
	//调用取消收藏接口后的各种状态信息
	private final int DELIVERY_CANCEL_COLLECTING=9;
	private final int DELIVERY_CANCEL_COLLECT_SUCCESS=10;
	private final int DELIVERY_CANCEL_COLLECT_FAILED=11;
	
	//执行分享前，调用接口获取app最新下载地址后的各种状态信息
	private final int APP_DOWNLOAD_URL_GETTING=12;
	private final int APP_DOWNLOAD_URL_GET_SUCCESS=13;
	private final int APP_DOWNLOAD_URL_GET_FAILED=14;
	
	//查询详情Listview的适配器
	private MyBaseAdapter mBaseAdapter;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//保存本地查询历史记录
	private ArrayList<DeliveryQueryHistoryItem> mDeliveryQueryHistoryItems;
	
	//保存单号详情
	private ArrayList<DeliveryQueryResultInfoItem> deliveryQueryResultInfoItems=new ArrayList<DeliveryQueryResultInfoItem>();
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case DELIVERY_QUERYING:   //查询接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","delivery querying......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_querying, QueryResultActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_QUERY_SUCCESS:   //快递详情查询成功
				
				Log.e("zyf","delivery list get success......");
				
				if(remark!=null&&remark.length()>0){
					mAddRemarkTv.setText(remark);
				}
				
				Utils.saveShareImage(QueryResultActivity.this);
				
				mTitleInfoContainer.setVisibility(View.VISIBLE);
				mSumTv.setText(getString(R.string.snum)+snum);
				
				String orderTime=deliveryQueryResultInfoItems.get(deliveryQueryResultInfoItems.size()-1).getDate();
				mOrderTimeTv.setText(getString(R.string.order_time)+orderTime);
				
				//更新本地查询历史
				DeliveryQueryHistoryItem newItem=new DeliveryQueryHistoryItem();
				newItem.setSnum(snum);
				newItem.setLogo(logo);
				newItem.setStat(stat);
				newItem.setCid(cid);
				newItem.setName(name);
				newItem.setStm(orderTime);
				newItem.setRemark(remark);
				if(offical==0){   //未收藏
					newItem.setOffical(false);
				}else{
					newItem.setOffical(true);  //已收藏
				}
				
				if(mDeliveryQueryHistoryItems==null){
					mDeliveryQueryHistoryItems=FileUtils.readDeliveryQueryHistory();  //读取本地的历史查询记录
				}
				DeliveryQueryHistoryItem item;
				int index=-1;
				for(int i=0;i<mDeliveryQueryHistoryItems.size();i++){
					item=mDeliveryQueryHistoryItems.get(i);
					if(item.getSnum().equals(snum)){
						
						index=i;
						
						break;
					}
				}
				
				if(index!=-1){   //在本地历史记录中发现该单号信息，执行更新操作
					
					Log.e("zyf","find snum in local, remove it......");
					
					mDeliveryQueryHistoryItems.remove(index);
					
					mDeliveryQueryHistoryItems.add(index,newItem);
					
				}else{      //在本地历史记录中没有发现该单号信息，执行添加操作
					
					Log.e("zyf","not not not find snum in local, remove it,add it......");
					
					mDeliveryQueryHistoryItems.add(newItem);
				}
				FileUtils.updateDeliveryQueryHistory(mDeliveryQueryHistoryItems);    //更新本地的历史查询记录
				
				
				//生成待分享内容的后半部分，app的下载链接后续调用接口获取
				shareContent=getString(R.string.snum2)+snum+"\n";
				for(int i=0;i<deliveryQueryResultInfoItems.size();i++){   //查询详情保存
					
					DeliveryQueryResultInfoItem deliveryQueryResultInfoItem=deliveryQueryResultInfoItems.get(i);
					
					ArrayList<DeliveryQueryResultItem> deliveryQueryResultItems=deliveryQueryResultInfoItem.getDeliveryQueryResultItems();
					
					shareContent+=deliveryQueryResultInfoItem.getDate()+"\n";
					
					for(int j=0;j<deliveryQueryResultItems.size();j++){
						shareContent+=deliveryQueryResultItems.get(j).getTime()+"  "+deliveryQueryResultItems.get(j).getContent()+"\n";
					}
				}
				
				if(mBaseAdapter==null){
					mBaseAdapter=new MyBaseAdapter(QueryResultActivity.this);
					
					mPullToRefreshListView.setAdapter(mBaseAdapter);
				}else{
					mBaseAdapter.notifyDataSetChanged();
				}
				
				mPullToRefreshListView.onRefreshComplete();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_query_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case DELIVERY_QUERY_FAILED:      //快递详情查询失败
				
				Log.e("zyf","delivery list get failed......");
				
				//更新本地查询历史  如果是第一次是第一次查询该单号，则保存相应的信息，如果该单号信息已保存在本地，则不进行任何操作
				DeliveryQueryHistoryItem failedItem=new DeliveryQueryHistoryItem();
				failedItem.setSnum(snum);
				failedItem.setLogo(logo);
				failedItem.setName(name);
				failedItem.setCid(cid);
				if(mDeliveryQueryHistoryItems==null){
					mDeliveryQueryHistoryItems=FileUtils.readDeliveryQueryHistory();    //读取本地的历史查询记录
				}
				DeliveryQueryHistoryItem item2;
				int index2=-1;
				for(int i=0;i<mDeliveryQueryHistoryItems.size();i++){
					item2=mDeliveryQueryHistoryItems.get(i);
					if(item2.getSnum().equals(snum)){
						
						index2=i;
						
						break;
					}
				}
				
				if(index2!=-1){   //本地记录中已保存该单号信息，不进行任何操作
					
					Log.e("zyf","find snum in local, as query failed ,so do not any options......");
					
				}else{     //本地记录中没有该单号信息，保存该记录至本地
					
					Log.e("zyf","not not not find snum in local, remove it,add it......");
					
					mDeliveryQueryHistoryItems.add(failedItem);
					
					FileUtils.updateDeliveryQueryHistory(mDeliveryQueryHistoryItems);    //更新本地的历史查询记录
				}
				
				
				mPullToRefreshListView.onRefreshComplete();
				
                MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_query_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				//跳转到查询失败界面
				Intent intent=new Intent(QueryResultActivity.this,QueryFailedActivity.class);
				startActivity(intent);
				
				finish();
				
				break;
			case DELIVERY_ADD_REMARKING:   //添加备注的接口已经调用，正在等待服务器端的回复
				
				//mode=MODE_ADD_REMARKING;
				
                Log.e("zyf","delivery add remarking......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_result_add_remark, QueryResultActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_ADD_REMARK_SUCCESS:   //添加备注成功
				
				Log.e("zyf","delivery add remark success......");
				
				mode=-1;
				
				if(remark!=null&&remark.length()>0){
					mAddRemarkTv.setText(remark);
				}else{
					mAddRemarkTv.setText(getResources().getString(R.string.add_remark));
				}
				
				//更新本地备注信息
				if(mDeliveryQueryHistoryItems==null){
					mDeliveryQueryHistoryItems=FileUtils.readDeliveryQueryHistory();    //读取本地的历史查询记录
				}
				for(int i=0;i<mDeliveryQueryHistoryItems.size();i++){
        			DeliveryQueryHistoryItem historyItem=mDeliveryQueryHistoryItems.get(i);
        			if(historyItem.getSnum().equals(snum)){
        				historyItem.setRemark(remark);
        			}
        		}
                FileUtils.updateDeliveryQueryHistory(mDeliveryQueryHistoryItems);    //更新本地的历史查询记录
				
				mAddRemarkDialog.dismiss();
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_result_add_remark_success), MyToast.LENGTH_SHORT).show();
				
				break;
			case DELIVERY_ADD_REMARK_FAILED:    //添加备注信息失败
				
				Log.e("zyf","delivery add remark failed......");
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_result_add_remark_failed), MyToast.LENGTH_SHORT).show();
				
				break;
			case DELIVERY_COLLECTING:     //收藏接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","delivery collecting......");
					
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_collect, QueryResultActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_COLLECT_SUCCESS:   //收藏成功
				
                Log.e("zyf","delivery collect success......");
                
                //更新本地收藏信息
				if(mDeliveryQueryHistoryItems==null){
					mDeliveryQueryHistoryItems=FileUtils.readDeliveryQueryHistory();    //读取本地的历史查询记录
				}
				for(int i=0;i<mDeliveryQueryHistoryItems.size();i++){
        			DeliveryQueryHistoryItem historyItem=mDeliveryQueryHistoryItems.get(i);
        			if(historyItem.getSnum().equals(snum)){
        				historyItem.setOffical(true);
        			}
        		}
                FileUtils.updateDeliveryQueryHistory(mDeliveryQueryHistoryItems);    //更新本地的历史查询记录
                
                offical=1;   //记录已收藏
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_collect_success), MyToast.LENGTH_SHORT).show();
				
				break;
			case DELIVERY_COLLECT_FAILED:    //收藏失败
				
                Log.e("zyf","delivery collect failed......");
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_collect_failed), MyToast.LENGTH_SHORT).show();
				
				break;
			case DELIVERY_CANCEL_COLLECTING:    //取消收藏的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","delivery cancel collecting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_delivery_cancel_collect, QueryResultActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case DELIVERY_CANCEL_COLLECT_SUCCESS:   //取消收藏成功
				
				Log.e("zyf","delivery cancel collect success......");
				
				//更新本地收藏信息
				if(mDeliveryQueryHistoryItems==null){
					mDeliveryQueryHistoryItems=FileUtils.readDeliveryQueryHistory();    //读取本地的历史查询记录
				}
				for(int i=0;i<mDeliveryQueryHistoryItems.size();i++){
        			DeliveryQueryHistoryItem historyItem=mDeliveryQueryHistoryItems.get(i);
        			if(historyItem.getSnum().equals(snum)){
        				historyItem.setOffical(false);
        			}
        		}
                FileUtils.updateDeliveryQueryHistory(mDeliveryQueryHistoryItems);    //更新本地的历史查询记录
				
				offical=0;    //记录未收藏

				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_cancel_collect_success), MyToast.LENGTH_SHORT).show();
				break;
			case DELIVERY_CANCEL_COLLECT_FAILED:    //取消收藏失败
				
				Log.e("zyf","delivery cancel collect success......");

				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_cancel_collect_failed), MyToast.LENGTH_SHORT).show();
				break;
			case APP_DOWNLOAD_URL_GETTING:   //获取app最新下载地址的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","share initing......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_share_int, QueryResultActivity.this);
				
				mProgressDialogUtils.showDialog();
				
				break;
			case APP_DOWNLOAD_URL_GET_SUCCESS:    //app最新下载地址获取成功
				
                mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(QueryResultActivity.this, getString(R.string.share_init_success), MyToast.LENGTH_SHORT).show();
				
				//初始化一键分享的界面
				ShareSDK.initSDK(QueryResultActivity.this);
		        OnekeyShare oks = new OnekeyShare();
		        oks.setText(totalContent);
		        oks.setTitle(getString(R.string.share_title)); 
		        //设置用于分享过程中，根据不同平台自定义分享内容的回调
		        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
					
					@Override
					public void onShare(Platform platform, ShareParams paramsToShare) {
						
						//处理分享至微信朋友圈时，若不设置image path,则会提示获取资源失败
						if(platform.getName().equals(WechatMoments.NAME)){
							
							Log.e("zyf","wechat moment moment moment......");
							
							paramsToShare.setImagePath(CacheHandler.getShareCacheDir()+"/"+DataConfigs.SHARE_IMAGE_FILE_NAME);
						}
					}
				});
		        oks.setDialogMode();
		        oks.show(QueryResultActivity.this);
							
				break;
				
			case APP_DOWNLOAD_URL_GET_FAILED:    //app最新下载地址获取失败，不执行分享操作
				
				mProgressDialogUtils.dismissDialog();
					
				MyToast.makeText(QueryResultActivity.this, getString(R.string.share_init_failed), MyToast.LENGTH_SHORT).show();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//查询单号详情的异步加载类
	private AsyncTaskDataLoader mDeliveryQueryAsyncTaskDataLoader;
	
	//添加备注的异步加载类
	private AsyncTaskDataLoader mAddRemarkAsyncTaskDataLoader;
	
	//收藏单号的异步加载类
	private AsyncTaskDataLoader mCollectAsyncTaskDataLoader;
	
	//取消收藏指定单号的异步加载类
	private AsyncTaskDataLoader mCancelCollectAsyncTaskDataLoader;
	
	//查询记录的id
	private String id="";
	
	//快递公司的id,图标，单号，快递公司名称，单号备注信息，单号是否已完成，单号的状态
	private String cid,logo,snum,name,remark,finished,stat;
	
	private int offical;  //标记单号是否已经收藏  0:无收藏  1:有收藏
	
	//获取单号详情时，用于暂时保存“items”字段的值，为收藏时上传该值做准备
	private String items;
	
	//用于用户重新登录成功后,重新刷新单号详情
	private int mode=-1;
	//private int MODE_ADD_REMARKING=0;
	
	private String shareContent;   //用于分享的后半部分内容
	private String totalContent;   //用于分享的部分内容

	//private JSONArray deliveryInfoItemsJsonArray;
	
    private MyAddRemarkDialog mAddRemarkDialog;   //添加备注对话框
    
    private PopupWindow mMorePopupWindow;   //“收藏”、“分享”窗口

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_query_result);
		
		id=getIntent().getStringExtra("id");
		
		cid=getIntent().getStringExtra("cid");
		logo=getIntent().getStringExtra("logo");
		snum=getIntent().getStringExtra("snum");
		name=getIntent().getStringExtra("name");
		remark=getIntent().getStringExtra("rmrk");
		
		initViews();
		
		String queryUrl;
		
		if(id!=null&&id.length()>0){    //此种情况，由于需求的变化暂时无法出现
			queryUrl=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_DETAIL_URL+"?sesn="+MyApplicaition.sesn+"&id="+id; 	
		}else{	
			queryUrl=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_QUERY_URL+"?sesn="+MyApplicaition.sesn+"&cid="+cid+"&snum="+snum;
			
			Log.e("zyf","delivery query url: "+queryUrl);
		}
		
		//查询单号详情
		mDeliveryQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_QUERY, queryUrl, null, null);
		mDeliveryQueryAsyncTaskDataLoader.setOnDataLoaderListener(this);
		mDeliveryQueryAsyncTaskDataLoader.execute();
	}
	
	private void initViews(){
		mPullToRefreshListView=(PullToRefreshListView)findViewById(R.id.mPullToRefreshListView);
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				String label = DateUtils.formatDateTime(QueryResultActivity.this, System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				if(refreshView.getCurrentMode()==Mode.PULL_FROM_START){ //下拉刷新
					
					String	queryUrl=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_QUERY_URL+"?sesn="+MyApplicaition.sesn+"&cid="+cid+"&snum="+snum;

					mDeliveryQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_QUERY, queryUrl, null, null);
					mDeliveryQueryAsyncTaskDataLoader.setOnDataLoaderListener(QueryResultActivity.this);
					mDeliveryQueryAsyncTaskDataLoader.execute();
					
				}else{
					
				}
				
			}
		});
		
		mTitleInfoContainer=(LinearLayout)findViewById(R.id.mTitleInfoContainer);
		
		mSumTv=(TextView)findViewById(R.id.mSumTv);
		mOrderTimeTv=(TextView)findViewById(R.id.mOrderTimeTv);
		mAddRemarkTv=(TextView)findViewById(R.id.mAddRemarkTv);
		
		if(remark!=null&&remark.length()>0){
			mAddRemarkTv.setText(remark);
		}else{
			mAddRemarkTv.setText(getResources().getString(R.string.add_remark));
		}
		
		mAddRemarkBtn=(Button)findViewById(R.id.mAddRemarkBtn);
		
		mAddRemarkTv.setOnClickListener(this);
		mAddRemarkBtn.setOnClickListener(this);
		
		mTitleInfoContainer.setVisibility(View.GONE);
	}

	//初始化页面的标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		moreBtn=(Button)findViewById(R.id.moreBtn);
		moreBtn.setVisibility(View.VISIBLE);
		moreBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_delivery_query_result));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_DELIVERY_QUERY){   //开始查询单号详情
			
			Utils.sendMessage(mHandler, DELIVERY_QUERYING);
			
		}else if(flag==Flag.FLAG_QUERY_RESULT_ADD_REMARK){    //开始添加备注信息
			
			Utils.sendMessage(mHandler, DELIVERY_ADD_REMARKING);
			
		}else if(flag==Flag.FLAG_DELIVERY_COLLECT){    //开始收藏
			
			Utils.sendMessage(mHandler, DELIVERY_COLLECTING);
			
		}else if(flag==Flag.FLAG_DELIVERY_CANCEL_COLLECT){    //开始取消收藏
			
			Utils.sendMessage(mHandler, DELIVERY_CANCEL_COLLECTING);
			
		}else if(flag==Flag.FLAG_CHECK_VERSION){    //开始获取app的最新下载链接
			
			Utils.sendMessage(mHandler, APP_DOWNLOAD_URL_GETTING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		Log.e("zyf","result: "+result);
		
		if(flag==Flag.FLAG_DELIVERY_QUERY){    //获取单号详情返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //单号详情获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray deliveryInfoItemsJsonArray=dataJsonObject.getJSONArray("items");
					
					items=dataJsonObject.getString("items");
					
					snum=dataJsonObject.getString("snum");
					stat=dataJsonObject.getString("stat");
					offical=dataJsonObject.getInt("offical");
					finished=dataJsonObject.getString("finished");
					logo=dataJsonObject.getString("logo");
					
					if(offical==1){  //已收藏，更新本地备注信息
						
						remark=dataJsonObject.getString("rmrk");
					}
					
					if(dataJsonObject.has("id")){
						id=dataJsonObject.getString("id");
						Log.e("zyf","need update id: "+id);
					}else{
						Log.e("zyf","need not update id: "+id);
					}
					
					JSONObject jsonObject;
					
					deliveryQueryResultInfoItems.clear();
					
					DeliveryQueryResultInfoItem resultInfoItem=null;
					
					String totalTime;
					String date;
					String time;
					
					int lastIndex=0; //用来记录新list中上次添加项的index
					
					//默认添加第一项
					jsonObject=deliveryInfoItemsJsonArray.getJSONObject(0);
					totalTime=jsonObject.getString("time");
			    	date=totalTime.split(" ")[0];
			    	time=totalTime.split(" ")[1];
			    	
			    	resultInfoItem=new DeliveryQueryResultInfoItem();
		    		resultInfoItem.setDate(date);
		    		
		    		//添加子项
		    	    DeliveryQueryResultItem timeItem=new DeliveryQueryResultItem();
		    	    timeItem.setContent(jsonObject.getString("cont"));
		    	    timeItem.setTime(time);
		    	    
		    	    resultInfoItem.getDeliveryQueryResultItems().add(timeItem);
		    	    
		    	    deliveryQueryResultInfoItems.add(resultInfoItem);
					
                    for(int i=1;i<deliveryInfoItemsJsonArray.length();i++){
				    	
				    	jsonObject=deliveryInfoItemsJsonArray.getJSONObject(i);
				    	
				    	totalTime=jsonObject.getString("time");
				    	date=totalTime.split(" ")[0];
				    	time=totalTime.split(" ")[1];
				    	
				    	if(!date.equals(deliveryQueryResultInfoItems.get(lastIndex).getDate())){   //当前日期不相等
				    		
				    		resultInfoItem=new DeliveryQueryResultInfoItem();
				    		resultInfoItem.setDate(date);
				    		
				    		//添加子项
				    	    timeItem=new DeliveryQueryResultItem();
				    	    timeItem.setContent(jsonObject.getString("cont"));
				    	    timeItem.setTime(time);
				    	    
				    	    resultInfoItem.getDeliveryQueryResultItems().add(timeItem);

				    	    deliveryQueryResultInfoItems.add(resultInfoItem);
				    	    
				    	    lastIndex++;
				    	}else{    //当前日期相等,直接添加子项
				    		
				    	    timeItem=new DeliveryQueryResultItem();
				    	    timeItem.setContent(jsonObject.getString("cont"));
				    	    timeItem.setTime(time);
				    	    
				    	    deliveryQueryResultInfoItems.get(lastIndex).getDeliveryQueryResultItems().add(timeItem);
				    	}
				    	
				    }
					//>end by Yongfeng.zhang
				    
				    Utils.sendMessage(mHandler, DELIVERY_QUERY_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_QUERY_FAILED);  //单号详情获取失败
			
		}else if(flag==Flag.FLAG_QUERY_RESULT_ADD_REMARK){    //获取备注添加返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //备注添加成功
					
					remark=tempRemark;
				    
				    Utils.sendMessage(mHandler, DELIVERY_ADD_REMARK_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_ADD_REMARK_FAILED);     //备注添加失败
			
		}else if(flag==Flag.FLAG_DELIVERY_COLLECT){    //获取收藏返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //收藏成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					id=dataJsonObject.getString("id");
					
					Log.e("zyf","id id id :"+id);
				    
				    Utils.sendMessage(mHandler, DELIVERY_COLLECT_SUCCESS);
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_COLLECT_FAILED);    //收藏失败
			
		}else if(flag==Flag.FLAG_DELIVERY_CANCEL_COLLECT){  ////获取取消收藏返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //取消收藏成功
				    
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
			
			Utils.sendMessage(mHandler, DELIVERY_CANCEL_COLLECT_FAILED);    //取消收藏失败
			
		}else if(flag==Flag.FLAG_CHECK_VERSION){    //获取app最新下载链接的返回结果
			
			Log.e("zyf","version check result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //app最新下载链接获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					//生成完整的分享内容
					String preContent=getString(R.string.share_propmt)+"\n";
					preContent+=getString(R.string.download_url)+dataJsonObject.getString("path")+"\n";
					totalContent=preContent+shareContent;
							
				    Utils.sendMessage(mHandler, APP_DOWNLOAD_URL_GET_SUCCESS);
					
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
			
			Utils.sendMessage(mHandler, APP_DOWNLOAD_URL_GET_FAILED);    //app最新下载链接获取失败
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_LOGIN){
			
			if(data!=null){
				
				if(mode==-1&&data.getBooleanExtra("loginStatus", false)){  //用户重新登录成功，刷新单号的详情
					
					Log.e("zyf","login status : true");
					
					String queryUrl=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_QUERY_URL+"?sesn="+MyApplicaition.sesn+"&cid="+cid+"&snum="+snum;

					mDeliveryQueryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_QUERY, queryUrl, null, null);
					mDeliveryQueryAsyncTaskDataLoader.setOnDataLoaderListener(this);
					mDeliveryQueryAsyncTaskDataLoader.execute();
				}
			}
		}
	}
	
	//单号详情Listview的适配器
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
			return deliveryQueryResultInfoItems.size();
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
			
				convertView=mInflater.inflate(R.layout.item_query_result_new, null);
			}
			
			DeliveryQueryResultInfoItem item;
			
			
			item=deliveryQueryResultInfoItems.get(position);
			
			//显示日期
			TextView mDateView=(TextView)convertView.findViewById(R.id.mDateView);
			mDateView.setText(item.getDate());
			
			LinearLayout mTimeContainer=(LinearLayout)convertView.findViewById(R.id.mTimeContainer);
			mTimeContainer.removeAllViews();
			
			//显示单个日期下的具体时间轴
			for(int i=0;i<item.getDeliveryQueryResultItems().size();i++){
				
				LinearLayout container=(LinearLayout)mInflater.inflate(R.layout.item_query_result_time_cont, null);
				
				TextView mTimeView=(TextView)container.findViewById(R.id.mTimeView);
				TextView mContentView=(TextView)container.findViewById(R.id.mContentView);
				
				if(position==0&&i==0){   //单号的最新状态信息设为蓝色
					mContentView.setTextColor(Color.parseColor("#73bad6"));
				}else{
					mContentView.setTextColor(Color.BLACK);
				}
				
				//具体的时间
				mTimeView.setText(item.getDeliveryQueryResultItems().get(i).getTime());
				
				//状态信息
				mContentView.setText(item.getDeliveryQueryResultItems().get(i).getContent());
				
				mTimeContainer.addView(container);
			}
			
			return convertView;
		}	
	}

    @Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.mAddRemarkTv:    //添加备注
			
		case R.id.mAddRemarkBtn:  //添加备注
			
			mAddRemarkDialog=new MyAddRemarkDialog(QueryResultActivity.this, R.style.MyDialog,remark);
			mAddRemarkDialog.setMySubmmitListener(this);
			mAddRemarkDialog.show();
			
			break;
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.moreBtn:  //初始化“收藏”、“分享”窗口
			
			View contentView=getLayoutInflater().inflate(R.layout.popup_window_more, null);
			mMorePopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
			mMorePopupWindow.setBackgroundDrawable(new PaintDrawable());
			mMorePopupWindow.setFocusable(true);
			
			TextView mCollectTv=(TextView)contentView.findViewById(R.id.mCollectTv);
			
			if(offical==0){  //单号尚未收藏
				mCollectTv.setText(getString(R.string.collect));
			}else{    //单号已经收藏
				mCollectTv.setText(getString(R.string.collect_cancel));
			}
			
			mCollectContainer=(LinearLayout)contentView.findViewById(R.id.mCollectContainer);
			mShareContainer=(LinearLayout)contentView.findViewById(R.id.mShareContainer);
			
			mCollectContainer.setOnClickListener(this);
			mShareContainer.setOnClickListener(this);
			
			mMorePopupWindow.showAsDropDown(moreBtn, 0, Utils.dip2px(this, 5));
			
			
			break;
		case R.id.mCollectContainer:   //收藏
			
			Log.e("zyf","collect......");
			
			mMorePopupWindow.dismiss();
			
			if(MyApplicaition.sesn.length()==0){  //只有登录的用户，才可以确定改单号是否已收藏
				
				Intent intent=new Intent(this, UserLoginActivity.class);
				startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				
			}else{

				if(offical==0){   //尚未收藏该单号，执行收藏操作
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COLLECT_URL;
					String content=formatCollectContent();
					mCollectAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_DELIVERY_COLLECT, url, null, content);
					mCollectAsyncTaskDataLoader.setOnDataLoaderListener(this);
					mCollectAsyncTaskDataLoader.execute();
					
				}else{    //该单号已经收藏，执行取消收藏
					
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_CANCEL_COLLECT_URL
							+"?sesn="+MyApplicaition.sesn
							+"&id="+id;
					mCancelCollectAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_DELIVERY_CANCEL_COLLECT, url, null, null);
					mCancelCollectAsyncTaskDataLoader.setOnDataLoaderListener(this);
					mCancelCollectAsyncTaskDataLoader.execute();
				}
			}
			
			break;
			
		case R.id.mShareContainer:    //分享
			
			Log.e("zyf","share......");
			
			mMorePopupWindow.dismiss();
			
			//获取app的最新下载链接，为生成分享内容作准备
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CHECK_VERSION_URL+"?version="+Utils.getVersion(this);
			AsyncTaskDataLoader mCheckVersionAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_CHECK_VERSION, url, null, null);
			mCheckVersionAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mCheckVersionAsyncTaskDataLoader.execute();
			
			break;
			
		default:
			break;
		}
	}
    
    //格式化收藏接口所要上传的Json数据
    private String formatCollectContent(){
    	
    	if(remark==null)
    		remark="";
		
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
				   +"\"cid\":"+"\""+cid+"\""+","
	               +"\"snum\":"+"\""+snum+"\""+","
	               +"\"rmrk\":"+"\""+remark+"\""+","
	               +"\"items\":"+items+","
				   +"\"finished\":"+"\""+finished+"\""+"}";

		return str;
	}
    
    //用来临时保存用户想要添加的备注信息
    private String tempRemark;

	@Override
	public void summit(String content) {
		
		mAddRemarkDialog.dismiss();
		
		Log.e("zyf","content: "+content);
		
		if(MyApplicaition.sesn.length()>0&&offical==1){   //用户已经登录，并且收藏了该单号，则备注到云端
			
			tempRemark=content+"";
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_QUERY_RESULT_ADD_REMARK_URL;
			content=formatAddRemarkContent();
			
			mAddRemarkAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_QUERY_RESULT_ADD_REMARK, url, null, content);
			mAddRemarkAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mAddRemarkAsyncTaskDataLoader.execute();
			
		}else{    //直接备注到本地
			
			Utils.sendMessage(mHandler, DELIVERY_ADD_REMARKING);
			
			remark=content;
			
			Utils.sendMessage(mHandler, DELIVERY_ADD_REMARK_SUCCESS);
			
			MyToast.makeText(QueryResultActivity.this, getString(R.string.delivery_result_add_remark_success), MyToast.LENGTH_SHORT).show();
		}
	}
	
	//格式化添加备注接口所要上传的json数据
	private String formatAddRemarkContent(){
		
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
	               +"\"id\":"+"\""+id+"\""+","
				   +"\"rmrk\":"+"\""+tempRemark+"\""+"}";
		return str;
	}

}
