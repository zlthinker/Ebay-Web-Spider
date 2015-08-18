package org.epiclouds.spiders.spiderobject.manager.abstracts;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
/**
 * manage the spiders of some class object
 * @author xianglong
 * @created 2015年6月16日 下午4:39:23
 * @version 1.0
 */
public class SpiderManagerBean {
	private DelayQueue<AbstractSpiderObject> waitingList=new DelayQueue<AbstractSpiderObject>();
	private ConcurrentHashMap<String,AbstractSpiderObject> runningMap=new ConcurrentHashMap<String, AbstractSpiderObject>();
	private final int runNumberThreshold;
	private final Class<?> cls;
	
	public SpiderManagerBean(Class<?> cls,int runNumberThreshold){
		this.runNumberThreshold=runNumberThreshold;
		this.cls=cls;
	}
	
	public void runOnce(){
		final int size=runningMap.size();
		for(int i=size;i<runNumberThreshold;i++){
			AbstractSpiderObject ob=waitingList.poll();
			if(ob==null){
				break;
			}

			try {
				runningMap.put(ob.getId(), ob);
				ob.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					ob.stop();
				} catch (Exception e1) {
					e1.printStackTrace();
					waitingList.add(ob);
				}
			}
		}
		System.err.println("The spiderObject name is :"+cls.getName()+"  waitingSize:"+getWaitingSize()+" runningNumber:"+getRunningSize());
	}
	
	public int getRunningSize(){
		return runningMap.size();
	}
	
	public int getWaitingSize(){
		return waitingList.size();
	}
	
	public AbstractSpiderObject getFromRunningMap(String name) {
		return runningMap.get(name);
	}
	
	public void addToWaitingList(AbstractSpiderObject o) {
		waitingList.add(o);
	}
	
	public void removeFromWaitingList(AbstractSpiderObject o) {
		waitingList.remove(o);
	}
	
	public void removeFromRunningMap(AbstractSpiderObject o) {
		runningMap.remove(o.getId());
	}
}
