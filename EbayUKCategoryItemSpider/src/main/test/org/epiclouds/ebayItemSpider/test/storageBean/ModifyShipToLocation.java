package org.epiclouds.ebayItemSpider.test.storageBean;

import java.io.IOException;
import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class ModifyShipToLocation {
	private static volatile MongoClient client;
	public static void main(String[] args) throws IOException {
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential("root", "admin","123Yuanshuju456".toCharArray());
			client = new MongoClient(new ServerAddress("106.3.38.50",27017), Arrays.asList(credential));
		}
		DB db=client.getDB("ebayUK");
		DBCollection col=db.getCollection("category_item");
		
		DBCursor cursor=col.find();
		int i = 0;
		while(cursor.hasNext()) {
			DBObject dbo = cursor.next();
			String itemid = (String)dbo.get("itemId");
			String shipTo_location = (String)dbo.get("shipToLocation");
			
			if (shipTo_location == null || shipTo_location.length() < 1) {
				System.out.println(itemid + ": "+shipTo_location);
				System.out.println(itemid + "'s shipToLocation is empty.");
				continue;
			}
			
			
			
			int strend = shipTo_location.length() - 1;
			int len = strend;
			while(strend>= 0 && (shipTo_location.charAt(strend)==' ' || shipTo_location.charAt(strend)==160 || shipTo_location.charAt(strend)==124)){
				System.err.println(strend+": "+shipTo_location.charAt(strend));
				strend --;
				
			}
			shipTo_location = shipTo_location.substring(0, strend+1);
			
			if (strend < len) {
				System.out.println((i++) + ": "+itemid + "'s shipToLocation is updated.");
				BasicDBObject con=new BasicDBObject();
				con.put("itemId", itemid);
				DBObject updateValue = new BasicDBObject();
				updateValue.put("shipToLocation", shipTo_location);
				DBObject updateSetValue=new BasicDBObject("$set",updateValue); 
				col.update(con, updateSetValue);
			}
		}
		
	}

}
