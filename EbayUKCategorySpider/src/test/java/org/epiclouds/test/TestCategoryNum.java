package org.epiclouds.test;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.epiclouds.bean.DataBean;
import org.epiclouds.bean.TerapeakBean;

public class TestCategoryNum {
	private static volatile MongoClient client;
	private static int count6 = 0;
	private static int count65 = 0;
	private static int count7 = 0;
	private static int total = 0;
	
	public static void main(String args[]) throws UnknownHostException {
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential("root", "admin","123Yuanshuju456".toCharArray());
			client = new MongoClient(new ServerAddress("106.3.38.50",27017), Arrays.asList(credential));
		}
		DB db=client.getDB("ebayUK");
		DBCollection col=db.getCollection("category_data");
		DBCursor cursor = col.find(new BasicDBObject());
		DBObject result;
		int i=0;
		while(cursor.hasNext()) {
			if ((result = cursor.next()) != null) {
				DataBean databean = JSONObject.parseObject(result.toString(),DataBean.class);
			//	System.out.println(databean.toString());
				TerapeakBean tb = databean.getData();
				int num = tb.getTotal_listings().getData().size();
				total ++;
				if (num > 700){
					count7 ++;
				}
				if (num > 650){
					count65 ++;					
				}
				if (num > 600){
					count6 ++;					
				}
				DecimalFormat df = new DecimalFormat("#####0.0000");
				double d7 = (double)count7/total;
			//	BigDecimal b7 = new BigDecimal(d7);
			//	double dd7 = b7.setScale(4,   BigDecimal.ROUND_HALF_UP).doubleValue();  
				double d65 = (double)count65/total;
			//	BigDecimal b65 = new BigDecimal(d65);
			//	double dd65 = b65.setScale(4,   BigDecimal.ROUND_HALF_UP).doubleValue();  
				double d6 = (double)count6/total;
			//	BigDecimal b6 = new BigDecimal(d6);
			//	double dd6 = b6.setScale(4,   BigDecimal.ROUND_HALF_UP).doubleValue();  
				System.out.println("total="+total+", 		>700="+count7+", percent="+df.format(d7)+
						", 		>650="+count65+", percent="+df.format(d65)+
						", 		>600="+count6+", percent="+df.format(d6)
						);
			}
		}
		System.out.println("It's over");
		if (client != null) 
			client.close();
	}

}
