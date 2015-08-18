package org.epiclouds.spiders.spiderbean.util;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.storage.imp.MongoManager;
import org.epiclouds.spiders.storage.imp.StorageBean;
import org.epiclouds.spiders.storage.imp.StorageBean.OperationType;
import org.epiclouds.spiders.util.ConsoleConfig;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * the spider manager
 * @author xianglong
 */
public class SpiderStatusManager {
	/**
	 * the ebay spider status bean
	 */
	private final static ConcurrentHashMap<String,SpiderClassBean> ebaySpiderStatus=new ConcurrentHashMap<String,SpiderClassBean>();

	
	/**
	 * get spider class number
	 * @return
	 */
	public static int getSpiderClassSize(){
		return ebaySpiderStatus.size();
	}
	
	/**
	 * get all spiderClassBean
	 * @return
	 */
	public static List<SpiderClassBean> getAllSpiderClassBeans(){
		List<SpiderClassBean> re=new LinkedList<SpiderClassBean>();
		synchronized(SpiderStatusManager.ebaySpiderStatus){
			for(SpiderClassBean esb:ebaySpiderStatus.values()){
				re.add(esb);
			}
			return re;
		}
	}
	
	
	
	/**
	 * remove spider based on name
	 * @param name
	 * @return
	 * @throws UnknownHostException 
	 */
	public static boolean removeSpider(String name) throws UnknownHostException{
		if(name==null){
			return false;
		}
		SpiderClassBean bean=null;
		synchronized(SpiderStatusManager.ebaySpiderStatus){
			bean=ebaySpiderStatus.remove(name);
		}
		if(bean!=null){
			StorageBean sb=new StorageBean();
			sb.setDbstr(ConsoleConfig.getStore_database());
			sb.setTablestr(ConsoleConfig.getMeta_table());
			sb.setType(OperationType.DELETE);
			DBObject db=new BasicDBObject();
			db.put("name", name);
			sb.setCondition(db);
			MongoManager.getManager().addRequest(sb);
		}
		return true;
	}
	


	/**
	 * put back the spider info
	 * @param esb
	 */
	public  static void  putBackSpider(SpiderClassBean esb) {
		synchronized(SpiderStatusManager.ebaySpiderStatus){
			if(esb!=null){
				StorageBean sb=new StorageBean();
				sb.setDbstr(ConsoleConfig.getStore_database());
				sb.setTablestr(ConsoleConfig.getMeta_table());
				sb.setType(OperationType.UPORINSERT);
				DBObject db=new BasicDBObject();
				db.put("name", esb.getName());
				sb.setCondition(db);
				DBObject data=new BasicDBObject();
				data.put("name", esb.getName());
				data.put("data", JSONObject.toJSONString(esb));
				sb.setData(data);
				MongoManager.getManager().addRequest(sb);
				ebaySpiderStatus.put(esb.getName(),esb);
			}
		}
	}
	

	
	/**
	 * put back the spider info
	 * @param esb
	 */
	public  static SpiderClassBean  getSpiderClassBean(String name) {
		synchronized(SpiderStatusManager.ebaySpiderStatus){
			if(name!=null){
				return ebaySpiderStatus.get(name);
			}
		}
		return null;
	}
	
	
	/**
	 * get  the addSpider config 
	 * @param name
	 */
	public static List<AddSpiderObjectBean> getAddSpiderConfig(String name){
		synchronized(SpiderStatusManager.ebaySpiderStatus){
			if(name!=null){
				return ebaySpiderStatus.get(name)!=null?(ebaySpiderStatus.get(name).getAddConfigs()):null;
			}
		}
		return null;
	}
	

	
}
