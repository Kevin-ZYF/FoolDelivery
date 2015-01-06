package com.weiwend.fooldelivery.items;

import java.io.Serializable;

public class SendInfoItem implements Serializable{
	
	private String id;           //订单的id
  	private String weight;       //订单物品的重量
  	private String size;         //订单物品的体积
	private String gtype;        //订单物品的状态(固态、液态、气态)
	private String qjtm;         //预约取件时间
	private String comet;        //订单备注
	private int stat;            //订单的状态
	//private String statusText;
	private String snum;         //订单单号
	private String cid;          //选择的快递公司id
	private String cname;        //选择的快递公司名称
	private String logo;         //选择的快递公司图标
	private String wid;          //选择的网点id
	private String gnam;         //订单物品的名称
	
	private AddressItem senderAddressItem;  //订单的寄件人信息
	private AddressItem recipientAddressItem;  //订单的发件人信息
	
	public SendInfoItem(){
		senderAddressItem=new AddressItem();
		recipientAddressItem=new AddressItem();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQjtm() {
		return qjtm;
	}

	public void setQjtm(String qjtm) {
		this.qjtm = qjtm;
	}

	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getGtype() {
		return gtype;
	}
	public void setGtype(String gtype) {
		this.gtype = gtype;
	}
	public String getComet() {
		return comet;
	}
	public void setComet(String comet) {
		this.comet = comet;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getWid() {
		return wid;
	}
	public void setWid(String wid) {
		this.wid = wid;
	}
	public AddressItem getSenderAddressItem() {
		return senderAddressItem;
	}
	public void setSenderAddressItem(AddressItem senderAddressItem) {
		this.senderAddressItem = senderAddressItem;
	}
	public AddressItem getRecipientAddressItem() {
		return recipientAddressItem;
	}
	public void setRecipientAddressItem(AddressItem recipientAddressItem) {
		this.recipientAddressItem = recipientAddressItem;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	/*public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}*/

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getSnum() {
		return snum;
	}

	public void setSnum(String snum) {
		this.snum = snum;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getGnam() {
		return gnam;
	}

	public void setGnam(String gnam) {
		this.gnam = gnam;
	}
}
