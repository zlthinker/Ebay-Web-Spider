
package org.epiclouds.spiders.spiderobject.abstracts;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.annotations.SpiderFeildConfig;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;
import org.epiclouds.spiders.spiderobject.manager.abstracts.AbstractSpiderObjectManager;
import org.epiclouds.spiders.spiderobject.manager.abstracts.ManagerCommandBean;
import org.epiclouds.spiders.spiderobject.manager.abstracts.SpiderManagerBean;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class AbstractSpiderObject implements SpiderObjectInterface , Delayed{
	private volatile String id=UUID.randomUUID().toString();
	@JSONField(serialize=false,deserialize=false)
	private  AtomicInteger                totalSpiderNum=new AtomicInteger(1);
	@JSONField(serialize=false,deserialize=false)
	private  AtomicInteger                finishSpiderNum=new AtomicInteger(0);
	@JSONField(serialize=false,deserialize=false)
	private volatile AbstractSpiderObject         parent;
	@JSONField(serialize=false,deserialize=false)
	private volatile Set<AbstractSpiderObject> children=new HashSet<AbstractSpiderObject>();
	
	private volatile long                         startTime;
	private  AtomicLong                   finishTime=new AtomicLong(0);
	@SpiderFeildConfig(desc="爬虫的运行时间间隔，秒")
	private volatile long                         delta;
	@JSONField(serialize=false,deserialize=false)
	private volatile  AbstractHandler             spiderHandler;
	@JSONField(serialize=false,deserialize=false)
	private volatile AbstractSpiderObjectManager           manager=Bootstrap.getSingle().getSpiderManager();
	@JSONField(serialize=false,deserialize=false)
	private AtomicBoolean isrun=new AtomicBoolean(false);
	@JSONField(serialize=false,deserialize=false)
	private AbstractConfigManager configManager=Bootstrap.getSingle().getAcfm();
	@JSONField(serialize=false,deserialize=false)
	private DBMangerInterface dbmanager =Bootstrap.getSingle().getDbm();

	public AbstractSpiderObjectManager getManager() {
		return manager;
	}

	public void setManager(AbstractSpiderObjectManager manager) {
		this.manager = manager;
	}

	public AbstractConfigManager getConfigManager() {
		return configManager;
	}

	public void setConfigManager(AbstractConfigManager configManager) {
		this.configManager = configManager;
	}

	public DBMangerInterface getDbmanager() {
		return dbmanager;
	}

	public void setDbmanager(DBMangerInterface dbmanager) {
		this.dbmanager = dbmanager;
	}

	public AbstractSpiderObject(){}

	public AbstractSpiderObject(AbstractSpiderObject parent){
		this.parent=parent;
	}
	/**
	 * 
	 */
	public  SpiderObjectBean toSpiderObjectBean(){
		SpiderObjectBean bean=new SpiderObjectBean();
		bean.setFinish_time(finishTime.get());
		bean.setIsrun(isrun.get());
		bean.setName(id);
		bean.setSpided_num(finishSpiderNum.get());
		bean.setStart_time(startTime);
		bean.setTotal_num(totalSpiderNum.get());
		bean.setInfo(getInfo());
		return bean;
	}


	public boolean isrun(){
		return isrun.get();
	}
	


	@Override
	public  void start() throws Exception {
		if(!isrun.compareAndSet(false, true)){
			throw new Exception("the spider is running!");
		}
		if(manager.getRunThread()==Thread.currentThread()){
			startTime=System.currentTimeMillis();
			spiderHandler=createSpiderHandler();
			if(spiderHandler!=null){
				spiderHandler.start();
			}
		}else{
			throw new Exception("not expected thread!");
		}
	}

	@Override
	public  void stop() throws Exception{
		if(!isrun.compareAndSet(true, false)){
			throw new Exception("the spider is not running!");
		}
		if(manager.getRunThread()==Thread.currentThread()){
			if(spiderHandler!=null){
				spiderHandler.stop();
			}
			for(AbstractSpiderObject child:children){
				if(child.isrun()){
					try{
						child.stop();
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					manager.removeRunningMapOrWaitingList(child);
				}
			}
			finishAction();
		}else{
			throw new Exception("not expected thread!");
		}
	}
	
	
	
	
	//itself really finish
	@Override
	public  void finish() {
		if(manager.getRunThread()==Thread.currentThread()){
			if(parent!=null){
				if(parent.getIsrun().get()
						&&parent.getFinishSpiderNum().incrementAndGet()>=parent.getTotalSpiderNum().get()){
					parent.finish();
				}
				finishAction();
			}else{
				if(isrun.compareAndSet(true, false)){
					finishAction();
				}
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(ManagerCommandBean.Command.FINISH, id);
			mm.setParent(this);
			manager.getCommandQue().add(mm);
		}
	}
	
	//do something when finish
	private void finishAction(){
		finishTime.set(System.currentTimeMillis());
		this.children.clear();
		this.spiderHandler=null;
		manager.removeRunningMapOrWaitingList(this);
		if(this.parent==null){
			try {
				AbstractSpiderObject o=cloneSelf();
				manager.update(this.getId(),o);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private AbstractSpiderObject cloneSelf() throws Exception{
		AbstractSpiderObject aso=(AbstractSpiderObject) this.getClass().newInstance();
		Class tmp=this.getClass();
		while(tmp!=null){
			Field[] fs=tmp.getDeclaredFields();
			for(Field f:fs){
				SpiderFeildConfig config=f.getAnnotation(SpiderFeildConfig.class);
				if(config!=null){
					char[] chars=f.getName().toCharArray();
					chars[0]=Character.toUpperCase(chars[0]);
					Method mm=tmp.getMethod("get"+new String(chars));
					Method mm2=tmp.getMethod("set"+new String(chars),String.class);
					if(mm.invoke(this)!=null)
						mm2.invoke(aso,mm.invoke(this).toString());
				}
			}
			tmp=tmp.getSuperclass();
		}
		return aso;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return id.equals(((AbstractSpiderObject)obj).getId());
	}

	@Override
	public int compareTo(Delayed o) {
		AbstractSpiderObject oo=(AbstractSpiderObject)o;
		if(oo.delta==-1){
			return -1;
		}
		if(this.delta==-1){
			return 1;
		}
		if(oo.finishTime.get()==0){
			return 1;
		}
		if(finishTime.get()==0){
			return -1;
		}
		
		long thisdelta=this.getDelay(TimeUnit.SECONDS);
		long otherdelta=oo.getDelay(TimeUnit.SECONDS);
		if(thisdelta>otherdelta){
			return 1;
		}
		if(thisdelta<otherdelta){
			return -1;
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		if(this.finishTime.get()==0){
			return 0;
		}
		if(this.delta==-1){
			return Long.MAX_VALUE;
		}

		long thisdelta=(long) (this.delta==0?configManager.getValue("delta",Integer.class):this.delta);
		long nowdelta=System.currentTimeMillis()-finishTime.get();
		return unit.convert(TimeUnit.MILLISECONDS.convert(thisdelta,TimeUnit.SECONDS)-nowdelta, TimeUnit.MILLISECONDS);
	}
	
	
	public  void storage() throws Exception{
		if(this.manager.getRunThread()==Thread.currentThread()){
			StorageBean.Builder bu=StorageBean.Builder.newBuilder(StorageBean.OperationType.UPORINSERT
					,configManager.getValue("spider_database",String.class),
					configManager.getValue("spider_meta_table",String.class)).addConditon(
							new EqualCondition<String>("id", id));
			dbmanager.storage(bu, this);
		}else{
			throw new Exception("not expected thread!");
		}
	}


	public  void addChild(AbstractSpiderObject spiderObject) {
		if(this.manager.getRunThread()==Thread.currentThread()){
			if(this.isrun.get()){
				this.children.add(spiderObject);
				this.totalSpiderNum.set(this.children.size());
				manager.getManagerBeans().putIfAbsent(spiderObject.getClass(),
						new SpiderManagerBean(
								spiderObject.getClass(),
								configManager.getValue(spiderObject.getClass().getSimpleName()+
						".runNumberThreshold", Integer.class)));
				manager.getManagerBeans().get(spiderObject.getClass()).addToWaitingList(spiderObject);
			}
		}else{
			ManagerCommandBean mm=new ManagerCommandBean(
					ManagerCommandBean.Command.ADDCHILD, id);
			mm.setChildOrOther(spiderObject);
			mm.setParent(this);
			manager.getCommandQue().add(mm);
		}
		
	}

	public AtomicLong getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(AtomicLong finishTime) {
		this.finishTime = finishTime;
	}
	public AtomicInteger getTotalSpiderNum() {
		return totalSpiderNum;
	}


	public void setTotalSpiderNum(AtomicInteger totalSpiderNum) {
		this.totalSpiderNum = totalSpiderNum;
	}
	
	public void setTotalSpiderNum(String totalSpiderNum) {
		this.totalSpiderNum.set(Integer.parseInt(totalSpiderNum));;
	}


	public AtomicInteger getFinishSpiderNum() {
		return finishSpiderNum;
	}


	public void setFinishSpiderNum(AtomicInteger finishSpiderNum) {
		this.finishSpiderNum = finishSpiderNum;
	}

	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}




	public long getDelta() {
		return delta;
	}


	public void setDelta(long delta) {
		this.delta = delta;
	}
	public void setDelta(String delta) {
		this.delta=Long.parseLong(delta);
	}


	public AbstractHandler getSpiderHandler() {
		return spiderHandler;
	}


	public void setSpiderHandler(AbstractHandler spiderHandler) {
		this.spiderHandler = spiderHandler;
	}


	public void setParent(AbstractSpiderObject parent) {
		this.parent = parent;
	}
	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public void setFinishTime(long time){
		finishTime.set(time);
	}

	public AbstractSpiderObject getParent(){
		return parent;
	}

	public AtomicBoolean getIsrun() {
		return isrun;
	}

	public void setIsrun(AtomicBoolean isrun) {
		this.isrun = isrun;
	}

}
