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
package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler.Skip;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakHint;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.PausableEventExecutor;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.net.SocketAddress;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Abstract base class for {@link ChannelHandlerContext} implementations.
 */
abstract class AbstractChannelHandlerContext implements ChannelHandlerContext, ResourceLeakHint {

    // This class keeps an integer member field 'skipFlags' whose each bit tells if the corresponding handler method
    // is annotated with @Skip. 'skipFlags' is retrieved in runtime via the reflection API and is cached.
    // The following constants signify which bit of 'skipFlags' corresponds to which handler method:

    static final int MASK_HANDLER_ADDED = 1;
    static final int MASK_HANDLER_REMOVED = 1 << 1;

    private static final int MASK_EXCEPTION_CAUGHT = 1 << 2;
    private static final int MASK_CHANNEL_REGISTERED = 1 << 3;
    private static final int MASK_CHANNEL_UNREGISTERED = 1 << 4;
    private static final int MASK_CHANNEL_ACTIVE = 1 << 5;
    private static final int MASK_CHANNEL_INACTIVE = 1 << 6;
    private static final int MASK_CHANNEL_READ = 1 << 7;
    private static final int MASK_CHANNEL_READ_COMPLETE = 1 << 8;
    private static final int MASK_CHANNEL_WRITABILITY_CHANGED = 1 << 9;
    private static final int MASK_USER_EVENT_TRIGGERED = 1 << 10;

    private static final int MASK_BIND = 1 << 11;
    private static final int MASK_CONNECT = 1 << 12;
    private static final int MASK_DISCONNECT = 1 << 13;
    private static final int MASK_CLOSE = 1 << 14;
    private static final int MASK_DEREGISTER = 1 << 15;
    private static final int MASK_READ = 1 << 16;
    private static final int MASK_WRITE = 1 << 17;
    private static final int MASK_FLUSH = 1 << 18;

    private static final int MASKGROUP_INBOUND = MASK_EXCEPTION_CAUGHT |
            MASK_CHANNEL_REGISTERED |
            MASK_CHANNEL_UNREGISTERED |
            MASK_CHANNEL_ACTIVE |
            MASK_CHANNEL_INACTIVE |
            MASK_CHANNEL_READ |
            MASK_CHANNEL_READ_COMPLETE |
            MASK_CHANNEL_WRITABILITY_CHANGED |
            MASK_USER_EVENT_TRIGGERED;

    private static final int MASKGROUP_OUTBOUND = MASK_BIND |
            MASK_CONNECT |
            MASK_DISCONNECT |
            MASK_CLOSE |
            MASK_DEREGISTER |
            MASK_READ |
            MASK_WRITE |
            MASK_FLUSH;

    /**
     * Cache the result of the costly generation of {@link #skipFlags} in a thread-local {@link WeakHashMap}.
     */
    private static final FastThreadLocal<WeakHashMap<Class<?>, Integer>> skipFlagsCache =
            new FastThreadLocal<WeakHashMap<Class<?>, Integer>>() {
                @Override
                protected WeakHashMap<Class<?>, Integer> initialValue() throws Exception {
                    return new WeakHashMap<Class<?>, Integer>();
                }
            };

    private static final AtomicReferenceFieldUpdater<AbstractChannelHandlerContext, PausableChannelEventExecutor>
            WRAPPED_EVENTEXECUTOR_UPDATER;

    static {
        AtomicReferenceFieldUpdater<AbstractChannelHandlerContext, PausableChannelEventExecutor> updater =
               PlatformDependent.newAtomicReferenceFieldUpdater(
                       AbstractChannelHandlerContext.class, "wrappedEventLoop");
        if (updater == null) {
            updater = AtomicReferenceFieldUpdater.newUpdater(
                    AbstractChannelHandlerContext.class, PausableChannelEventExecutor.class, "wrappedEventLoop");
        }
        WRAPPED_EVENTEXECUTOR_UPDATER = updater;
    }

    /**
     * Returns an integer bitset that tells which handler methods were annotated with {@link Skip}.
     * It gets the value from {@link #skipFlagsCache} if an handler of the same type were queried before.
     * Otherwise, it delegates to {@link #skipFlags0(Class)} to get it.
     */
    static int skipFlags(ChannelHandler handler) {
        WeakHashMap<Class<?>, Integer> cache = skipFlagsCache.get();
        Class<? extends ChannelHandler> handlerType = handler.getClass();
        int flagsVal;
        Integer flags = cache.get(handlerType);
        if (flags != null) {
            flagsVal = flags;
        } else {
            flagsVal = skipFlags0(handlerType);
            cache.put(handlerType, Integer.valueOf(flagsVal));
        }

        return flagsVal;
    }

