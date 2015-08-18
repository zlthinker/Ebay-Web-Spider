package org.epiclouds.spider;

import io.netty.handler.codec.http.HttpMethod;

import org.epiclouds.bean.SearchBean;
import org.epiclouds.handler.TerapeakItemHandler;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.epiclouds.bean.CategoryBean;

public class TerapeakSpider extends AbstractSpiderObject{
	private String host = "sell.terapeak.com";
	private String URL= "/services/ebay/categories/trends?token=4e5396e3fe80ee1249a0b8147c08c5636a95579b274624fc6ce568ef3d2cdde5";
	private CategoryBean cb;
	private SearchBean sb;
	private int days;
	private int num;
	
	public TerapeakSpider(CategoryBean cb,
			AbstractSpiderObject parent, int totalSpiderNum, SearchBean sb) {
		super(parent);
		this.cb = cb;
		this.sb = sb;
	}
	
	public TerapeakSpider(){
		super();
	}


	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		days = this.getConfigManager().getValue("days", int.class);
		num = this.getConfigManager().getValue("spider_num", int.class);
		return new TerapeakItemHandler(host, URL, HttpMethod.POST, this, days, num, sb, cb);
	}


	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
