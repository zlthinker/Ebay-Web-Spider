
package org.epiclouds.spiders.spiderobject.manager.abstracts;

/**
 * the spiderObjectManager interface implement
 * @author ZhuYicong 
 */
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.epiclouds.message.manager.impl.MessageManager;
import org.epiclouds.spiders.annotations.SpiderFeildConfig;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.data.impl.DBDataEntry;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;





@Component(value="singlespidermanager")
public  class SingltonSpiderObjectManager extends AbstractSpiderObjectManager{
	private static Logger mainlogger = LoggerFactory.getLogger(SingltonSpiderObjectManager.class);
	public Class<?> getClassz() {
		return classz;
	}
	public void setClassz(Class<? extends AbstractSpiderObject> classz){
		this.classz=classz;
		System.out.println(this.classz.getSimpleName());
		this.managerBeans.put(classz, new SpiderManagerBean(
				classz,
				configManager.getValue(this.classz.getSimpleName()+".runNumberThreshold", Integer.class)));
	}

	public SingltonSpiderObjectManager(){		
	}

	public AbstractSpiderObject createSpiderObject(List<AddSpiderObjectBean> values) throws Exception{
		AbstractSpiderObject asb=(AbstractSpiderObject) classz.newInstance();
		for(AddSpiderObjectBean bean:values){
			char[] chars=bean.getName().toCharArray();
			chars[0]=Character.toUpperCase(chars[0]);
			Method mm=classz.getMethod("set"+new String(chars),String.class);
			mm.invoke(asb, bean.getValue());
		}
		return asb;
	}
	
	public  List<AbstractSpiderObject> getAllSpiderObjects(){
		List<AbstractSpiderObject> re=new LinkedList<AbstractSpiderObject>();
		synchronized(this){
			re.addAll(totalMap.values());
		}
		return re;
	}
	
	public List<AddSpiderObjectBean> getSpiderObjectConfig(String name) throws Exception{
			AbstractSpiderObject oo=null;
			if(name!=null){
				oo=totalMap.get(name);
				if(oo==null){
					throw new Exception("the spider is not absent");
				}
			}
			//if totalMap.containKey(name) is false
			List<AddSpiderObjectBean> re=new LinkedList<AddSpiderObjectBean>();
			Class tmp=classz;
			while(tmp!=null){
				Field[] fs=tmp.getDeclaredFields();
				for(Field f:fs){
					SpiderFeildConfig config=f.getAnnotation(SpiderFeildConfig.class);
					if(config!=null){
						AddSpiderObjectBean aso=new AddSpiderObjectBean();
						aso.setDesc(config.desc());
						aso.setName(f.getName());
						if(oo!=null){
							char[] chars=f.getName().toCharArray();
							chars[0]=Character.toUpperCase(chars[0]);
							Method mm=tmp.getMethod("get"+new String(chars));
							aso.setValue(mm.invoke(oo).toString());
						}
						re.add(aso);
					}
				}
				tmp=tmp.getSuperclass();
			}
			if(oo!=null){
				AddSpiderObjectBean ids=new AddSpiderObjectBean();
				ids.setName("id");
				ids.setDesc("id，唯一");
				ids.setValue(oo.getId());
				re.add(ids);
			}
			return re;
	}

	@Override
	public void add(AbstractSpiderObject spiderObject) throws Exception{
		if(totalMap.containsKey(spiderObject.getId())){
			throw new Exception("The add spider already exsist");
		}
		if(this.getRunThread()==Thread.currentThread()){
			String name=spiderObject.getId();
			if(!totalMap.containsKey(name)){
				persistent(spiderObject);
				totalMap.put(name, spiderObject);
				managerBeans.get(spiderObject.getClass()).addToWaitingList(spiderObject);
			}
			else{
				throw new Exception("The add spider already exsist");
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.ADD, null);
			mm.setChildOrOther(spiderObject);
			this.getCommandQue().add(mm);
		}
	}
	//save the spiderObject to mongodb;
	private void persistent(AbstractSpiderObject spiderObject) throws Exception{
		spiderObject.storage();
	}
	

	
	@Override
	public void delete(String id) throws Exception{
		if(!totalMap.containsKey(id)){
			throw new Exception("The  spider not already exsist");
		}
		if(this.getRunThread()==Thread.currentThread()){
			if(managerBeans.get(classz).getFromRunningMap(id)==null){
				if(totalMap.containsKey(id)){
					delete2(id);
					managerBeans.get(classz).removeFromWaitingList(totalMap.remove(id));
				}else{
					throw new Exception("The spider is absent");
				}
			}else{
				throw new Exception("The spider is running,must stop it firstly!");
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.DELETE, id);
			this.getCommandQue().add(mm);
		}
	}
	