    /**
     * Determines the {@link #skipFlags} of the specified {@code handlerType} using the reflection API.
     */
    static int skipFlags0(Class<? extends ChannelHandler> handlerType) {
        int flags = 0;
        try {
            if (isSkippable(handlerType, "handlerAdded")) {
                flags |= MASK_HANDLER_ADDED;
            }
            if (isSkippable(handlerType, "handlerRemoved")) {
                flags |= MASK_HANDLER_REMOVED;
            }
            if (isSkippable(handlerType, "exceptionCaught", Throwable.class)) {
                flags |= MASK_EXCEPTION_CAUGHT;
            }
            if (isSkippable(handlerType, "channelRegistered")) {
                flags |= MASK_CHANNEL_REGISTERED;
            }
            if (isSkippable(handlerType, "channelUnregistered")) {
                flags |= MASK_CHANNEL_UNREGISTERED;
            }
            if (isSkippable(handlerType, "channelActive")) {
                flags |= MASK_CHANNEL_ACTIVE;
            }
            if (isSkippable(handlerType, "channelInactive")) {
                flags |= MASK_CHANNEL_INACTIVE;
            }
            if (isSkippable(handlerType, "channelRead", Object.class)) {
                flags |= MASK_CHANNEL_READ;
            }
            if (isSkippable(handlerType, "channelReadComplete")) {
                flags |= MASK_CHANNEL_READ_COMPLETE;
            }
            if (isSkippable(handlerType, "channelWritabilityChanged")) {
                flags |= MASK_CHANNEL_WRITABILITY_CHANGED;
            }
            if (isSkippable(handlerType, "userEventTriggered", Object.class)) {
                flags |= MASK_USER_EVENT_TRIGGERED;
            }
            if (isSkippable(handlerType, "bind", SocketAddress.class, ChannelPromise.class)) {
                flags |= MASK_BIND;
            }
            if (isSkippable(handlerType, "connect", SocketAddress.class, SocketAddress.class, ChannelPromise.class)) {
                flags |= MASK_CONNECT;
            }
            if (isSkippable(handlerType, "disconnect", ChannelPromise.class)) {
                flags |= MASK_DISCONNECT;
            }
            if (isSkippable(handlerType, "close", ChannelPromise.class)) {
                flags |= MASK_CLOSE;
            }
            if (isSkippable(handlerType, "deregister", ChannelPromise.class)) {
                flags |= MASK_DEREGISTER;
            }
            if (isSkippable(handlerType, "read")) {
                flags |= MASK_READ;
            }
            if (isSkippable(handlerType, "write", Object.class, ChannelPromise.class)) {
                flags |= MASK_WRITE;
            }
            if (isSkippable(handlerType, "flush")) {
                flags |= MASK_FLUSH;
            }
        } catch (Exception e) {
            // Should never reach here.
            PlatformDependent.throwException(e);
        }

        return flags;
    }

    @SuppressWarnings("rawtypes")
    private static boolean isSkippable(
            Class<?> handlerType, String methodName, Class<?>... paramTypes) throws Exception {

        Class[] newParamTypes = new Class[paramTypes.length + 1];
        newParamTypes[0] = ChannelHandlerContext.class;
        System.arraycopy(paramTypes, 0, newParamTypes, 1, paramTypes.length);

        return handlerType.getMethod(methodName, newParamTypes).isAnnotationPresent(Skip.class);
    }

    volatile AbstractChannelHandlerContext next;
    volatile AbstractChannelHandlerContext prev;

    private final AbstractChannel channel;
    private final DefaultChannelPipeline pipeline;
    private final String name;

    /**
     * Set when the {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)} of
     * this context's handler is invoked.
     * Cleared when a user calls {@link #fireChannelReadComplete()} on this context.
     *
     * See {@link #fireChannelReadComplete()} to understand how this flag is used.
     */
    boolean invokedThisChannelRead;

    /**
     * Set when a user calls {@link #fireChannelRead(Object)} on this context.
     * Cleared when a user calls {@link #fireChannelReadComplete()} on this context.
     *
     * See {@link #fireChannelReadComplete()} to understand how this flag is used.
     */
    private volatile boolean invokedNextChannelRead;

