package org.epiclouds.test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.awt.List;
import java.io.File;

import org.bson.BSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;



public class ImportTop {
	
	private static volatile MongoClient client;
	private static String database = "ebayUK";

	
	public static void importTop () throws Exception {
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential("root", "admin","123Yuanshuju456".toCharArray());
			client = new MongoClient(new ServerAddress("106.3.38.50",27017), Arrays.asList(credential));
		}
		DB db=client.getDB(database);
		DBCollection col=db.getCollection("category");
		
		DBObject condition = new BasicDBObject();
		condition.put("parentid", "-1");
		
		ArrayList<String> topList = new ArrayList<String>();
		DBCursor cursor = col.find(condition);
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			String childid = (String)obj.get("id");
			System.out.println("id = "+ childid);
			topList.add(childid);
		}
		System.out.println(cursor.count());
		DBObject topValue = new BasicDBObject();		
		topValue.put("id", "-1");
		topValue.put("name", "top");
		topValue.put("children", topList);
		topValue.put("parentid", "");
		topValue.put("isleaf", false);
		
		col.insert(topValue);
		if (client != null) 
			client.close();
	}
	
	
}



