package org.epiclouds.spiders.dbstorage.manager.abstracts;

import java.util.List;

/**
 * 
 * @author xianglong
 * @created 2015年6月5日 下午3:24:35
 * @version 1.0
 */
public interface DBMangerInterface {
	/**
	 * find java beans
	 * @param sb
	 * @param cls
	 * @return
	 * @throws Exception
	 */
	public<T> List<T> find(StorageBean sb,Class<T> cls) throws Exception;
	/**
	 * execute delete、insert、update、updateorinsert
	 * @param builder
	 * @param data
	 * @throws Exception
	 */
	public void storage(StorageBean.Builder builder,Object data) throws Exception;
	

}
