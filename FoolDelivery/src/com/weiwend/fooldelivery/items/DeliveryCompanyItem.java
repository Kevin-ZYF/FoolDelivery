package com.weiwend.fooldelivery.items;

import java.io.Serializable;

public class DeliveryCompanyItem implements Serializable{
	
	private String id;    //快递公司的id
	private String name;  //快递公司的名称
	private String telp;  //快递公司的联系方式
	private String logo;  //快递公司的图标
	private String desp;  //快递公司的描述
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTelp() {
		return telp;
	}
	public void setTelp(String telp) {
		this.telp = telp;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getDesp() {
		return desp;
	}
	public void setDesp(String desp) {
		this.desp = desp;
	}
	
	
}
