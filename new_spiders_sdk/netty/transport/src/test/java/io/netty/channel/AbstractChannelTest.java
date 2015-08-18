/*
 * Copyright 2014 The Netty Project

 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.SocketAddress;

import org.easymock.Capture;
import org.junit.Test;

public class AbstractChannelTest {

    @Test
    public void ensureInitialRegistrationFiresActive() throws Throwable {
        EventLoop eventLoop = createNiceMock(EventLoop.class);
        // This allows us to have a single-threaded test
        expect(eventLoop.inEventLoop()).andReturn(true).anyTimes();

        TestChannel channel = new TestChannel();
        ChannelHandler handler = createMock(ChannelHandler.class);
        handler.handlerAdded(anyObject(ChannelHandlerContext.class)); expectLastCall();
        Capture<Throwable> throwable = catchHandlerExceptions(handler);
        handler.channelRegistered(anyObject(ChannelHandlerContext.class));
        expectLastCall().once();
        handler.channelActive(anyObject(ChannelHandlerContext.class));
        expectLastCall().once();
        replay(handler, eventLoop);
        channel.pipeline().addLast(handler);

        registerChannel(eventLoop, channel);

        checkForHandlerException(throwable);
        verify(handler);
    }

    @Test
    public void ensureSubsequentRegistrationDoesNotFireActive() throws Throwable {
            EventLoop eventLoop = createNiceMock(EventLoop.class);
            // This allows us to have a single-threaded test
            expect(eventLoop.inEventLoop()).andReturn(true).anyTimes();

            TestChannel channel = new TestChannel();
            ChannelHandler handler = createMock(ChannelHandler.class);
            handler.handlerAdded(anyObject(ChannelHandlerContext.class)); expectLastCall();
            Capture<Throwable> throwable = catchHandlerExceptions(handler);
            handler.channelRegistered(anyObject(ChannelHandlerContext.class));
            expectLastCall().times(2); // Should register twice
            handler.channelActive(anyObject(ChannelHandlerContext.class));
            expectLastCall().once(); // Should only fire active once
            replay(handler, eventLoop);
            channel.pipeline().addLast(handler);

            registerChannel(eventLoop, channel);
            channel.unsafe().deregister(new DefaultChannelPromise(channel));
            registerChannel(eventLoop, channel);

            checkForHandlerException(throwable);
            verify(handler);
        }

    private void registerChannel(EventLoop eventLoop, Channel channel) throws Exception {
        DefaultChannelPromise future = new DefaultChannelPromise(channel);
        channel.unsafe().register(eventLoop, future);
        future.sync(); // Cause any exceptions to be thrown
    }

    private Capture<Throwable> catchHandlerExceptions(ChannelHandler handler) throws Exception {
        Capture<Throwable> throwable = new Capture<Throwable>();
        handler.exceptionCaught(anyObject(ChannelHandlerContext.class), capture(throwable));
        expectLastCall().anyTimes();
        return throwable;
    }

    private void checkForHandlerException(Capture<Throwable> throwable) throws Throwable {
        if (throwable.hasCaptured()) {
            throw throwable.getValue();
        }
    }

    private static class TestChannel extends AbstractChannel {

        private class TestUnsafe extends AbstractUnsafe {

            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) { }
        }

        public TestChannel() {
            super(null);
        }

        @Override
        public ChannelConfig config() {
            return new DefaultChannelConfig(this);
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public ChannelMetadata metadata() {
            return null;
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return new TestUnsafe();
        }

        @Override
        protected boolean isCompatible(EventLoop loop) {
            return true;
        }

        @Override
        protected SocketAddress localAddress0() {
            return null;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return null;
        }

        @Override
        protected void doBind(SocketAddress localAddress) throws Exception { }

        @Override
        protected void doDisconnect() throws Exception { }

        @Override
        protected void doClose() throws Exception { }

        @Override
        protected void doBeginRead() throws Exception { }

        @Override
        protected void doWrite(ChannelOutboundBuffer in) throws Exception { }
    }
}