	//load the spiderObject from mongodb;
	private void delete2(String name) throws Exception{
		StorageBean.Builder sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.DELETE
				,configManager.getValue("spider_database",String.class),
				configManager.getValue("spider_meta_table",String.class)).addConditon(
						new EqualCondition("id", name));
		dbManager.storage(sb, null);
	}
	@Override
	public void start(String id) throws Exception{
		if(!totalMap.containsKey(id) ){
			throw new Exception("The spider is not exist");
		}
		if(this.getRunThread()==Thread.currentThread()){
			if(managerBeans.get(classz).getFromRunningMap(id)==null ){
				if(totalMap.containsKey(id) ){
					managerBeans.get(classz).removeFromWaitingList(totalMap.get(id));
					totalMap.get(id).setFinishTime(0);
					//if one spider is start,when the spiders recover from crash,it need start again
					persistent(totalMap.get(id));
					managerBeans.get(classz).addToWaitingList(totalMap.get(id));
				}else{
					throw new Exception("The spider is not exist");
				}
			}
			else{
				throw new Exception("The spider is running");
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.START, id);
			this.getCommandQue().add(mm);
		}
	}
	
	//start all the spiders in the waiting list,it may be diffent with which admin see in the console
	public void startAll() throws Exception{
		if(this.getRunThread()==Thread.currentThread()){
			for(String name:totalMap.keySet()){
				try{
					start(name);
				}catch(Exception e){
					mainlogger.error("start all in one:"+name+" error!", e);
				}
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.STARTALL,null);
			this.getCommandQue().add(mm);
		}
	}
	
	public void runOnce() {
		if(this.getRunThread()==Thread.currentThread()){
			if(!init){
				init=true;
				try {
					initFromStorage();
				} catch (Exception e) {
					mainlogger.error("initFromStorage error", e);
					System.exit(0);
				}
			}
			while(!this.commandQue.isEmpty()){
				ManagerCommandBean mcb=commandQue.poll();
				if(mcb!=null){
					try{
						switch(mcb.getCc()){
							case ADD: add(mcb.getChildOrOther()); break;
							case DELETE: delete(mcb.getId()); break;
							case START: start(mcb.getId()); break;
							case STARTALL: startAll(); break;
							case STOP: stop(mcb.getId()); break;
							case UPDATE: update(mcb.getId(),mcb.getChildOrOther()); break;
							case ADDCHILD: mcb.getParent().addChild(mcb.getChildOrOther());; break;
							case FINISH: mcb.getParent().finish(); break;
						
						}
					}catch(Exception e){
						mainlogger.error("initFromStorage error", e);
					}
				}
			}
			for(Class<?> clz:managerBeans.keySet()){
				managerBeans.get(clz).runOnce();
			}
		}
	}
	
	@Override
	public void stop(String id) throws Exception{
		if(!totalMap.containsKey(id)){
			throw new Exception("the spider is not exist!");
		}
		if(this.getRunThread()==Thread.currentThread()){
			if(totalMap.containsKey(id)){
				if(managerBeans.get(classz).getFromRunningMap(id)!=null){
					managerBeans.get(classz).getFromRunningMap(id).stop();
				}
				else{
					throw new Exception("the spider is not running!");
				}
			}
			else{
				throw new Exception("the spider is not exist!");
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.STOP,id);
			this.getCommandQue().add(mm);
		}
	}
	

	protected void initFromStorage() throws Exception {
		List<? extends AbstractSpiderObject> spiderObjectList=find();
		for(AbstractSpiderObject spiderObject:spiderObjectList){
			//initSpiderBean
			managerBeans.get(classz).addToWaitingList(spiderObject);
			totalMap.put(spiderObject.getId(), spiderObject);
		}
		
	}
	
	
	@Override
	public  void update(String name,AbstractSpiderObject spiderObject) throws Exception{
		if(!totalMap.containsKey(name)){
			throw new Exception("The spider is not exist");
		}
		if(this.getRunThread()==Thread.currentThread()){
			if(totalMap.containsKey(name)){
				//if the update spider is running
				if(managerBeans.get(classz).getFromRunningMap(name)==null){
					AbstractSpiderObject oldspiderObject=totalMap.get( name );
					spiderObject.setFinishTime(oldspiderObject.getFinishTime().get());
					spiderObject.setStartTime(oldspiderObject.getStartTime());
					delete2(name);
					spiderObject.storage();
					managerBeans.get(classz).removeFromWaitingList(totalMap.remove( name ));
					totalMap.put(spiderObject.getId(), spiderObject);
					managerBeans.get(classz).addToWaitingList(spiderObject);
				}else{
					throw new Exception("The spider is running");
				}
			}
			else{
				throw new Exception("The spider is not exist");
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.UPDATE,name);
			mm.setChildOrOther(spiderObject);
			this.getCommandQue().add(mm);
		}
		
	}
	public void removeRunningMapOrWaitingList(AbstractSpiderObject spider) {
		if(this.getRunThread()==Thread.currentThread()){
			managerBeans.get(spider.getClass()).removeFromRunningMap(spider);
			managerBeans.get(spider.getClass()).removeFromWaitingList(spider);
		}
	}
	
	//load the spiderObject from mongodb;
	private List<? extends AbstractSpiderObject> find() throws Exception{
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.FIND
				,configManager.getValue("spider_database",String.class),
				configManager.getValue("spider_meta_table",String.class)).build();
		return dbManager.find(sb,classz);
	}
	
	


}

