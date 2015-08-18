/**
 * @author Administrator
 * @created 2014 2014年12月16日 上午10:46:39
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.epiclouds.handlers.util.ProxyStateBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.joda.time.DateTime;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;


/**
 * @author Administrator
 *
 */
public abstract class AbstractHttpClientCrawlerHandler extends
		AbstractHandler implements Runnable  {

	private  ArrayBlockingQueue<CloseableHttpResponse> que=new ArrayBlockingQueue<>(10);
	
    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpclient ;
	
    private int timeout=30;
    

	public AbstractHttpClientCrawlerHandler(ProxyStateBean proxyaddr
			,String host,String url,String schema,String charset,AbstractSpiderObject spider
			){
		super(spider);
		this.proxyaddr=proxyaddr;
		this.host=host;
		this.url=url;
		this.schema=schema;
		this.charset=charset;
	}
	

	
	private void reconnect() throws FailingHttpStatusCodeException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		if(httpclient==null){
			 SSLContext sslContext = SSLContexts.custom()
			            .loadTrustMaterial(null,new TrustStrategy() {


							@Override
							public boolean isTrusted(X509Certificate[] chain,
									String authType)
									throws CertificateException {
								// TODO Auto-generated method stub
								return true;
							}
			            })
			            .useTLS()
			            .build();
			 SSLConnectionSocketFactory connectionFactory =
			            new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());

	        // 从上述SSLContext对象中得到SSLSocketFactory对象
			 cookieStore = new BasicCookieStore();
			 httpclient = HttpClients.custom()
					 	.setSSLSocketFactory(connectionFactory)
			            .setDefaultCookieStore(cookieStore)
			            .build();
			requestSelf();
		}
	}
	
	private void handleResponse2(CloseableHttpResponse webResponse) throws Exception{
		int status=webResponse.getStatusLine().getStatusCode()/100;
		if(status==2){
			ByteBuf bb=null;
			try{
				//bb=Unpooled.wrappedBuffer(EntityUtils.toByteArray(webResponse.getEntity()));
				handle(EntityUtils.toString(webResponse.getEntity(), charset));
			}finally{
				if(bb!=null){
					bb.release();
				}
			}
			return;
		}
		System.err.println("error:"+webResponse.getStatusLine()+":"+this.getUrl());
		errorNum++;
		Thread.sleep(errorSleepTime);
		onError(webResponse);
		
	}
	public void onError(Object response){
		
	}
	public void close(){
		try {
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpclient=null;
	}


	
	private void requestSelf() throws ClientProtocolException, IOException {
			this.que.clear();
			HttpHost target = new HttpHost(host,schema.equals("https")?443:80, schema);
			org.apache.http.HttpRequest request = null;
			if(this.md.compareTo(HttpMethod.GET)==0){
				request = new HttpGet(url);
			}
			if(this.md.compareTo(HttpMethod.POST)==0){
				request = new HttpPost(url);
				if(this.postdata!=null){
					if(this.postdata.size()==1&&this.postdata.get(null)!=null){
						((HttpPost)request).setEntity(new StringEntity(this.postdata.get(null)));  
					}else{
						for(String k:this.getPostdata().keySet()){
							List<NameValuePair> nvps = new ArrayList <NameValuePair>();  
					          
					        Set<String> keySet = this.postdata.keySet();  
					        for(String key : keySet) {  
					            nvps.add(new BasicNameValuePair(key, this.postdata.get(key)));  
					        }  
					          
					        ((HttpPost)request).setEntity(new UrlEncodedFormEntity(nvps));  
						}
					}
				}
				
			}
			 RequestConfig config ;
			if(this.getProxyaddr()!=null){
	            HttpHost proxy = new HttpHost(((InetSocketAddress)this.getProxyaddr().getAddr()).getHostString(), 
	            		((InetSocketAddress)this.getProxyaddr().getAddr()).getPort(),
	            		"http");
	            config = RequestConfig.custom()
						 .setConnectionRequestTimeout(getTimeout()*1000)
						 .setConnectTimeout(getTimeout()*1000)
						 .setSocketTimeout(getTimeout()*1000)
		                 .setProxy(proxy)
	                    .build();
			}else{
				config = RequestConfig.custom()
						 .setConnectionRequestTimeout(getTimeout()*1000)
						 .setConnectTimeout(getTimeout()*1000)
						 .setSocketTimeout(getTimeout()*1000)
		                    .build();
			}
			((HttpRequestBase)request).setConfig(config);
			request.addHeader("Connection","keep-Alive");
			request.addHeader("Host",this.host);
			request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36");
			HttpClientContext context=null;
			if(this.getProxyaddr()!=null&&this.getProxyaddr().getAuthStr()!=null){
				CredentialsProvider credsProvider =  new BasicCredentialsProvider();
            	credsProvider.setCredentials(
            	        new AuthScope(null, -1),
            	        new UsernamePasswordCredentials(this.getProxyaddr().getAuthStr()));
            	AuthCache authCache = new BasicAuthCache();

            	context = HttpClientContext.create();
            	context.setCredentialsProvider(credsProvider);
            	context.setAuthCache(authCache);
			}
			if(this.headers!=null){
				for(String k:headers.keySet()){
					request.addHeader(k,headers.get(k));
				}
			}
			
			CloseableHttpResponse response = httpclient.execute(target, request,context);
            this.que.add(response);
	}

	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	public void request(String url,HttpMethod hm,Map<String,String> headers,
			Map<String,String> postdata,String schema) throws Exception{
		this.url=url;
		this.md=hm;
		this.headers=headers;
		this.postdata=postdata;
		this.schema=schema;
		requestSelf();
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		thisThread=Thread.currentThread();
		try{
			while(isrun.get()){
				try{
					reconnect();
					CloseableHttpResponse re=this.que.poll(30*1000,TimeUnit.MILLISECONDS);
					if(re!=null){
						try{
							handleResponse2(re);
						}finally{
							re.close();
						}
					}else{
						errorNum++;
						requestSelf();
					}
				}catch(Exception e){
					CrawlerClient.mainlogger.error(this.url,e);
					errorNum++;
					if(maxErrorNum!=0&&errorNum>maxErrorNum){
						this.state=HandlerResultState.ERROR;
						break;
					}
					try {
						Thread.sleep(errorSleepTime);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
		}finally{
			if(this.httpclient!=null){
				try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}



}
