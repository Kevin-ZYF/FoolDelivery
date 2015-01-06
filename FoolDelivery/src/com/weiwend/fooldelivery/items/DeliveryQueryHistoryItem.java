package com.weiwend.fooldelivery.items;

import java.io.Serializable;

public class DeliveryQueryHistoryItem implements Serializable{
	
	private String id;    //查询记录的id
	private String cid;   //查询记录的快递公司的id
	private String snum;  //查询记录的单号
	private String logo;  //查询记录的快递公司的图标
	private String stat;  //查询记录当前的状态
	private String name;  //查询记录的快递公司的名称
	private String stm;   //查询记录的下单时间
	private String remark;   //备注信息
	private boolean offical; //是否已收藏
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getSnum() {
		return snum;
	}
	public void setSnum(String snum) {
		this.snum = snum;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getStat() {
		return stat;
	}
	public void setStat(String stat) {
		this.stat = stat;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStm() {
		return stm;
	}
	public void setStm(String stm) {
		this.stm = stm;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public boolean isOffical() {
		return offical;
	}
	public void setOffical(boolean offical) {
		this.offical = offical;
	}
}
