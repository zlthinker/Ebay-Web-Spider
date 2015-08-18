package org.epiclouds.spiders.dbstorage.manager.impl;

import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.springframework.stereotype.Component;
/**
 * The mongodb async version
 * @author xianglong
 * @created 2015年6月5日 下午4:44:23
 * @version 1.0
 */
public class MongoDBAsyncManager extends MongoDBSyncManager implements Runnable{
	private Queue<StorageBean> que=new LinkedBlockingQueue<StorageBean>();
	public MongoDBAsyncManager()
			throws UnknownHostException {
		super();
		// TODO Auto-generated constructor stub
		
	}

	public void init() throws Exception{
		super.init();
		Executors.newSingleThreadExecutor().execute(this);
	}
	
	@Override
	protected void execute(StorageBean sb) {
		if(sb!=null)
			que.add(sb);
	}


	@Override
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		
		while(true){
			StorageBean sb=que.poll();
			if(sb!=null){
				try {
					super.execute(sb);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
    
	
	


	

}
