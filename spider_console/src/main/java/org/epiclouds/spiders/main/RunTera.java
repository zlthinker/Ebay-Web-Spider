package org.epiclouds.spiders.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.storage.imp.MongoManager;
import org.epiclouds.spiders.storage.imp.StorageBean;
import org.epiclouds.spiders.storage.imp.StorageBean.OperationType;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.epiclouds.spiders.util.Para;
import org.epiclouds.spiders.webconsole.AddSpiderObject;
import org.epiclouds.spiders.webconsole.DeleteSpiderObject;
import org.epiclouds.spiders.webconsole.Login;
import org.epiclouds.spiders.webconsole.Logout;
import org.epiclouds.spiders.webconsole.RemoveSpiderClass;
import org.epiclouds.spiders.webconsole.StartAllSpiderObject;
import org.epiclouds.spiders.webconsole.StartSpiderObject;
import org.epiclouds.spiders.webconsole.StopSpiderObject;
import org.epiclouds.spiders.webconsole.UpdateSpiderObject;
import org.epiclouds.spiders.webconsole.UpdateRunningConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class RunTera {
	public static ApplicationContext context;

	private static Logger mainlogger = LoggerFactory.getLogger(RunTera.class);
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		init();
	}

	private static void init(){
		try{
			getConfig();
			context=new ClassPathXmlApplicationContext("spring.xml");
			System.out.println("It's going to initialize StorageBeans");
			initStorageBeans();
			System.out.println("It's going to start inner jetty");
			startInnerJetty();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	  /**
	    * get config from file
	 * @throws Exception 
	    */
	private static void getConfig() throws Exception{
		Properties pros=new Properties();
		pros.load(new InputStreamReader(new FileInputStream("config"), "UTF-8"));
		for(Object key:pros.keySet()){
			Para p=new Para();
			String value=pros.getProperty((String)key);
			String[] values=value.split(" ");
			 p.setName((String)key);
			if(values!=null){
				if(values.length>=1) p.setValue(values[0]);
				if(values.length>=2) p.setDesc(new String(values[1].getBytes(),"utf-8"));
				if(values.length>=3) p.setRunning(Boolean.parseBoolean(values[2]));
			}
			ConsoleConfig.getParas().put((String)key, p);
		}
	}
	/**
	 * 
	 * @param m
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void initStorageBeans() throws IOException, InterruptedException {
		StorageBean sb=new StorageBean();
		DBObject con=new BasicDBObject();
		sb.setCondition(con);
		sb.setDbstr(ConsoleConfig.getStore_database());
		sb.setTablestr(ConsoleConfig.getMeta_table());
		sb.setType(OperationType.FIND);
		List<DBObject> re=MongoManager.getManager().getObjects(sb);
		for(DBObject db:re){
			String data=(String)db.get("data");
			SpiderClassBean bean=JSONObject.parseObject(data, SpiderClassBean.class);
			SpiderStatusManager.putBackSpider(bean);
		}
	}
	
	


	/**
	 * start the innner server
	 * 
	 * @throws Exception
	 */
	public static void startInnerJetty() throws Exception {
		mainlogger.info("Start web console in " + ConsoleConfig.getJettyport());
		Server server = new Server();
		try {

			Connector connector = new SelectChannelConnector();
			connector.setPort(ConsoleConfig.getJettyport());
			server.setConnectors(new Connector[] { connector });
			WebAppContext webapp = new WebAppContext();
			webapp.setContextPath("/");// url is /jettytest
			webapp.setResourceBase("./WebRoot");// the folder
			webapp.addServlet(Login.class, "/login");
			webapp.addServlet(StartSpiderObject.class, "/startSpiderObject");
			webapp.addServlet(StartAllSpiderObject.class, "/startAllSpiderObject");
			webapp.addServlet(Logout.class, "/logout");
			webapp.addServlet(StopSpiderObject.class, "/stopSpiderObject");
			webapp.addServlet(UpdateRunningConfig.class, "/updateRunningConfig");
			webapp.addServlet(DeleteSpiderObject.class, "/deleteSpiderObject");
			webapp.addServlet(AddSpiderObject.class, "/addSpiderObject");
			webapp.addServlet(RemoveSpiderClass.class, "/removeSpiderClass");
			webapp.addServlet(UpdateSpiderObject.class, "/updateSpiderObject");

			/*webapp.addServlet(new ServletHolder(new GetSourceType()),
			"/getSourceType");*/

			server.setHandler(webapp);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
			mainlogger.error("start web console error!", e);
			System.exit(1);
		}
		mainlogger.info("Start web console finished");
	}

}
