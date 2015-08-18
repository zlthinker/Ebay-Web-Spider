package org.epiclouds.ebayItemSpider.spider;

import org.epiclouds.ebayItemSpider.handler.ItemHandler;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

public class CategoryItemSpider extends AbstractSpiderObject{

	private String  categoryId;
	private String  itemId;
	private final String host="www.ebay.co.uk";
	private final String url_pre="/itm/";
	
	public CategoryItemSpider(AbstractSpiderObject parent,String categoryId,String itemId){
		super(parent);
		this.categoryId=categoryId;
		this.itemId=itemId;
	}

	

	@Override
	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		String url=url_pre+itemId;
		ItemHandler itemHandler=new ItemHandler(url,host,this,itemId,categoryId);
		return itemHandler;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
