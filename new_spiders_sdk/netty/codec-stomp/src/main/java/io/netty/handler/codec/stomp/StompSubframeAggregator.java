/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageAggregator;
import io.netty.handler.codec.TooLongFrameException;

/**
 * A {@link ChannelHandler} that aggregates an {@link StompHeadersSubframe}
 * and its following {@link StompContentSubframe}s into a single {@link StompFrame}.
 * It is useful when you don't want to take care of STOMP frames whose content is 'chunked'.  Insert this
 * handler after {@link StompSubframeDecoder} in the {@link ChannelPipeline}:
 */
public class StompSubframeAggregator
        extends MessageAggregator<StompSubframe, StompHeadersSubframe, StompContentSubframe, StompFrame> {

    /**
     * Creates a new instance.
     *
     * @param maxContentLength
     *        the maximum length of the aggregated content.
     *        If the length of the aggregated content exceeds this value,
     *        a {@link TooLongFrameException} will be raised.
     */
    public StompSubframeAggregator(int maxContentLength) {
        super(maxContentLength);
    }

    @Override
    protected boolean isStartMessage(StompSubframe msg) throws Exception {
        return msg instanceof StompHeadersSubframe;
    }

    @Override
    protected boolean isContentMessage(StompSubframe msg) throws Exception {
        return msg instanceof StompContentSubframe;
    }

    @Override
    protected boolean isLastContentMessage(StompContentSubframe msg) throws Exception {
        return msg instanceof LastStompContentSubframe;
    }

    @Override
    protected boolean isAggregated(StompSubframe msg) throws Exception {
        return msg instanceof StompFrame;
    }

    @Override
    protected boolean hasContentLength(StompHeadersSubframe start) throws Exception {
        return start.headers().contains(StompHeaders.CONTENT_LENGTH);
    }

    @Override
    protected long contentLength(StompHeadersSubframe start) throws Exception {
        return start.headers().getLong(StompHeaders.CONTENT_LENGTH, 0);
    }

    @Override
    protected Object newContinueResponse(StompHeadersSubframe start) throws Exception {
        return null;
    }

    @Override
    protected StompFrame beginAggregation(StompHeadersSubframe start, ByteBuf content) throws Exception {
        StompFrame ret = new DefaultStompFrame(start.command(), content);
        ret.headers().set(start.headers());
        return ret;
    }
}
