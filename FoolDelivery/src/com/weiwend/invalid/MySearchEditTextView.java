package com.weiwend.invalid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.weiwend.fooldelivery.R;

//******************************该类由于需求问题，暂时已经作废*******************************
public class MySearchEditTextView extends RelativeLayout {
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	private EditText mEditText;
	
	private Button mSearchBtn;
	
	public interface MyOnSearchClickLister
	{
		void OnSearch(String text);
	};
	
	private MyOnSearchClickLister mOnSearchClickLister;
	
	public void setOnSearchClickLister(MyOnSearchClickLister mOnSearchClickLister)
	{
		this.mOnSearchClickLister=mOnSearchClickLister;
	}

	public MySearchEditTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		
		initViews();
	}
	
	private void initViews(){
		 View contentView=mInflater.inflate(R.layout.custom_search_edittext, this);//must write this
		 
		 mEditText=(EditText)contentView.findViewById(R.id.mEditText);
		 mSearchBtn=(Button)contentView.findViewById(R.id.mSearchBtn);
		 
		 mSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				if(mOnSearchClickLister!=null){
					mOnSearchClickLister.OnSearch(mEditText.getText().toString());
				}
			}
		});
	}
	
	public void setHint(String hint){
		mEditText.setHint(hint);
	}
	
	public void setText(String text){
		mEditText.setText(text);
	}

}
