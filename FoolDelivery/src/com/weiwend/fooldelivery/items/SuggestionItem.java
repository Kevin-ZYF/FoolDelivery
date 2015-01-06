package com.weiwend.fooldelivery.items;

public class SuggestionItem {
	
	private int type;       //类型(投诉、建议)
	private String snum;    //单号名称
	private String content;  //投诉内容
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSnum() {
		return snum;
	}
	public void setSnum(String snum) {
		this.snum = snum;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
