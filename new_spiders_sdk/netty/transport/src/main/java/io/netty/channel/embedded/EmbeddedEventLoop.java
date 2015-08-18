/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.embedded;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerInvoker;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractScheduledEventExecutor;
import io.netty.util.concurrent.Future;

import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelHandlerInvokerUtil.*;

final class EmbeddedEventLoop extends AbstractScheduledEventExecutor implements ChannelHandlerInvoker, EventLoop {

    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>(2);

    @Override
    public EventLoop unwrap() {
        return this;
    }

    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("command");
        }
        tasks.add(command);
    }

    void runTasks() {
        for (;;) {
            Runnable task = tasks.poll();
            if (task == null) {
                break;
            }

            task.run();
        }
    }

    long runScheduledTasks() {
        long time = AbstractScheduledEventExecutor.nanoTime();
        for (;;) {
            Runnable task = pollScheduledTask(time);
            if (task == null) {
                return nextScheduledTaskNano();
            }

            task.run();
        }
    }

    long nextScheduledTask() {
        return nextScheduledTaskNano();
    }

    @Override
    protected void cancelScheduledTasks() {
        super.cancelScheduledTasks();
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<?> terminationFuture() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return register(channel, new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        channel.unsafe().register(this, promise);
        return promise;
    }

    @Override
    public boolean inEventLoop() {
        return true;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return true;
    }

    @Override
    public ChannelHandlerInvoker asInvoker() {
        return this;
    }

    @Override
    public EventExecutor executor() {
        return this;
    }

    @Override
    public void invokeChannelRegistered(ChannelHandlerContext ctx) {
        invokeChannelRegisteredNow(ctx);
    }

    @Override
    public void invokeChannelUnregistered(ChannelHandlerContext ctx) {
        invokeChannelUnregisteredNow(ctx);
    }

    @Override
    public void invokeChannelActive(ChannelHandlerContext ctx) {
        invokeChannelActiveNow(ctx);
    }

    @Override
    public void invokeChannelInactive(ChannelHandlerContext ctx) {
        invokeChannelInactiveNow(ctx);
    }

    @Override
    public void invokeExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        invokeExceptionCaughtNow(ctx, cause);
    }

    @Override
    public void invokeUserEventTriggered(ChannelHandlerContext ctx, Object event) {
        invokeUserEventTriggeredNow(ctx, event);
    }

    @Override
    public void invokeChannelRead(ChannelHandlerContext ctx, Object msg) {
        invokeChannelReadNow(ctx, msg);
    }

    @Override
    public void invokeChannelReadComplete(ChannelHandlerContext ctx) {
        invokeChannelReadCompleteNow(ctx);
    }

    @Override
    public void invokeChannelWritabilityChanged(ChannelHandlerContext ctx) {
        invokeChannelWritabilityChangedNow(ctx);
    }

    @Override
    public void invokeBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        invokeBindNow(ctx, localAddress, promise);
    }

    @Override
    public void invokeConnect(
            ChannelHandlerContext ctx,
            SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        invokeConnectNow(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void invokeDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
        invokeDisconnectNow(ctx, promise);
    }

    @Override
    public void invokeClose(ChannelHandlerContext ctx, ChannelPromise promise) {
        invokeCloseNow(ctx, promise);
    }

    @Override
    public void invokeDeregister(ChannelHandlerContext ctx, final ChannelPromise promise) {
        invokeDeregisterNow(ctx, promise);
    }

    @Override
    public void invokeRead(ChannelHandlerContext ctx) {
        invokeReadNow(ctx);
    }

    @Override
    public void invokeWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        invokeWriteNow(ctx, msg, promise);
    }

    @Override
    public void invokeFlush(ChannelHandlerContext ctx) {
        invokeFlushNow(ctx);
    }
}
