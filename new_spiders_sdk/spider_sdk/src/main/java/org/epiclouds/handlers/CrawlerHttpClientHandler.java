package org.epiclouds.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.Resource;

import org.epiclouds.spiders.bootstrap.imp.Bootstrap;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 *
 */
public class CrawlerHttpClientHandler extends ChannelHandlerAdapter{
	public  static Logger mainlogger = LoggerFactory.getLogger(CrawlerHttpClientHandler.class);
	private volatile AbstractNettyCrawlerHandler h;
	private AbstractConfigManager acm=Bootstrap.getSingle().getAcfm();
	public CrawlerHttpClientHandler(AbstractNettyCrawlerHandler h){
		this.h=h;
	}
	@Override
	public boolean isSharable() {
		// TODO Auto-generated method stub
		return super.isSharable();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerAdded(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerRemoved(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause instanceof ReadTimeoutException) {
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }

	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("新连接："+ctx.channel().localAddress().toString());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("连接Inactive："+ctx.channel().localAddress().toString());
		super.channelInactive(ctx);
		if(h.getIsrun().get()){
			ctx.channel().close();
			NioSocketChannel nchannel=new NioSocketChannel();
			ctx.channel().eventLoop().register(nchannel);
			final SocketAddress remoteAddr=h.getProxyaddr()!=null?h.getProxyaddr().getAddr():
				new InetSocketAddress(h.getHost(), "http".equals(h.getSchema())?80:443);
			ChannelFuture cf=nchannel.connect(remoteAddr);
			cf.addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future)
						throws Exception {
					Channel n=((DefaultChannelPromise) future).channel();
					if(future.isSuccess()){
						CrawlerHttpClientHandler.this.h.setChannel(n);
						n.pipeline().addLast(new HttpResponseDecoder());
	
						n.pipeline().addLast("readtimeouthandler",new ReadTimeoutHandler(
								acm.getValue("read_timeout",
										Integer.class)));
				       n. pipeline().addLast("redeflater", new HttpContentDecompressor());
				       n.pipeline().addLast("aggregator", new HttpObjectAggregator(1048576*1024));
				        /**
				         * http服务器端对request编码
				         */
				        n.pipeline().addLast( new HttpRequestEncoder());
				        n.pipeline().addLast("myhandler",new CrawlerHttpClientHandler(CrawlerHttpClientHandler.this.h));
				        CrawlerHttpClientHandler.this.h.continueSpider();
					}else{
						mainlogger.error(future.cause().getLocalizedMessage(), future.cause());
						if(!h.getIsrun().get()){
							return;
						}
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
											ChannelFuture cf2=n.connect(remoteAddr);
											cf2.addListener(lis);
										}else{
											el.register(new NioSocketChannel()).addListener(this);
										}
									}
								}		
						);
					}
				}
				
			});
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//System.out.println("连接channelRead："+ctx.channel().localAddress().toString());
		FullHttpResponse response=(FullHttpResponse)msg;
		h.runOnce( response);
	}

	
	


	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
			ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.bind(ctx, localAddress, promise);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception {
		// TODO Auto-generated method stub
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		// TODO Auto-generated method stub
		super.disconnect(ctx, promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		//System.out.println("连接关闭："+ctx.channel().localAddress().toString());
		super.close(ctx, promise);
	}

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.read(ctx);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.write(ctx, msg, promise);
	}


	
}
