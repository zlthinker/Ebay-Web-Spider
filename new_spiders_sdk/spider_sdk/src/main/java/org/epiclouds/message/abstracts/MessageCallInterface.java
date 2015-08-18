package org.epiclouds.message.abstracts;

/**
 * the message call operations
 * @author xianglong
 * @created 2015年6月4日 上午10:57:36
 * @version 1.0
 */
public interface MessageCallInterface {
	/**
	 * get the message from theb source
	 * @param sources
	 * @return the message
	 */
	public String getMessage(String... sources);
	/**
	 * send message to a destination
	 * @param destination
	 * @param others
	 */
	public void sendMessage(String destination,String id,Object... others);
}
