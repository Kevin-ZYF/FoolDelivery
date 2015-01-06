package com.weiwend.fooldelivery.customviews;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.utils.Utils;

public class TimeSelectorView extends LinearLayout implements OnClickListener{
	
	//上下文对象
	private Context mContext;
	
	//private TextView mDateTv,mTimeTv;
	
	//“日期”、“时间”选中后底下出现的小箭头
	private ImageView mArrowUpIv1,mArrowUpIv2;
	
	//“日期”、“时间”的外部容器
	private LinearLayout mDateContainer,mTimeContainer;
	
	//显示“日期”的ListView
	private ListView mDateListView;
	
	//显示“时间”的GridView
	private MyGridView mTimeGridView;
	
	//“日期”ListView的适配器
	private MyDateBaseAdapter mDateBaseAdapter;
	
	//“时间”GridView的适配器
	private MyTimeBaseAdapter mTimeBaseAdapter;

	//“取消”按钮
	private Button mCancelBtn;
	
	//保存有效的日期值(中文显示)
	private String dateItems[];
	
	//保存有效的时间值
	private String timeItems[];
	
	//保存有效的日期值
	private String dates[];
	
	private int curSelectedIndex=0;//当前选择的项目,0：date   1：time
	
	//用于标识今天能否预约取件(6:00-18:00之间可以预约取件)
	private int mode;
	private int MODE_TODAY_AVAILABLE=0;
	private int MODE_TODAY_NOT_AVAILABLE=1;
	
	//日期时间选择监听器
	public interface MyOnTimeSelectedClickLister
	{
		void OnDateSelected(String dateShow,String realDate,boolean immediately);
		void OnTimeSelected(String time);
	};
	
	//日期时间选择监听器
	private MyOnTimeSelectedClickLister mOnTimeSelectedClickLister;
	
	//设置日期时间选择监听器
	public void setOnTimeSelectedListener(MyOnTimeSelectedClickLister mOnTimeSelectedClickLister)
	{
		this.mOnTimeSelectedClickLister=mOnTimeSelectedClickLister;
	}
	
	//该控件所在页面的外围ScrollView
	private ScrollView mScrollView;
	
	//设置该控件所在页面的外围ScrollView
	public void setScrollView(ScrollView mScrollView){
		this.mScrollView=mScrollView;
	}

