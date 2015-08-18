package org.epiclouds.ebayItemSpider.run;


import java.util.Arrays;

import org.epiclouds.ebayItemSpider.spider.CategorySpider;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;
import org.epiclouds.spiders.spiderobject.manager.abstracts.SpiderObjectManagerInterface;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;



public class SpiderStart {
	private static Bootstrap boot;

	public static SpiderObjectManagerInterface spiderManager;
//	private static volatile MongoClient client;

	
	public static void main(String[] args) throws Exception {
		boot=new Bootstrap();
		boot.setBootSpiderClass(CategorySpider.class);
		boot.start();
		
	/*	CategorySpider categoryspider = new CategorySpider();
        categoryspider.setCategoryId("63861");
		categoryspider.setPageNumber(3);
		
		spiderManager=boot.getSingle().getSpiderManager();
		spiderManager.add(categoryspider);*/
	/*	if(client==null){
			MongoCredential credential = MongoCredential.createCredential("yuanshuju", "admin","123".toCharArray());
			client = new MongoClient(new ServerAddress("127.0.0.1",27017), Arrays.asList(credential));
		}
		DB db=client.getDB("ebayUK");
		DBCollection col=db.getCollection("category");
		DBCursor cursor = col.find(new BasicDBObject("isleaf", true));
		DBObject result;
		int i=0;
		while(cursor.hasNext()) {
			if ((result = cursor.next()) != null) {
                CategorySpider categoryspider = new CategorySpider();
                categoryspider.setCategoryId((String)result.get("id"));
				categoryspider.setPageNumber(3);
				spiderManager.add(categoryspider);
				System.out.println("i = "+ (++i) +", CategoryId: "+(String)result.get("id"));
            }
		}
		System.out.println("Finish adding.");*/

	}
}
