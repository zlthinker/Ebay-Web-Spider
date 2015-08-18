/**
 * @author Administrator
 * @created 2014 2014�?12�?8�? 下午1:30:30
 * @version 1.0
 */
package org.epiclouds.spiders.storage.imp;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.epiclouds.spiders.util.ConsoleConfig;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * 
 * @author Administrator
 *
 */
public class MongoManager implements Runnable{
	
	private final static LinkedBlockingQueue<StorageBean> mongoQue = new LinkedBlockingQueue<StorageBean>();
	private static volatile  MongoManager mongo;
	private volatile MongoClient client;
	public static MongoManager getManager() {
		if(mongo==null){
			synchronized (mongoQue) {
				if(mongo==null){
					try {
						init();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return mongo;
	}
	public void addRequest(StorageBean sb){
		if(sb!=null){
			mongoQue.add(sb);
		}
	}
	private static void init() throws UnknownHostException{
		mongo = new MongoManager(ConsoleConfig.getMongo_host(), 
				ConsoleConfig.getMongo_port(),ConsoleConfig.getMongo_user()
				,ConsoleConfig.getMongo_authticateDatabase(),ConsoleConfig.getMongo_pass());
		Executors.newSingleThreadExecutor().execute(mongo);
	}
	private  MongoManager(String host,int port,String userName,String authencateDatabase,String password) 
			throws UnknownHostException{
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential(userName, authencateDatabase, password.toCharArray());
			client = new MongoClient(new ServerAddress(host,port), Arrays.asList(credential));
		}
	}
	
	private  void executeUpOrInsert(StorageBean sb){
		DB db=client.getDB(sb.getDbstr());
		DBCollection col=db.getCollection(sb.getTablestr());
		col.update(sb.getCondition(),sb.getData(), true, false);
	}
	
	public List<DBObject> getObjects(StorageBean sb){
		DB db=client.getDB(sb.getDbstr());
		DBCollection col=db.getCollection(sb.getTablestr());
		DBCursor dc=col.find(sb.getCondition());
		List<DBObject> re=new LinkedList<DBObject>();
		while(dc.hasNext()){
			re.add(dc.next());
		}
		return re;
	}
	
	private  void executeDelete(StorageBean sb){
		DB db=client.getDB(sb.getDbstr());
		DBCollection col=db.getCollection(sb.getTablestr());
		col.remove(sb.getCondition());
	}
	@Override
	public void run() {
		while(true){
			StorageBean sb=null;
			try {
				sb=mongoQue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(sb!=null){
				switch (sb.getType()) {
				case DELETE:
					executeDelete(sb);
					break;
				case INSERT:
					
					break;		
				case UPDATE:
					
					break;
					
				case UPORINSERT:
					executeUpOrInsert(sb);
					break;
				default:
					break;
				}
				
			}
		}
		
	}
	
}
