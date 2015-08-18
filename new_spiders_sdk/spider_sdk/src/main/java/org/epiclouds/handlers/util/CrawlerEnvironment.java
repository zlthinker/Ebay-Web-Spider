/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午2:49:56
 * @version 1.0
 */
package org.epiclouds.handlers.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 *
 */
public class CrawlerEnvironment{
	public static Executor pool=Executors.newFixedThreadPool(40);
}
