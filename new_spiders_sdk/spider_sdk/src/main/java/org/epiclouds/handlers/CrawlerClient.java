/**
 * @author Administrator
 * @created 2014 2014年8月27日 下午3:04:28
 * @version 1.0
 */
package org.epiclouds.handlers;

/**
 * @author Administrator
 *
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.Resource;

import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Discards any incoming data.
 */
@Component(value="crawlerclient")
public class CrawlerClient {

	public  static Logger mainlogger = LoggerFactory.getLogger(CrawlerClient.class);
	
	@Resource
	private AbstractConfigManager acm;

    private Bootstrap sb=new Bootstrap();
    private EventLoopGroup workers=new NioEventLoopGroup();

    public  CrawlerClient() throws Exception {   
	        sb.group(workers).channel(NioSocketChannel.class).
	        option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					
					/*ChannelPipeline n = ch.pipeline();
					n.addLast(new HttpResponseDecoder());
					n.addLast("readtimeouthandler",new ReadTimeoutHandler(120));
				    n.addLast("redeflater", new HttpContentDecompressor());
				    n.addLast("aggregator", new HttpObjectAggregator(1048576*1024));
			        *//**
			         * http服务器端对request编码
			         *//*
			        n.addLast( new HttpRequestEncoder());*/
				}	
	        });
	        System.out.println("client started");
    }
    
    public void execute(final AbstractNettyCrawlerHandler ac) {
    		String host=null;
    		int port=0;
    		ChannelFuture cf=null;
    		SocketAddress paddr;
    		if(ac.getProxyaddr()==null){
    			host=ac.getHost();
    			port="http".equals(ac.getSchema())?80:443;
    			paddr=new InetSocketAddress(host, port);
    			cf=sb.connect(host, port);
    		}else{
    			cf=sb.connect(ac.getProxyaddr().getAddr());
    			paddr=ac.getProxyaddr().getAddr();
    		}
    		final SocketAddress faddr=paddr;
			cf.addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future)
						throws Exception {
					Channel n=((DefaultChannelPromise) future).channel();
					if(future.isSuccess()){
						ac.setChannel(n);
						n.pipeline().addLast(new HttpResponseDecoder());
	
						n.pipeline().addLast("readtimeouthandler",new ReadTimeoutHandler(acm.getValue("read_timeout",
								Integer.class)));
				       n. pipeline().addLast("redeflater", new HttpContentDecompressor());
				       n.pipeline().addLast("aggregator", new HttpObjectAggregator(1048576*1024));
				        /**
				         * http服务器端对request编码
				         */
				        n.pipeline().addLast( new HttpRequestEncoder());
				        n.pipeline().addLast("myhandler",new CrawlerHttpClientHandler(ac));
				        ac.startToRun();
					}else{
						mainlogger.error(future.cause().getLocalizedMessage(), future.cause());
						Thread.sleep(10);
						NioSocketChannel nchannel=new NioSocketChannel();
						final GenericFutureListener lis=this; 
						final EventLoop el=n.eventLoop();
						n.eventLoop().register(nchannel).addListener(
								new GenericFutureListener<Future<? super Void>>() {
									@Override
									public void operationComplete(
											Future<? super Void> future)
											throws Exception {
										Channel n=((DefaultChannelPromise) future).channel();
										if(future.isSuccess()){
											ChannelFuture cf2=n.connect(faddr);
											cf2.addListener(lis);
										}else{
											el.register(new NioSocketChannel()).addListener(this);
										}
									}
								}		
						);
						
						if(n!=null)n.close();
					}
				}
				
			});
    		return; 
    }
    public void execute(final Runnable h) {
		CrawlerEnvironment.pool.execute(h);
    }
    
    public void close(){
    	workers.shutdownGracefully();
    }


}
