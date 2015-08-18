package org.epiclouds.bean;



public class TerapeakBean {
	private volatile CommonData items_sold=new CommonData();
	private volatile CommonData total_listings=new CommonData();
	private volatile CommonData bids_per_listings=new CommonData();
	private volatile CommonData bids=new CommonData();
	private volatile CommonData revenue=new CommonData();
	private volatile CommonData sell_through=new CommonData();
	private volatile CommonData average_end_price=new CommonData();
	
	public CommonData getAverage_end_price() {
		return average_end_price;
	}
	public void addAverage_end_price(CommonData average_end_price) {
		if(average_end_price==null){
			return;
		}
		if(this.average_end_price==null){
			this.average_end_price=average_end_price;
			return;
		}
		if(this.average_end_price.getData()==null){
			this.average_end_price.setData(average_end_price.getData());
			return;
		}
		if(average_end_price.getData()==null){
			return;
		}
		this.average_end_price.getData().addAll(average_end_price.getData());
		
	}

	public void addBids(CommonData bids) {
		if(bids==null){
			return;
		}
		if(this.bids==null){
			this.bids=bids;
			return;
		}
		if(this.bids.getData()==null){
			this.bids.setData(bids.getData());
			return;
		}
		if(bids.getData()==null){
			return;
		}
		this.bids.getData().addAll(bids.getData());
		
	}
	public void addBids_per_listings(CommonData bids_per_listings) {
		if(bids_per_listings==null){
			return;
		}
		if(this.bids_per_listings==null){
			this.bids_per_listings=bids_per_listings;
			return;
		}
		if(this.bids_per_listings.getData()==null){
			this.bids_per_listings.setData(bids_per_listings.getData());
			return;
		}
		if(bids_per_listings.getData()==null){
			return;
		}
		this.bids_per_listings.getData().addAll(bids_per_listings.getData());
	}
	public void addItems_sold(CommonData items_sold) {
		if(items_sold==null){
			return;
		}
		if(this.items_sold==null){
			this.items_sold=items_sold;
			return;
		}
		if(this.items_sold.getData()==null){
			this.items_sold.setData(items_sold.getData());
			return;
		}
		if(items_sold.getData()==null){
			return;
		}
		this.items_sold.getData().addAll(items_sold.getData());
	}
	public void addRevenue(CommonData revenue) {
		if(revenue==null){
			return;
		}
		if(this.revenue==null){
			this.revenue=revenue;
			return;
		}
		if(this.revenue.getData()==null){
			this.revenue.setData(revenue.getData());
			return;
		}
		if(revenue.getData()==null){
			return;
		}
		this.revenue.getData().addAll(revenue.getData());
	}

	public void addSell_through(CommonData sell_through) {
		if(sell_through==null){
			return;
		}
		if(this.sell_through==null){
			this.sell_through=sell_through;
			return;
		}
		if(this.sell_through.getData()==null){
			this.sell_through.setData(sell_through.getData());
			return;
		}
		if(sell_through.getData()==null){
			return;
		}
		this.sell_through.getData().addAll(sell_through.getData());
	}
	public void addTotal_listings(CommonData total_listings) {
		if(total_listings==null){
			return;
		}
		if(this.total_listings==null){
			this.total_listings=total_listings;
			return;
		}
		if(this.total_listings.getData()==null){
			this.total_listings.setData(total_listings.getData());
			return;
		}
		if(total_listings.getData()==null){
			return;
		}
		this.total_listings.getData().addAll(total_listings.getData());
	}
	

	public void setAverage_end_price(CommonData average_end_price) {
		this.average_end_price = average_end_price;
	}
	public CommonData getBids() {
		return bids;
	}
	public void setBids(CommonData bids) {
		this.bids = bids;
	}
	public CommonData getBids_per_listings() {
		return bids_per_listings;
	}
	public void setBids_per_listings(CommonData bids_per_listings) {
		this.bids_per_listings = bids_per_listings;
	}
	public CommonData getItems_sold() {
		return items_sold;
	}
	public void setItems_sold(CommonData items_sold) {
		this.items_sold = items_sold;
	}
	public CommonData getRevenue() {
		return revenue;
	}
	public void setRevenue(CommonData revenue) {
		this.revenue = revenue;
	}
	public CommonData getSell_through() {
		return sell_through;
	}
	public void setSell_through(CommonData sell_through) {
		this.sell_through = sell_through;
	}
	public CommonData getTotal_listings() {
		return total_listings;
	}
	public void setTotal_listings(CommonData total_listings) {
		this.total_listings = total_listings;
	}
}
