package org.epiclouds.main;

import org.epiclouds.spider.TerapeakManageSpider;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;

public class TerapeakSpiderStart {
		
		public static void main(String[] args) throws Exception {
			Bootstrap bt=new Bootstrap();
			bt.setBootSpiderClass(TerapeakManageSpider.class).start();
		}

}
