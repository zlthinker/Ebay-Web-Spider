package org.epiclouds.ebayItemSpider.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.epiclouds.ebayItemSpider.spider.CategoryItemSpider;
import org.epiclouds.ebayItemSpider.spider.CategorySpider;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageHandler extends AbstractNettyCrawlerHandler{
	
	private List<String> itemIdList=new LinkedList<String>();
	private int pageNumber;
	private String categryId;

	public static HashMap<String,String> hs;
	static{
		hs=new HashMap<String,String>();
		//headers
	    hs.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

	    hs.put("Origin","http://www.ebay.co.uk/");
	}

	public PageHandler(String url, String host,
			AbstractSpiderObject spider,String categoryId) {
		super(host,url,spider);
		this.getHeaders().putAll(hs);
		this.categryId=categoryId;
	}

	@Override
	public void handle(String content) throws Exception {
		System.out.println("Category "+categryId+" Page "+pageNumber+"is handling.");
		Document doc=Jsoup.parse(content);	
		if(doc==null||"".equals(content)){
			throw new Exception("return content is null!");
		}
		Elements eles=doc.select("ul#ListViewInner > li");
		String item_id="";
		String hot_red="";
		Element ele;
		if(eles.size()==0){
			stop();
			return;
		}
		for(int i=0;i<eles.size();i++){
			item_id=eles.get(i).select("[iid]").attr("iid");
			ele=eles.get(i).select("div.hotness-signal.red").first();
			if(ele!=null)
				hot_red=ele.ownText();
			else
				hot_red="";

			if(!item_id.equals("")&&(hot_red.contains("sold"))){
				itemIdList.add(item_id);
			}
		}
		stop();
	}

	@Override
	protected void onBefore() {
		
	}

	@Override
	protected void onNormalFinished() {
		// TODO Auto-generated method stub
		System.out.println("the PageSpider "+pageNumber+" itemList get finish and the item number is: "+itemIdList.size());
		CategorySpider parent=(CategorySpider)this.getSpider().getParent();
		AbstractSpiderObject sb=this.getSpider();
		if(itemIdList.size()==0){
			sb.finish();
			return;
		}
		int size = 0;
		for(String itemId:itemIdList){
			if(parent.addItem(itemId)){
				CategoryItemSpider spiderObject=new CategoryItemSpider(sb,categryId,itemId);
				sb.addChild(spiderObject);
			} else {
				size ++;
			}
		}
		if(size >= itemIdList.size()){
			sb.finish();
			return;
		}
		
	}

	@Override
	protected void onDataFinished() {
		// TODO Auto-generated method stub
		//nothing
	}
	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
}