    /**
     * Set when a user calls {@link #read()} on this context.
     * Cleared when a user calls {@link #fireChannelReadComplete()} on this context.
     *
     * See {@link #fireChannelReadComplete()} to understand how this flag is used.
     */
    private volatile boolean invokedPrevRead;

    /**
     * {@code true} if and only if this context has been removed from the pipeline.
     */
    private boolean removed;

    final int skipFlags;

    // Will be set to null if no child executor should be used, otherwise it will be set to the
    // child executor.
    final ChannelHandlerInvoker invoker;
    private ChannelFuture succeededFuture;

    // Lazily instantiated tasks used to trigger events to a handler with different executor.
    // These needs to be volatile as otherwise an other Thread may see an half initialized instance.
    // See the JMM for more details
    volatile Runnable invokeChannelReadCompleteTask;
    volatile Runnable invokeReadTask;
    volatile Runnable invokeFlushTask;
    volatile Runnable invokeChannelWritableStateChangedTask;

    /**
     * Wrapped {@link EventLoop} and {@link ChannelHandlerInvoker} to support {@link Channel#deregister()}.
     */
    @SuppressWarnings("UnusedDeclaration")
    private volatile PausableChannelEventExecutor wrappedEventLoop;

    AbstractChannelHandlerContext(
            DefaultChannelPipeline pipeline, ChannelHandlerInvoker invoker, String name, int skipFlags) {

        if (name == null) {
            throw new NullPointerException("name");
        }

        channel = pipeline.channel;
        this.pipeline = pipeline;
        this.name = name;
        this.invoker = invoker;
        this.skipFlags = skipFlags;
    }

