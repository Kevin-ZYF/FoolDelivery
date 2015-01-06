package com.weiwend.invalid;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.weiwend.fooldelivery.BaseActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.R.id;
import com.weiwend.fooldelivery.R.layout;
import com.weiwend.fooldelivery.sqlite.AreasDatabaseHelperUtil;
import com.weiwend.fooldelivery.utils.Utils;

//*****************************该界面由于需求问题，暂时已经作废*************************
public class AreaSelectorActivity extends BaseActivity implements View.OnClickListener{
	
	private ArrayList<AreaItem2> mAreaItems=new ArrayList<AreaItem2>();
	private MyBaseAdapter mBaseAdapter;
	
	private EditText mAreaInputEt;
	private GridView mGridView;
	
	private AreasDatabaseHelperUtil mAreasDatabaseHelperUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_area_selector);
		
		mGridView=(GridView)findViewById(R.id.mGridView);
		mBaseAdapter=new MyBaseAdapter(this);
		mGridView.setAdapter(mBaseAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Log.e("zyf","area: "+mAreaItems.get(position).getArea_name());
			}
		});
		
		mAreasDatabaseHelperUtil=new AreasDatabaseHelperUtil(this);
		
		mAreaInputEt=(EditText)findViewById(R.id.mAreaInputEt);
		mAreaInputEt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				String key=mAreaInputEt.getText().toString();
				if(key.length()==0){
					mAreaItems.clear();
				}else{
					if(Utils.isStrChinese(key)){    //Chinese String
						mAreaItems=mAreasDatabaseHelperUtil.getFuzzyAreasWithChineseKey(key);
					}else{
						mAreaItems=mAreasDatabaseHelperUtil.getFuzzyAreasWithEnglishKey(key);
					}
				}
				mBaseAdapter.notifyDataSetChanged();
				
				//Log.e("zyf",PinyinUtils.getPinYinHeadChar(key));
			}
		});
	}

	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
	}
	
	class MyBaseAdapter extends BaseAdapter
	{
		
		Context mContext;
		LayoutInflater mInflater;
		
		public MyBaseAdapter(Context context)
		{
			mContext=context;
			mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mAreaItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			
			if(convertView==null)
			{
				convertView=mInflater.inflate(R.layout.item_gridview_area_selector_fuzzy, null);
			}
			
			final TextView mNameTv=(TextView)convertView.findViewById(R.id.mNameTv);

			mNameTv.setText(mAreaItems.get(position).getArea_name());
			
			return convertView;
		}	
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:
			finish();
			break;

		default:
			break;
		}
	}

}
