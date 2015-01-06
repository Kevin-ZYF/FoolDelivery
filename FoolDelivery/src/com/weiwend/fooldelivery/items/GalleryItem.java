package com.weiwend.fooldelivery.items;

//首页单个轮播图对象
public class GalleryItem {
	
	private int idx;    //轮播图的序号
	private String path;   //轮播图的远程路径
	private int time;      //轮播图的显示时间
	private String kact;   //轮播图的点击事件
	
	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getKact() {
		return kact;
	}
	public void setKact(String kact) {
		this.kact = kact;
	}
	
	
}
