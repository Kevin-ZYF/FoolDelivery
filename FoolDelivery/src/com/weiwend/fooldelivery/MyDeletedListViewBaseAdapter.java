package com.weiwend.fooldelivery;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.customviews.OnDeleteListioner;
import com.weiwend.fooldelivery.items.AddressItem;

public class MyDeletedListViewBaseAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<AddressItem> mlist = null;
	private OnDeleteListioner mOnDeleteListioner;
	private boolean delete = false;

	// private Button curDel_btn = null;
	// private UpdateDate mUpdateDate = null;

	public MyDeletedListViewBaseAdapter(Context mContext, ArrayList<AddressItem> mlist) {
		this.mContext = mContext;
		this.mlist = mlist;

	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setOnDeleteListioner(OnDeleteListioner mOnDeleteListioner) {
		this.mOnDeleteListioner = mOnDeleteListioner;
	}

	public int getCount() {

		return mlist.size();
	}

	public Object getItem(int pos) {
		return mlist.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}

	public View getView(final int pos, View convertView, ViewGroup p) {

		final ViewHolder viewHolder;
		
		if (convertView == null) {
			
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_del_listview_address, null);
			
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.mNameView);
			viewHolder.phone = (TextView)convertView.findViewById(R.id.mPhoneView);
			viewHolder.pre_address = (TextView)convertView.findViewById(R.id.mPreAddressView);
			viewHolder.detail_address = (TextView)convertView.findViewById(R.id.mDetailAddressView);
			viewHolder.delete = (TextView) convertView.findViewById(R.id.mDeleteView);
			viewHolder.edit = (TextView) convertView.findViewById(R.id.mEditView);
			viewHolder.mRightContainer = (LinearLayout)convertView.findViewById(R.id.mRightContainer);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final OnClickListener mOnDeleteClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (mOnDeleteListioner != null)
					mOnDeleteListioner.onDelete(pos);

			}
		};
		
		final OnClickListener mOnEditClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (mOnDeleteListioner != null)
					mOnDeleteListioner.onEdit(pos);

			}
		};
		
		//viewHolder.delete.setOnClickListener(mOnClickListener);
		
		viewHolder.delete.setOnClickListener(mOnDeleteClickListener);
		viewHolder.edit.setOnClickListener(mOnEditClickListener);
		
		viewHolder.name.setText(mlist.get(pos).getName());
		viewHolder.phone.setText(mlist.get(pos).getTelp());
		viewHolder.pre_address.setText(mlist.get(pos).getpName()+mlist.get(pos).getcName()+mlist.get(pos).getdName());
		viewHolder.detail_address.setText(mlist.get(pos).getAddress());
		
		return convertView;
	}

	public static class ViewHolder {
		public TextView name;
		public TextView delete;
		public TextView edit;
        TextView phone;
        TextView pre_address;
        TextView detail_address;
        
        LinearLayout mRightContainer;

	}

}
