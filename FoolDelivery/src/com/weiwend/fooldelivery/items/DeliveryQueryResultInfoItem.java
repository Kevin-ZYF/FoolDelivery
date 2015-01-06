package com.weiwend.fooldelivery.items;

import java.util.ArrayList;

public class DeliveryQueryResultInfoItem {
	
	private String date;   //日期
	
	private ArrayList<DeliveryQueryResultItem> deliveryQueryResultItems;  //状态列表
	
	public DeliveryQueryResultInfoItem(){
		deliveryQueryResultItems=new ArrayList<DeliveryQueryResultItem>();
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public ArrayList<DeliveryQueryResultItem> getDeliveryQueryResultItems() {
		return deliveryQueryResultItems;
	}
	public void setDeliveryQueryResultItems(
			ArrayList<DeliveryQueryResultItem> deliveryQueryResultItems) {
		this.deliveryQueryResultItems = deliveryQueryResultItems;
	}
	
}
