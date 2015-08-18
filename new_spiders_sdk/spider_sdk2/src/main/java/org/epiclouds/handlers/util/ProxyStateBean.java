package org.epiclouds.handlers.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.Resource;

import org.epiclouds.spiders.config.manager.abstracts.ConfigManagerInterface;

/**
 * proxy status
 * @author xianglong
 *
 */
public class ProxyStateBean {
	/**
	 * socket address
	 */
	private volatile SocketAddress addr;
	/**
	 * authority info
	 */
	private volatile String authStr;
	/**
	 * is using by one thread
	 */
	private volatile boolean using=false;
	/**
	 * the error info
	 */
	private volatile String errorInfo=null;
	
	@Resource
	private ConfigManagerInterface cmi;

	public ProxyStateBean(SocketAddress addr,String authStr){
		this.setAddr(addr);
		this.setAuthStr(authStr);
	}
	public ProxyStateBean(){
		this.setAddr(new InetSocketAddress(cmi.getValue("proxy_addr", String.class), 
				cmi.getValue("proxy_port", Integer.class)));
		this.setAuthStr(cmi.getValue("auth_String", String.class));
	}
	public boolean isUsing() {
		return using;
	}

	public void setUsing(boolean using) {
		this.using = using;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	public SocketAddress getAddr() {
		return addr;
	}
	public void setAddr(SocketAddress addr) {
		this.addr = addr;
	}
	public String getAuthStr() {
		return authStr;
	}
	public void setAuthStr(String authStr) {
		this.authStr = authStr;
	}
	
	
}
