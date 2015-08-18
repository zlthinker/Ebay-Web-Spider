package org.epiclouds.spider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.epiclouds.spiders.bootstrap.imp.Bootstrap;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoPartition {
	private static boolean runLocal = false;
	private static String mongoClient = "106.3.38.50";
	private static String mongoDataBase;
	private static MongoCredential credential = MongoCredential
			.createCredential("root", "admin", "123Yuanshuju456".toCharArray());
	private static String collection_origin = "category_data";
	// private static String collection_sold = "category_sold";
	// private static String collection_price = "category_price";
	private static String collection_soldAndPrice = "category_sold_price";

	
	public static void run(){
		mongoDataBase="ebayUK";//Bootstrap.getSingle().getAcfm().getValue("spider_database",String.class);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MongoPartition.run2();
			}
		}).start();
	}
	
	private static void run2() {
		Calendar calendar = Calendar.getInstance();
		Date curTime = calendar.getTime();

		System.out.println("start time: " + calendar.get(Calendar.YEAR) + "-"
				+ (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DATE) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE) + ":"
				+ calendar.get(Calendar.SECOND));

		long startTime = curTime.getTime();
		System.out.println();

		partition();

		calendar = Calendar.getInstance();
		curTime = calendar.getTime();

		System.out.println();
		System.out.println("end time: " + calendar.get(Calendar.YEAR) + "-"
				+ (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DATE) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE) + ":"
				+ calendar.get(Calendar.SECOND));

		long endTime = System.currentTimeMillis();
		int diff = (int) ((endTime - startTime) / 1000);
		System.out.println("cost " + diff + " seconds");
		System.out.println();
	}

	private static void partition() {

		MongoClient client = null;
		try {
			if (runLocal == true) {
				client = new MongoClient();
			} else {
				client = new MongoClient(new ServerAddress(mongoClient, 27017),
						Arrays.asList(credential));
			}
			DB db = client.getDB(mongoDataBase);
			DBCollection col = db.getCollection(collection_origin);
			DBCursor cursor = col.find();
			// DBCollection col_sold = db.getCollection(collection_sold);
			// DBCollection col_price = db.getCollection(collection_price);
			DBCollection col_soldAndPrice = db
					.getCollection(collection_soldAndPrice);

			System.out.println("writing into Mongo...");
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String name = (String) obj.get("name");
				String categoryId = (String) obj.get("id");

				String data = obj.get("data").toString();
				if (data.matches("\\{\\}")) {
					continue;
				}
				JSONObject jsonObj = JSONObject.parseObject(data);
				JSONObject categorySold = jsonObj.getJSONObject("items_sold");
				JSONObject categoryPrice = jsonObj
						.getJSONObject("average_end_price");

				//
				// String categorySoldString = JSONObject
				// .toJSONString(categorySold);
				// String categoryPriceString = JSONObject
				// .toJSONString(categoryPrice);
				//
				//
				// // System.out.println(categoryPriceString);
				//
				// DBObject sold = new BasicDBObject();
				// sold.put("name", name);
				// sold.put("id", categoryId);
				// sold.put("data", categorySoldString);
				//
				// DBObject price = new BasicDBObject();
				// price.put("name", name);
				// price.put("id", categoryId);
				// price.put("data", categoryPriceString);

				JSONObject categoryPrice_Sold = new JSONObject();
				categoryPrice_Sold.put("items_sold", categorySold);
				categoryPrice_Sold.put("average_end_price", categoryPrice);
				String categorySoldPriceString = JSONObject
						.toJSONString(categoryPrice_Sold);

				DBObject sold_price = new BasicDBObject();
				sold_price.put("name", name);
				sold_price.put("id", categoryId);
				sold_price.put("data", categorySoldPriceString);

				// col_sold.insert(sold);
				// col_price.insert(price);

				BasicDBObject searchQuery = new BasicDBObject().append("id",
						categoryId);

				DBObject oldRecord = col_soldAndPrice.findOne(searchQuery);
				if (oldRecord == null) {
					col_soldAndPrice.insert(sold_price);
				} else {
					BasicDBObject newDocument = new BasicDBObject();
					newDocument.append("$set", sold_price);
					col_soldAndPrice.update(searchQuery, newDocument);
				}
			}
			System.out.println("Partition finished.");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
				client.close();
			}
		}
	}
	
	public static void main(String args[]) {
		run();
	}
}
