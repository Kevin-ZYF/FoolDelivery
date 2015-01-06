package com.weiwend.fooldelivery.items;

import java.io.Serializable;

public class ParityCompanyItem implements Serializable{
	
	private String arrive;    //预计到达时间
	private String logo;      //网点的图标
	private String price;     //预计价格
	private String mobi;      //网点的联系方式
	private String cname;     //网点的名称
	private String official;  //是否加入到我们的平台
	private String addr;      //网点的详细地址
	private String cid;       //网点所属快递公司的id
	private String wid;       //网点的id
	
	public String getArrive() {
		return arrive;
	}
	public void setArrive(String arrive) {
		this.arrive = arrive;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getMobi() {
		return mobi;
	}
	public void setMobi(String mobi) {
		this.mobi = mobi;
	}
	public String getCname() {
		return cname;
	}
	public void setCname(String cname) {
		this.cname = cname;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getOfficial() {
		return official;
	}
	public void setOfficial(String official) {
		this.official = official;
	}
	public String getWid() {
		return wid;
	}
	public void setWid(String wid) {
		this.wid = wid;
	}
}
