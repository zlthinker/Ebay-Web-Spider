package org.epiclouds.ebayItemSpider.test.storageBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class ResetOrAddMongoCategoryItemMetaDataByFile {
	private static volatile MongoClient client;
	public static void main(String[] args) throws IOException {
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential("root", "admin","123Yuanshuju456".toCharArray());
			client = new MongoClient(new ServerAddress("106.3.38.50",27017), Arrays.asList(credential));
		}
		DB db=client.getDB("ebayUK");
		DBCollection col=db.getCollection("category_item_meta");
		BasicDBObject con=new BasicDBObject();
		BufferedReader br=new BufferedReader(new FileReader(new File("F://eclipse//categoryUK.txt")));
		
		String line=null;
		int i=0;
		while((line=br.readLine())!=null){
		//	String[] line_data = line.split(" ");
			String categoryId = line;
		//	String pageNum = line_data[1];
			
			con.put("categoryId", categoryId);
			System.err.println(line);
			DBObject oo=col.findOne(con);
			if(oo==null) {
				oo = new BasicDBObject();
				System.err.println("cateoryId: "+categoryId);
				oo.put("_id", categoryId);
				oo.put("finishTime", 0);
				oo.put("pageNumber", 3);
				oo.put("delta", 0);
				oo.put("startTime", System.currentTimeMillis());
				oo.put("id", categoryId);
				oo.put("categoryId", categoryId);
				oo.put("info", categoryId);
				
			}else{
				oo.put("finishTime", 0);
			}
			col.update(con, oo, true, false);
			i++;
			System.err.println(i);
		}
		
	}
	
}
