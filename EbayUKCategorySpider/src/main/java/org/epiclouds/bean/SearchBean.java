package org.epiclouds.bean;

public class SearchBean {
	private volatile String id;
	private volatile String siteID;
	private volatile String query;
	private volatile String date;
	private volatile int date_range;
	private volatile String sellerId;
	private volatile String currency;

	public String getSiteID() {
		return siteID;
	}
	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		if(query!=null){
			this.query=query.replaceAll("\\*|\\(|\\)|\"|-|\\?", " ");
		}
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getDate_range() {
		return date_range;
	}
	public void setDate_range(int date_range) {
		this.date_range = date_range;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSellerId() {
		return sellerId;
	}
	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
}
