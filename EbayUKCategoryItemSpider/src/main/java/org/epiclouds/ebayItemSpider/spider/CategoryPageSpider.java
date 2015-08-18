package org.epiclouds.ebayItemSpider.spider;

import org.epiclouds.ebayItemSpider.handler.PageHandler;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

public class CategoryPageSpider extends AbstractSpiderObject{

	private int pageNumber;
	private String categoryId;


	private final String host="www.ebay.co.uk";
	private final String url_suf="/i.html?_ipg=200&rt=nc&_dmd=1&LH_BIN=1&_pgn=";
	private final String url_pre="/sch/";
	



	@Override
	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		String url=url_pre+categoryId+url_suf+pageNumber;
		PageHandler handler=new PageHandler(url,host,this,categoryId);
		handler.setPageNumber(pageNumber);
		System.out.println("Page handler is created: "+url);
		return handler;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}
