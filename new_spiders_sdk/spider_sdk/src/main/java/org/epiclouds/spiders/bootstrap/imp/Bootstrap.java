package org.epiclouds.spiders.bootstrap.imp;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.epiclouds.handlers.util.ProxyStateBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Bootstrap implements Runnable{
	private  ApplicationContext context;
	private  Class<? extends AbstractSpiderObject> cls;
	private String spring_file="spring.xml";
	private static BootstrapHelper bootstrapHelper=new BootstrapHelper();
	
	public Bootstrap(){
	}
	public  Bootstrap setBootSpiderClass(Class<? extends AbstractSpiderObject> cls){
		this.cls=cls;
		return this;
	}
	
	public  void start() throws Exception{
		if(context!=null){
			throw new Exception("Bootstrap have run!");
		}
		if(cls==null){
			throw new Exception("Boot Class should  not be null!");
		}
		context=new ClassPathXmlApplicationContext(spring_file);
		bootstrapHelper=context.getBean(BootstrapHelper.class);
		context.getBean(BootstrapHelper.class).getSpiderManager().setClassz(cls);
		context.getBean(BootstrapHelper.class).getSpiderManager().initFromStorage();
		if(this.bootstrapHelper.getAcfm().getValue("proxy_addr", String.class)!=null){
			this.bootstrapHelper.setPxy(new ProxyStateBean(new 
				InetSocketAddress(this.bootstrapHelper.getAcfm().getValue("proxy_addr", String.class), 
						this.bootstrapHelper.getAcfm().getValue("proxy_port", Integer.class)),
						this.bootstrapHelper.getAcfm().getValue("auth_String", String.class)));
		}
		Executors.newSingleThreadExecutor().execute(this);
	}

	public static BootstrapHelper getSingle(){
		return bootstrapHelper;
	}
	@Override
	public void run() {
		while(true){
			try{
				context.getBean(BootstrapHelper.class).getSpiderManager().runOnce();
				Thread.sleep(5000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	
}
