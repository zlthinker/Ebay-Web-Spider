package test;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.main.RunTera;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.junit.Test;
import org.epiclouds.message.manager.impl.MessageManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TestRuntera {

	@Test
	public void testMain() throws Exception{
		for(int i=0;i<5;i++){
			SpiderClassBean cb=new SpiderClassBean();
			cb.setDesc("你好");
			cb.setName("ok"+i);
			cb.setQueue_name("ok"+i);
			SpiderStatusManager.putBackSpider(cb);	
		}


		
//		RunTera.main(null);
		ApplicationContext context=new ClassPathXmlApplicationContext("spring.xml");
		//MessageManager mm = new MessageManager();
		Thread.sleep(1000*1000);


	}

}
