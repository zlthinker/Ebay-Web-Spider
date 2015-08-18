package org.epiclouds.spiders.spiderobject.abstracts;

import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;

/**
 * 
 * @author xianglong
 * @created 2015年6月2日 下午3:24:02
 * @version 1.0
 */
public interface SpiderObjectInterface {
	/**
	 * start the spider object
	 */
	public void start()throws Exception;
	/**
	 * stop the spider object
	 */
	public void stop()throws Exception;
	
	/**
	 * when the spider finish, called by spider if finished normally(not stopped by user)
	 */
	public void finish();
	
	/**
	 * Create the spiderHandle using information of spiderObject.
	 * The handler must be implemented by user.
	 */
	public AbstractHandler createSpiderHandler();
	/**
	 * to spider object bean
	 * @return spider object bean
	 */
	public SpiderObjectBean toSpiderObjectBean();
	/**
	 * the spider is running
	 * @return
	 */
	public boolean isrun();
	/**
	 * get the spider's info
	 * @return
	 */
	public String getInfo();
}
