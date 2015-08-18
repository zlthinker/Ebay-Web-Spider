/**
 * @author Administrator
 * @created 2014 2014年12月16日 下午12:08:15
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epiclouds.handlers.util.ProxyStateBean;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;
import org.epiclouds.spiders.dbstorage.data.impl.DBDataEntry;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHandler implements SpiderHandlerInterface{

	protected AtomicBoolean isrun=new AtomicBoolean(true);
	
	protected Thread thisThread=null;

	protected volatile HandlerResultState state=HandlerResultState.SUCCESS;
	
	protected volatile String schema="http";
	protected volatile String host;
	protected volatile String url;
	protected volatile HttpMethod md=HttpMethod.GET;
	protected volatile Map<String,String> headers=new HashMap<String, String>();
	protected volatile Map<String,String> postdata=new HashMap<String, String>();
	protected volatile String charset="utf-8";
	protected volatile ProxyStateBean proxyaddr=Bootstrap.getSingle().getPxy();
	protected volatile boolean ismultipart=false;

	protected volatile int errorSleepTime=20*1000;
	protected volatile int maxErrorNum=0;
    protected volatile int errorNum=0;
    
    private volatile AbstractSpiderObject spider;
	
    /**
     * This function runs after start and before any other actions
     */
    protected abstract void onBefore();
    /**
     * This method runs when the spider finished normally(not stopped by user)
     */
    protected abstract void onNormalFinished();
	/**
	 * This methods runs before onnormalfinished method(not stopped by user)
	 * Used to store data to database if exist
	 */
	protected abstract void onDataFinished();
	/**
	 * This method runs when it does not finished normally(stopped by user).
	 */
	protected  void onForceFinished(){
		
	}
	protected abstract void onError(Object response);
	/**
	 * start the handler
	 */
	public abstract void start();

	public AbstractHandler(AbstractSpiderObject spider){
		this.setSpider(spider);
	}
	protected abstract void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,
			String schema) throws Exception;
	/**
	 * stop the handler
	 */
	public void stop(){
		if(this.isrun.compareAndSet(true, false)){
			if(thisThread==Thread.currentThread()){
				if(this.getState()==HandlerResultState.SUCCESS){
					onDataFinished();
				}
				onNormalFinished();
			}else{
				onForceFinished();
			}
		}
	}
	
	public ProxyStateBean getProxyaddr() {
		return proxyaddr;
	}

	public void setProxyaddr(ProxyStateBean proxyaddr) {
		this.proxyaddr = proxyaddr;
	}
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}



	public int getErrorNum() {
		return errorNum;
	}

	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}

	public int getMaxErrorNum() {
		return maxErrorNum;
	}

	public void setMaxErrorNum(int maxErrorNum) {
		this.maxErrorNum = maxErrorNum;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getErrorSleepTime() {
		return errorSleepTime;
	}

	public void setErrorSleepTime(int errorSleepTime) {
		this.errorSleepTime = errorSleepTime;
	}

	public String getHost() {
		return host;
	}

	public HttpMethod getMd() {
		return md;
	}

	public void setMd(HttpMethod md) {
		this.md = md;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getPostdata() {
		return postdata;
	}

	public void setPostdata(Map<String, String> postdata) {
		this.postdata = postdata;
	}

	public void setHost(String host) {
		this.host = host;
	}
	public HandlerResultState getState() {
		return state;
	}

	public void setState(HandlerResultState state) {
		this.state = state;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if(url.startsWith("http://")||url.startsWith("Http://")){
			this.url=url.substring(url.indexOf("/", 7));
			return;
		}
		this.url = url;
	}

	public AtomicBoolean getIsrun() {
		return isrun;
	}

	public void setIsrun(AtomicBoolean isrun) {
		this.isrun = isrun;
	}
	public AbstractSpiderObject getSpider() {
		return spider;
	}
	public void setSpider(AbstractSpiderObject spider) {
		this.spider = spider;
	}
	public  enum HandlerResultState{
		SUCCESS,
		FAILED,
		ERROR
	}
	
	public boolean isIsmultipart() {
		return ismultipart;
	}
	public void setIsmultipart(boolean ismultipart) {
		this.ismultipart = ismultipart;
	}
	
}
