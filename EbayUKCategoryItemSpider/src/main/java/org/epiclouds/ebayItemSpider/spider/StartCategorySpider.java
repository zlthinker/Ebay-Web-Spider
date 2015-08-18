package org.epiclouds.ebayItemSpider.spider;

import java.util.LinkedList;
import java.util.List;

import org.epiclouds.ebayItemSpider.bean.CategoryBean;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

public class StartCategorySpider extends AbstractSpiderObject{

	@Override
	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public synchronized void start() throws Exception{
		super.start();
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.FIND
				,this.getConfigManager().getValue("spider_database", String.class),
				this.getConfigManager().getValue("spider_ebaycategories_table", String.class)).
				addConditon(new EqualCondition<Boolean>("isLeaf", true)).
				build();
		List<CategoryBean> re=new LinkedList<CategoryBean>();
		try {
			re = this.getDbmanager().find(sb, CategoryBean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int i=0;
		try{
			for(CategoryBean cb : re) {
				CategorySpider categoryspider=new CategorySpider();
				categoryspider.setCategoryId(cb.getId());
				categoryspider.setPageNumber(3);
				System.out.println("i = "+ (++i) +", CategoryId: "+cb.getId());
				categoryspider.setParent(this);
				addChild(categoryspider);
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
