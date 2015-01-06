package com.weiwend.fooldelivery.items;

public class HotCityItem {
	
	private int hIndex;   //热门城市的index
	private String cId;   //热门城市的id
	private String cName; //热门城市的名称
	private String pId;   //热门城市所属省份的id
	private String pName; //热门城市所属省份的名称
	
	public int gethIndex() {
		return hIndex;
	}
	public void sethIndex(int hIndex) {
		this.hIndex = hIndex;
	}
	public String getcId() {
		return cId;
	}
	public void setcId(String cId) {
		this.cId = cId;
	}
	public String getcName() {
		return cName;
	}
	public void setcName(String cName) {
		this.cName = cName;
	}
	public String getpId() {
		return pId;
	}
	public void setpId(String pId) {
		this.pId = pId;
	}
	public String getpName() {
		return pName;
	}
	public void setpName(String pName) {
		this.pName = pName;
	}

}
