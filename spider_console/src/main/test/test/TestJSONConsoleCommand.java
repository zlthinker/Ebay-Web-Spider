package test;

import java.util.LinkedList;
import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class TestJSONConsoleCommand {

	@Test
	public void testMain() throws Exception{
		List<SpiderObjectBean> ll=new LinkedList<SpiderObjectBean>();
		for(int i=0;i<5;i++){
			SpiderObjectBean cb=new SpiderObjectBean();
			cb.setName("ebay store"+i);
			cb.setIsrun(false);
			cb.setTotal_num(100);
			cb.setSpided_num(10+i);
			ll.add(cb);
		}
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(AbstractConsoleCommandHandler.ConsoleCommand.ADD);
		ccb.setOb(ll);
		String str=JSONObject.toJSONString(ccb);
		System.err.println(str);
		ConsoleCommandBean ccb2=JSONObject.parseObject(str, ConsoleCommandBean.class);
		List<SpiderObjectBean> ll2=JSONObject.parseArray(JSONObject.toJSONString(ccb2.getOb()), SpiderObjectBean.class);
		System.err.println(str);
	}

}
