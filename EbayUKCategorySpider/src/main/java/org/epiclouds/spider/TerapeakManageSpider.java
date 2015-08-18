package org.epiclouds.spider;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.epiclouds.bean.SearchBean;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean.OperationType;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.epiclouds.bean.CategoryBean;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class TerapeakManageSpider extends AbstractSpiderObject{

	private List<CategoryBean> resultList;
	
	public TerapeakManageSpider(AbstractSpiderObject parent, int totalSpiderNum) {
		super(parent);
	}
	
	public TerapeakManageSpider() {
		super(null);
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		String database = this.getConfigManager().getValue("spider_database", String.class);
		String category_table = this.getConfigManager().getValue("category_table", String.class);
		DBMangerInterface DBManager = this.getDbmanager();
		StorageBean.Builder builder = StorageBean.Builder.newBuilder(OperationType.FIND, database, category_table);
		EqualCondition<Boolean> condition = new EqualCondition<Boolean>("isleaf", true);
		builder.addConditon(condition);
		StorageBean sb = builder.build();
		try {
			resultList = DBManager.find(sb, CategoryBean.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(resultList == null || resultList.isEmpty()) {
			finish();
			return;
		}
		
		for(CategoryBean cb : resultList) {
	//		System.out.println("CategorySpider " + cb.getId() + " is added.");
			SearchBean sbean = new SearchBean();
			sbean.setId(cb.getId());
			sbean.setSiteID(this.getConfigManager().getValue("siteId", String.class));
			sbean.setDate_range(this.getConfigManager().getValue("days", int.class));
			sbean.setCurrency("1");			
			TerapeakSpider ts = new TerapeakSpider(cb, this, 1, sbean);
			this.addChild(ts);			
		}
		
		createIndex();                 //create index of table category_data
	}
	
	public AbstractHandler createSpiderHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return "Ebay";
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		if(this.getParent()==null){
			MongoPartition.run();
		}
		super.finish();
	}
	
	public void createIndex() throws UnknownHostException {
		String database = this.getConfigManager().getValue("spider_database", String.class);
		String table = this.getConfigManager().getValue("category_data_table", String.class);
		String user = this.getConfigManager().getValue("mongo_user", String.class);
		String pwd = this.getConfigManager().getValue("mongo_pass", String.class);
		String host = this.getConfigManager().getValue("mongo_host", String.class);
		MongoCredential credential = MongoCredential.createCredential(user, "admin",pwd.toCharArray());
		MongoClient client = new MongoClient(new ServerAddress(host,27017), Arrays.asList(credential));
		DB db=client.getDB(database);
		DBCollection col=db.getCollection(table);
		col.createIndex(new BasicDBObject("id", 1));
	}

}