    @Override
    public final Channel channel() {
        return channel;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public ByteBufAllocator alloc() {
        return channel().config().getAllocator();
    }

    @Override
    public final EventExecutor executor() {
        if (invoker == null) {
            return channel().eventLoop();
        } else {
            return wrappedEventLoop();
        }
    }

    @Override
    public final ChannelHandlerInvoker invoker() {
        if (invoker == null) {
            return channel().eventLoop().asInvoker();
        } else {
            return wrappedEventLoop();
        }
    }

    private PausableChannelEventExecutor wrappedEventLoop() {
        PausableChannelEventExecutor wrapped = wrappedEventLoop;
        if (wrapped == null) {
            wrapped = new PausableChannelEventExecutor0();
            if (!WRAPPED_EVENTEXECUTOR_UPDATER.compareAndSet(this, null, wrapped)) {
                // Set in the meantime so we need to issue another volatile read
                return wrappedEventLoop;
            }
        }
        return wrapped;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return channel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return channel.hasAttr(key);
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeChannelRegistered(next);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeChannelUnregistered(next);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeChannelActive(next);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeChannelInactive(next);
        return this;
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeExceptionCaught(next, cause);
        return this;
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object event) {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeUserEventTriggered(next, event);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        AbstractChannelHandlerContext next = findContextInbound();
        ReferenceCountUtil.touch(msg, next);
        invokedNextChannelRead = true;
        next.invoker().invokeChannelRead(next, msg);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        /**
         * If the handler of this context did not produce any messages via {@link #fireChannelRead(Object)},
         * there's no reason to trigger {@code channelReadComplete()} even if the handler called this method.
         *
         * This is pretty common for the handlers that transform multiple messages into one message,
         * such as byte-to-message decoder and message aggregators.
         *
         * Only one exception is when nobody invoked the channelRead() method of this context's handler.
         * It means the handler has been added later dynamically.
         */
        if (invokedNextChannelRead ||  // The handler of this context produced a message, or
            !invokedThisChannelRead) { // it is not required to produce a message to trigger the event.

            invokedNextChannelRead = false;
            invokedPrevRead = false;

            AbstractChannelHandlerContext next = findContextInbound();
            next.invoker().invokeChannelReadComplete(next);
            return this;
        }

        /**
         * At this point, we are sure the handler of this context did not produce anything and
         * we suppressed the {@code channelReadComplete()} event.
         *
         * If the next handler invoked {@link #read()} to read something but nothing was produced
         * by the handler of this context, someone has to issue another {@link #read()} operation
         * until the handler of this context produces something.
         *
         * Why? Because otherwise the next handler will not receive {@code channelRead()} nor
         * {@code channelReadComplete()} event at all for the {@link #read()} operation it issued.
         */
        if (invokedPrevRead && !channel().config().isAutoRead()) {
            /**
             * The next (or upstream) handler invoked {@link #read()}, but it didn't get any
             * {@code channelRead()} event. We should read once more, so that the handler of the current
             * context have a chance to produce something.
             */
            read();
        } else {
            invokedPrevRead = false;
        }

        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        AbstractChannelHandlerContext next = findContextInbound();
        next.invoker().invokeChannelWritabilityChanged(next);
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return bind(localAddress, newPromise());
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return connect(remoteAddress, newPromise());
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return connect(remoteAddress, localAddress, newPromise());
    }

    @Override
    public ChannelFuture disconnect() {
        return disconnect(newPromise());
    }

    @Override
    public ChannelFuture close() {
        return close(newPromise());
    }

    @Override
    public ChannelFuture deregister() {
        return deregister(newPromise());
    }

    @Override
    public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeBind(next, localAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return connect(remoteAddress, null, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeConnect(next, remoteAddress, localAddress, promise);
        return promise;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        if (!channel().metadata().hasDisconnect()) {
            return close(promise);
        }

        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeDisconnect(next, promise);
        return promise;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeClose(next, promise);
        return promise;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeDeregister(next, promise);
        return promise;
    }

    @Override
    public ChannelHandlerContext read() {
        AbstractChannelHandlerContext next = findContextOutbound();
        invokedPrevRead = true;
        next.invoker().invokeRead(next);
        return this;
    }

    @Override
    public ChannelFuture write(Object msg) {
        return write(msg, newPromise());
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        AbstractChannelHandlerContext next = findContextOutbound();
        ReferenceCountUtil.touch(msg, next);
        next.invoker().invokeWrite(next, msg, promise);
        return promise;
    }

    @Override
    public ChannelHandlerContext flush() {
        AbstractChannelHandlerContext next = findContextOutbound();
        next.invoker().invokeFlush(next);
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        AbstractChannelHandlerContext next;
        next = findContextOutbound();
        ReferenceCountUtil.touch(msg, next);
        next.invoker().invokeWrite(next, msg, promise);
        next = findContextOutbound();
        next.invoker().invokeFlush(next);
        return promise;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return writeAndFlush(msg, newPromise());
    }

    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(channel(), executor());
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return new DefaultChannelProgressivePromise(channel(), executor());
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        ChannelFuture succeededFuture = this.succeededFuture;
        if (succeededFuture == null) {
            this.succeededFuture = succeededFuture = new SucceededChannelFuture(channel(), executor());
        }
        return succeededFuture;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return new FailedChannelFuture(channel(), executor(), cause);
    }

    private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.next;
        } while ((ctx.skipFlags & MASKGROUP_INBOUND) == MASKGROUP_INBOUND);
        return ctx;
    }

    private AbstractChannelHandlerContext findContextOutbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.prev;
        } while ((ctx.skipFlags & MASKGROUP_OUTBOUND) == MASKGROUP_OUTBOUND);
        return ctx;
    }

    @Override
    public ChannelPromise voidPromise() {
        return channel.voidPromise();
    }

    void setRemoved() {
        removed = true;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public String toHintString() {
        return '\'' + name + "' will handle the message from this point.";
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + name + ", " + channel + ')';
    }

    private final class PausableChannelEventExecutor0 extends PausableChannelEventExecutor {

        @Override
        public void rejectNewTasks() {
            /**
             * This cast is correct because {@link #channel()} always returns an {@link AbstractChannel} and
             * {@link AbstractChannel#eventLoop()} always returns a {@link PausableChannelEventExecutor}.
             */
            ((PausableEventExecutor) channel().eventLoop()).rejectNewTasks();
        }

        @Override
        public void acceptNewTasks() {
            ((PausableEventExecutor) channel().eventLoop()).acceptNewTasks();
        }

        @Override
        public boolean isAcceptingNewTasks() {
            return ((PausableEventExecutor) channel().eventLoop()).isAcceptingNewTasks();
        }

        @Override
        public Channel channel() {
            return AbstractChannelHandlerContext.this.channel();
        }

        @Override
        public EventExecutor unwrap() {
            return unwrapInvoker().executor();
        }

        @Override
        public ChannelHandlerInvoker unwrapInvoker() {
            /**
             * {@link #invoker} can not be {@code null}, because {@link PausableChannelEventExecutor0} will only be
             * instantiated if {@link #invoker} is not {@code null}.
             */
            return invoker;
        }
    }
}
