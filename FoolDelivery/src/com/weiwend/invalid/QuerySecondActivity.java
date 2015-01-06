package com.weiwend.invalid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.BaseActivity;
import com.weiwend.fooldelivery.DeliveryCompanySelectActivity;
import com.weiwend.fooldelivery.QueryResultActivity;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.R.id;
import com.weiwend.fooldelivery.R.layout;
import com.weiwend.fooldelivery.R.string;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.items.DeliveryCompanyItem;
import com.weiwend.fooldelivery.scan.MipcaCaptureActivity;
import com.weiwend.fooldelivery.utils.ActivityResultCode;

//******************************该界面由于需求问题，暂时已经作废*******************************
public class QuerySecondActivity extends BaseActivity implements OnClickListener{
	
	private Button mScanBtn,mDeliveryCompanySelectBtn,mQueryBtn,mArrowBtn;
	
	private TextView mDeliveryCompanyTv;
	
	private EditText mCodeShowEt;
	
	private LinearLayout mCompanySelectContainer;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_query_second);
		
		initViews();
	}
	
	private void initViews(){
		mScanBtn=(Button)findViewById(R.id.mScanBtn);
		mDeliveryCompanySelectBtn=(Button)findViewById(R.id.mDeliveryCompanySelectBtn);
		mQueryBtn=(Button)findViewById(R.id.mQueryBtn);
		mArrowBtn=(Button)findViewById(R.id.mArrowBtn);
		
		mScanBtn.setOnClickListener(this);
		
		mQueryBtn.setOnClickListener(this);
		
		mDeliveryCompanySelectBtn.setOnClickListener(this);
		mArrowBtn.setOnClickListener(this);
		
		mCodeShowEt=(EditText)findViewById(R.id.mCodeShowEt);
		mDeliveryCompanyTv=(TextView)findViewById(R.id.mDeliveryCompanyTv);
		
		mCompanySelectContainer=(LinearLayout)findViewById(R.id.mCompanySelectContainer);
		mCompanySelectContainer.setOnClickListener(this);
	}

	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_query));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:
			finish();
			break;
		case R.id.mScanBtn:
			Intent intent = new Intent(this, MipcaCaptureActivity.class);
			startActivityForResult(intent, ActivityResultCode.CODE_SCAN);
			break;
        case R.id.mDeliveryCompanySelectBtn:
        	Intent intent2=new Intent(QuerySecondActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent2, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
			break;
        case R.id.mArrowBtn:
        	Intent intent4=new Intent(QuerySecondActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent4, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
        	break;
        case R.id.mCompanySelectContainer:
        	
        	Intent intent5=new Intent(QuerySecondActivity.this,DeliveryCompanySelectActivity.class);
			startActivityForResult(intent5, ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR);
			
        	break;
        case R.id.mQueryBtn:
        	
        	if(mCodeShowEt.getText().toString().length()==0||mDeliveryCompanyTv.getText().toString().length()==0){
        		
        		//MyToast.makeText(QuerySecondActivity.this, getString(R.string.prompt_delivery_query), MyToast.LENGTH_SHORT).show();
        		return;
        	}
        	
        	Intent intent3=new Intent(QuerySecondActivity.this,QueryResultActivity.class);
			intent3.putExtra("cid", deliveryCompanyItem.getId());
			intent3.putExtra("snum", mCodeShowEt.getText().toString());
			intent3.putExtra("logo", deliveryCompanyItem.getLogo());
			intent3.putExtra("name", deliveryCompanyItem.getName());
			startActivity(intent3);
			break;
		default:
			break;
		}
	}
	
	private DeliveryCompanyItem deliveryCompanyItem;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_SCAN){
			if(data!=null){
				Bundle bundle = data.getExtras();
				Log.e("zyf","scan code: "+bundle.getString("result"));
				
				mCodeShowEt.setText(bundle.getString("result"));
			}
		}else if(requestCode==ActivityResultCode.CODE_DELIVERY_COMPANY_SELECTOR){
			if(data!=null){
				deliveryCompanyItem = (DeliveryCompanyItem) data.getSerializableExtra("DeliveryCompanyItem");
				
				Log.e("zyf",deliveryCompanyItem.getName());
				
				mDeliveryCompanyTv.setText(deliveryCompanyItem.getName());
			}
		}
	}

}
