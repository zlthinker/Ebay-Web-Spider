package org.epiclouds.spiders.spiderbean.util;

/**
 * The spider object state info bean
 * @author xianglong
 *
 */
public class SpiderObjectBean {
	/**
	 * the name of sprider object, unique
	 */
	private String name;
	/**
	 * the spider object's total number should crawls.
	 */
	private int total_num;
	/**
	 * finished number
	 */
	private int spided_num;
	/**
	 * finished time, utc time
	 */
	private long finish_time;
	/**
	 * whether the spider is running
	 */
	private boolean isrun;
	/**
	 * start time, utc time
	 */
	private long start_time;
	/**
	 * 
	 */
	private String info;
	
	public SpiderObjectBean(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTotal_num() {
		return total_num;
	}
	public void setTotal_num(int total_num) {
		this.total_num = total_num;
	}
	public int getSpided_num() {
		return spided_num;
	}
	public void setSpided_num(int spided_num) {
		this.spided_num = spided_num;
	}
	public long getFinish_time() {
		return finish_time;
	}
	public void setFinish_time(long finish_time) {
		this.finish_time = finish_time;
	}
	public boolean isIsrun() {
		return isrun;
	}
	public void setIsrun(boolean isrun) {
		this.isrun = isrun;
	}
	public long getStart_time() {
		return start_time;
	}
	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
