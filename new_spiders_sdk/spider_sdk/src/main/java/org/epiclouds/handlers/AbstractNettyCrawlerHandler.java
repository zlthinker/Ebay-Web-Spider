/**
 * @author Administrator
 * @created 2014 2014骞�12鏈�1鏃� 涓嬪崍2:35:09
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

import org.epiclouds.handlers.util.ProxyStateBean;
import org.epiclouds.spiders.bootstrap.imp.Bootstrap;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

/**
 * 
 * @author xianglong
 * @created 2015年6月10日 下午2:59:25
 * @version 1.0
 */
public abstract class AbstractNettyCrawlerHandler extends AbstractHandler{
	
	private volatile Channel channel;
	

	public AbstractNettyCrawlerHandler(
			String host,
			String url,
			AbstractSpiderObject spider){
		super(spider);
		this.host=host;
		this.url=url;
	}


	
	public void stop(){
		if(isrun.compareAndSet(true, false)){
			EventLoop el=null;
			if(channel!=null){
				el=channel.eventLoop();
			}
			close();
			if(el!=null&&el.inEventLoop()){
				if(this.getState()==HandlerResultState.SUCCESS){
					onDataFinished();
				}
				onNormalFinished();
			}else{
				onForceFinished();
			}
		}else{
			close();
		}
	}
	
	public void start(){
		if(this.isrun.get()==false){
			System.err.println("cannot run a stopped handler");
			return;
		}
		Bootstrap.getSingle().getCc().execute(this);
	}
	
	protected void startToRun(){
		if(this.isrun.get()==false){
			System.err.println("cannot run a stopped handler");
			return;
		}
		onBefore();
		continueSpider();
	}
	
	
	protected void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,String schema){
		this.url=url;
		this.md=hm;
		this.headers=headers;
		this.postdata=postdata;
		this.schema=schema;
		requestSelf();
	}
	
	protected void continueSpider(){
		if(this.isrun.get()){
			requestSelf();
		}else{
			this.stop();
		}
	}
	
	protected void requestSelf(){
		if(this.channel==null||!this.channel.isActive()){
			return;
		}
		FullHttpRequest req2=null;
		if(this.proxyaddr==null){
			req2=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, this.md, url);
			req2.headers().add("Connection","Keep-Alive");
			
		}else{
			req2=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, this.md, schema+"://"+this.host+url);
			if(this.getProxyaddr().getAuthStr()!=null){
				try {
					req2.headers().add("Proxy-Authorization", "Basic "
							+new String(Base64.getEncoder().encode(this.getProxyaddr().getAuthStr().getBytes("utf-8")),"utf-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		req2.headers().add("Host",this.host+":"+("http".equals(schema)?80:443));
		req2.headers().add("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
		if(headers!=null){
			for(String key:headers.keySet()){
				req2.headers().add(key,headers.get(key));
			}
		}
		HttpPostRequestEncoder encoder=null;
		
		if(this.md.compareTo(HttpMethod.POST)==0&&postdata!=null){
			try {
				HttpRequest req=req2;
				if(postdata.size()==1&&postdata.get(null)!=null){
					req2.content().writeBytes(postdata.get(null).getBytes());
					req2.headers().addInt("Content-Length", req2.content().readableBytes());
				}else{
					//not support file upload
					encoder = new HttpPostRequestEncoder(req2, ismultipart);
					for(String key:postdata.keySet()){
						encoder.addBodyAttribute(key, postdata.get(key));
					}
					req= encoder.finalizeRequest();
				}
				channel.writeAndFlush(req);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
		}else{
			req2.headers().addInt("Content-Length", req2.content().readableBytes());
			channel.writeAndFlush(req2);
		}
	}
	

	


	protected void runOnce(FullHttpResponse r) {
		if(isrun.get()){
				try{
					if(r!=null){
						try{
							this.handleResponse2(r);
						}finally{
							r.release();
						}
					}else{
						errorNum++;
						this.close();
					}
					if(maxErrorNum!=0&&errorNum>maxErrorNum){
						this.state=HandlerResultState.ERROR;
						stop();
					}
				}catch(Exception e){
					e.printStackTrace();
					try{
						errorNum++;
						if(maxErrorNum!=0&&errorNum>maxErrorNum){
							this.state=HandlerResultState.ERROR;
							stop();
							return;
						}
						CrawlerClient.mainlogger.error(this.url,e);
						this.requestSelf();
					}catch(Exception e1){
						
					}
				}
			}
	}
	private void handleResponse2(FullHttpResponse fullHttpResponse) throws Exception{
		int status=fullHttpResponse.status().code()/100;
		if(status==2){
			try{
				handle(fullHttpResponse.content().toString(Charset.forName(charset)));
			}catch(Exception e){
				CrawlerClient.mainlogger.error(this.url,e);
				e.printStackTrace();
				throw e;
			}
			return;
		}else{
			errorNum++;
			onError(fullHttpResponse);
		}
		
	}
	protected void onError(Object response){
		FullHttpResponse fullHttpResponse=(FullHttpResponse)response;
		int status=fullHttpResponse.status().code()/100;
		CrawlerClient.mainlogger.error("error:"+fullHttpResponse.status()+":"+this.getUrl());
		
		if(status==3){
			if((fullHttpResponse.headers().get("Location")+"").contains("err")
					||(fullHttpResponse.headers().get("Location")+"").contains("error")
					||(fullHttpResponse.headers().get("Location")+"").contains("sec")
					||(fullHttpResponse.headers().get("Location")+"").contains("security")){
				return;
			}
			this.setUrl(fullHttpResponse.headers().get("Location")+"");
			this.requestSelf();
			return;
		}
		if(status==4||status==5||status==1){
			this.requestSelf();
			return;
		}
	}
	
	

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	
	private void close(){
		try{
			if(this.channel!=null&&this.channel.isActive()){
				this.channel.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