	public TimeSelectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		
		initViews();
	}
	
    private void initViews(){
    	
    	dates=Utils.getFuture4Date();
    	
    	timeItems=getResources().getStringArray(R.array.time_items);
    	
    	View contentView=LayoutInflater.from(mContext).inflate(R.layout.custom_time_selector, this);
    	
    	mDateContainer=(LinearLayout)contentView.findViewById(R.id.mDateContainer);
    	mTimeContainer=(LinearLayout)contentView.findViewById(R.id.mTimeContainer);
    	
    	mArrowUpIv1=(ImageView)contentView.findViewById(R.id.mArrowUpIv1);
    	mArrowUpIv2=(ImageView)contentView.findViewById(R.id.mArrowUpIv2);
    	
    	/*mDateTv=(TextView)contentView.findViewById(R.id.mDateTv);
    	mTimeTv=(TextView)contentView.findViewById(R.id.mTimeTv);*/
    	
    	mDateListView=(ListView)contentView.findViewById(R.id.mDateListView);
    	mDateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				if(mode==MODE_TODAY_AVAILABLE){   //当前时间还可以预约取件，显示日期为“马上寄出”、“今天”、“明天”、“后天”
					
					boolean immediately=false;
					
	                if(position==0){   //选择“马上寄出”
	                	
						cancel();
						immediately=true;
						
					}else if(position==1){  //“日期”选择“今天”，则出现的“时间”应从当前时间至18:00之间的整点
						
						updateViewsStatus(1, true);
						
					}else{    //“日期”选择“明天”或者“后天”，则出现的“时间”应从6:00至18:00之间的整点
						
						updateViewsStatus(1, false);
						
					}
					
					if(mOnTimeSelectedClickLister!=null){   //回调日期时间选择监听器的处理函数
						
						mOnTimeSelectedClickLister.OnDateSelected(dateItems[position],dates[position],immediately);
					}
				}else{    //当前时间不可以预约取件，显示日期为“明天”、“后天”
					
					updateViewsStatus(1, false);
					
					if(mOnTimeSelectedClickLister!=null){    //回调日期时间选择监听器的处理函数
						
						mOnTimeSelectedClickLister.OnDateSelected(dateItems[position],dates[position+2],false);
					}
					
				}
			}
		});
    	
    	mTimeGridView=(MyGridView)contentView.findViewById(R.id.mTimeGridView);
    	mTimeGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				if(mOnTimeSelectedClickLister!=null){    //“时间”选择完毕,回调日期时间选择监听器的处理函数
					
					mOnTimeSelectedClickLister.OnTimeSelected(timeItems[position]);
				}
				
				cancel();
			}
		});
    	
    	mCancelBtn=(Button)contentView.findViewById(R.id.mCancelBtn);
    	mCancelBtn.setOnClickListener(this);
    	
    }
    
    //更新ui
    private void updateViewsStatus(int index,boolean isToday){
    	
    	if(index==curSelectedIndex){   
    		return;
    	}
    	
    	if(index==0){  //当前显示“日期”
    		
    		mArrowUpIv1.setVisibility(View.VISIBLE);
    		mArrowUpIv2.setVisibility(View.INVISIBLE);
    		
    		mDateContainer.setBackgroundColor(getResources().getColor(R.color.orange_selected));
    		mTimeContainer.setBackgroundColor(getResources().getColor(R.color.orange_normal));
    		
    		mDateListView.setVisibility(View.VISIBLE);
    		mTimeGridView.setVisibility(View.GONE);
    		
    	}else{  //当前显示“时间”
    		
    		mDateContainer.setBackgroundColor(getResources().getColor(R.color.orange_normal));
    		mTimeContainer.setBackgroundColor(getResources().getColor(R.color.orange_selected));
    		
    		mArrowUpIv2.setVisibility(View.VISIBLE);
    		mArrowUpIv1.setVisibility(View.INVISIBLE);
    		
    		if(isToday){  //用户“日期”选择了“今天”，则出现的“时间”应为:从当前时间至18:00之间的整点
    			
    			timeItems=Utils.getAfterTime();
    			
    		}else{   //用户“日期”选择了“明天”或“后天”，则出现的“时间”应为:6::00至18:00之间的整点
    			
    			timeItems=getResources().getStringArray(R.array.time_items);
    			
    		}
    		
    		if(mTimeBaseAdapter==null){
    			mTimeBaseAdapter=new MyTimeBaseAdapter(mContext);
    			mTimeGridView.setAdapter(mTimeBaseAdapter);
    		}else{
    			mTimeBaseAdapter.notifyDataSetChanged();
    		}
    		
    		mDateListView.setVisibility(View.GONE);
    		mTimeGridView.setVisibility(View.VISIBLE);
    	}
    	
    	curSelectedIndex=index;
    	
    	new Handler().post(new Runnable() {  //mScrollView滚动到底部,使本控件完全显示
		    @Override
		    public void run() {
		    	mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
		    }
		});  
    }
    
    //“日期”显示Listview的适配器
    class MyDateBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyDateBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return dateItems.length;
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
				convertView=mInflater.inflate(R.layout.item_custom_time_selector, null);
			}
			
			TextView mNameTv=(TextView)convertView.findViewById(R.id.mNameTv);
			
			mNameTv.setText(dateItems[position]);
			
			return convertView;
		}	
	}
    
    //“时间”显示GridView的适配器
    class MyTimeBaseAdapter extends BaseAdapter{
		
		private Context mContext;
		
		private LayoutInflater mInflater;

		public MyTimeBaseAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			
			mInflater=(LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return timeItems.length;
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
				convertView=mInflater.inflate(R.layout.item_custom_time_selector, null);
			}
			
			TextView mNameTv=(TextView)convertView.findViewById(R.id.mNameTv);
			
			mNameTv.setText(timeItems[position]);
			
			return convertView;
		}	
	}

	@Override
	public void onClick(View view) {
		
		//int index=-1;
		
		switch (view.getId()) {
		/*case R.id.mDateTv:
			index=0;
			break;
        case R.id.mTimeTv:
        	index=1;
			break;*/
        case R.id.mCancelBtn:    //“取消”按钮
        	cancel();
			break;
		default:
			break;
		}
		
	}
	
	//取消
	public void cancel(){
		
		updateViewsStatus(0, false);
		
		setVisibility(View.GONE);
	}
	
	//控件visible前，刷新界面信息
	public void refresh(){
		
		if(Utils.isTodayAvailable()){   //当前可以预约取件
			
			mode=MODE_TODAY_AVAILABLE;
			
			dateItems=getResources().getStringArray(R.array.date_items);
			
		}else{   //当前不可以预约取件，“日期”屏蔽“马上寄出”，“今天”
			
			mode=MODE_TODAY_NOT_AVAILABLE;
			
			dateItems=getResources().getStringArray(R.array.date_items_today_not_available);
		}
		
		if(mDateBaseAdapter==null){
			mDateBaseAdapter=new MyDateBaseAdapter(mContext);
	    	mDateListView.setAdapter(mDateBaseAdapter);
		}else{
			mDateBaseAdapter.notifyDataSetChanged();
		}
		
		Utils.setListViewHeightBasedOnChildren(mDateListView);
		
	}

}
