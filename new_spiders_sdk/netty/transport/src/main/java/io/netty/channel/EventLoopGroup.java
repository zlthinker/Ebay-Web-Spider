/*
 * Copyright 2013 The Netty Project
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

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Special {@link EventExecutorGroup} which allows registering {@link Channel}s that get
 * processed for later selection during the event loop.
 */
public interface EventLoopGroup extends EventExecutorGroup {
    @Override
    EventLoop next();

    /**
     * Register a {@link Channel} with an {@link EventLoop} from this {@link EventLoopGroup}. The returned
     * {@link ChannelFuture} will get notified once the registration is completed.
     * <p>
     * It's only safe to submit a new task to the {@link EventLoop} from within a
     * {@link ChannelHandler} once the {@link ChannelPromise} succeeded. Otherwise
     * the task may or may not be rejected.
     * </p>
     */
    ChannelFuture register(Channel channel);

    /**
     * Register a {@link Channel} with an {@link EventLoop} from this {@link EventLoopGroup}. The provided
     * {@link ChannelPromise} will get notified once the registration is completed. The returned {@link ChannelFuture}
     * is the same {@link ChannelPromise} that was passed to the method.
     * <p>
     * It's only safe to submit a new task to the {@link EventLoop} from within a
     * {@link ChannelHandler} once the {@link ChannelPromise} succeeded. Otherwise
     * the task may or may not be rejected.
     * </p>
     */
    ChannelFuture register(Channel channel, ChannelPromise promise);
}
