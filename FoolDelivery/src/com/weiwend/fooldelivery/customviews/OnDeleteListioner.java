package com.weiwend.fooldelivery.customviews;

public interface OnDeleteListioner {
	public abstract boolean isCandelete(int position);
	public abstract void onDelete(int ID);
	public abstract void onEdit(int ID);
	public abstract void onBack();
}
