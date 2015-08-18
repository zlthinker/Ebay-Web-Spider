package org.epiclouds.ebayItemSpider.bean;

import org.joda.time.DateTime;

public class SoldNumberBean {
	private String time;
	private String soldnumber;
	public SoldNumberBean(){}
	
	public SoldNumberBean(String soldnumber){
		this.soldnumber=soldnumber;
		this.time=new DateTime().toString("yyyy-MM-dd HH:mm:ss");
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getSoldnumber() {
		return soldnumber;
	}
	public void setSoldnumber(String soldnumber) {
		this.soldnumber = soldnumber;
	}
	
}
