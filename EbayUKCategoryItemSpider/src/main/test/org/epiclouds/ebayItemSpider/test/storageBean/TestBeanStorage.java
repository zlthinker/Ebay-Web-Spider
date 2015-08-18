package org.epiclouds.ebayItemSpider.test.storageBean;

import org.epiclouds.ebayItemSpider.bean.EbayItemBean;
import org.epiclouds.ebayItemSpider.spider.CategorySpider;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;

public class TestBeanStorage {
	private static Bootstrap boot;
	public static void main(String[] args) throws Exception {
		boot=new Bootstrap();
		boot.setBootSpiderClass(CategorySpider.class);
		boot.start();
		EbayItemBean bean=new EbayItemBean();
		bean.setCategoryId("123456");
	}
}
