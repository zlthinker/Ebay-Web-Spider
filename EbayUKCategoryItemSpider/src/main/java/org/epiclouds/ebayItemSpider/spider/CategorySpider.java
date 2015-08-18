package org.epiclouds.ebayItemSpider.spider;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.epiclouds.ebayItemSpider.bean.RefetchBean;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.annotations.SpiderFeildConfig;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

import com.alibaba.fastjson.annotation.JSONField;


public class CategorySpider  extends AbstractSpiderObject{
	
	@SpiderFeildConfig(desc="爬虫的categoryId")
	private String categoryId;
	@SpiderFeildConfig(desc="爬取的页数")
	private int pageNumber;
	@JSONField(serialize=false,deserialize=false)
	private final ConcurrentHashSet<String> itemIdHashSet=new ConcurrentHashSet<String>();
	@JSONField(serialize=false,deserialize=false)
	private List<RefetchBean> itemList=new LinkedList<RefetchBean>();
	public CategorySpider(){
		
	}

	public boolean addItem(String itemId){
		synchronized (itemIdHashSet) {
			if(itemIdHashSet.contains(itemId)){
				return false;
			}
			else{
				itemIdHashSet.add(itemId);
				return true;
			}
		}
	}
	
	@Override
	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/*
	 * 
	 * category直接启动页而不需要爬取任何信息
	 * 再启动一个虚拟的页，用来获取所有的refetch_list中所有的item，并爬取
	 * 
	 */
	@Override
	public synchronized void start() throws Exception{
		super.start();
		for(int i=0;i<pageNumber;i++){
			CategoryPageSpider spiderObject2=new CategoryPageSpider();
			spiderObject2.setCategoryId(categoryId);
			spiderObject2.setParent(this);
			spiderObject2.setPageNumber(i+1);			
			addChild(spiderObject2);
		}
		
	}
	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return categoryId;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	@JSONField(serialize=false,deserialize=false)
	public ConcurrentHashSet<String>  getItemSet(){
		return itemIdHashSet;
	}
	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public void setPageNumber(String pageNumber) {
		this.pageNumber = Integer.parseInt(pageNumber);
	}

	@Override
	public void finish() {
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.FIND
				,this.getConfigManager().getValue("spider_database",String.class),
				this.getConfigManager().getValue("refetch_item_table",String.class))
				.addConditon(new EqualCondition<String>("categoryId", categoryId)).build();
		try {
			itemList=this.getDbmanager().find(sb,RefetchBean.class);
			System.out.println("the historyPage itemNumber is"+ itemList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(itemList.size()==0){
			super.finish();
		}
		else{
			int skip=0;
			for( RefetchBean bean:itemList){
				CategorySpider parent=this;
				if(parent.addItem(bean.getItemId())){
					CategoryItemSpider spiderObject=new CategoryItemSpider(this,bean.getCategoryId(),bean.getItemId());
					addChild(spiderObject);
				}else{
					skip++;
				}
			}
			if(skip>=itemList.size()){
				super.finish();
				return;
			}
		}
	}
	
}
