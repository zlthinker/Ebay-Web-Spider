/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import static io.netty.handler.codec.http2.Http2Error.INTERNAL_ERROR;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

/**
 * This adapter provides just header/data events from the HTTP message flow defined
 * here <a href="http://tools.ietf.org/html/draft-ietf-httpbis-http2-16#section-8.1.">HTTP/2 Spec Message Flow</a>.
 * <p>
 * See {@link HttpToHttp2ConnectionHandler} to get translation from HTTP/1.x objects to HTTP/2 frames for writes.
 */
public class InboundHttp2ToHttpAdapter extends Http2EventAdapter {
    private static final ImmediateSendDetector DEFAULT_SEND_DETECTOR = new ImmediateSendDetector() {
        @Override
        public boolean mustSendImmediately(FullHttpMessage msg) {
            if (msg instanceof FullHttpResponse) {
                return ((FullHttpResponse) msg).status().codeClass() == HttpStatusClass.INFORMATIONAL;
            }
            if (msg instanceof FullHttpRequest) {
                return msg.headers().contains(HttpHeaderNames.EXPECT);
            }
            return false;
        }

        @Override
        public FullHttpMessage copyIfNeeded(FullHttpMessage msg) {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest copy = ((FullHttpRequest) msg).copy(null);
                copy.headers().remove(HttpHeaderNames.EXPECT);
                return copy;
            }
            return null;
        }
    };

    private final int maxContentLength;
    protected final Http2Connection connection;
    protected final boolean validateHttpHeaders;
    private final ImmediateSendDetector sendDetector;
    protected final IntObjectMap<FullHttpMessage> messageMap;
    private final boolean propagateSettings;

    public static class Builder {

        private final Http2Connection connection;
        private int maxContentLength;
        private boolean validateHttpHeaders;
        private boolean propagateSettings;

        /**
         * Creates a new {@link InboundHttp2ToHttpAdapter} builder for the specified {@link Http2Connection}.
         *
         * @param connection The object which will provide connection notification events for the current connection
         */
        public Builder(Http2Connection connection) {
            this.connection = connection;
        }

        /**
         * Specifies the maximum length of the message content.
         *
         * @param maxContentLength the maximum length of the message content. If the length of the message content
         *        exceeds this value, a {@link TooLongFrameException} will be raised
         * @return {@link Builder} the builder for the {@link InboundHttp2ToHttpAdapter}
         */
        public Builder maxContentLength(int maxContentLength) {
            this.maxContentLength = maxContentLength;
            return this;
        }

        /**
         * Specifies whether validation of HTTP headers should be performed.
         *
         * @param validate
         * <ul>
         * <li>{@code true} to validate HTTP headers in the http-codec</li>
         * <li>{@code false} not to validate HTTP headers in the http-codec</li>
         * </ul>
         * @return {@link Builder} the builder for the {@link InboundHttp2ToHttpAdapter}
         */
        public Builder validateHttpHeaders(boolean validate) {
            validateHttpHeaders = validate;
            return this;
        }

        /**
         * Specifies whether a read settings frame should be propagated alone the channel pipeline.
         *
         * @param propagate if {@code true} read settings will be passed along the pipeline. This can be useful
         *                     to clients that need hold off sending data until they have received the settings.
         * @return {@link Builder} the builder for the {@link InboundHttp2ToHttpAdapter}
         */
        public Builder propagateSettings(boolean propagate) {
            propagateSettings = propagate;
            return this;
        }

        /**
         * Builds/creates a new {@link InboundHttp2ToHttpAdapter} instance using this builders current settings.
         */
        public InboundHttp2ToHttpAdapter build() {
            InboundHttp2ToHttpAdapter instance = new InboundHttp2ToHttpAdapter(this);
            connection.addListener(instance);
            return instance;
        }
    }

    protected InboundHttp2ToHttpAdapter(Builder builder) {
        checkNotNull(builder.connection, "connection");
        if (builder.maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength must be a positive integer: "
                    + builder.maxContentLength);
        }
        connection = builder.connection;
        maxContentLength = builder.maxContentLength;
        validateHttpHeaders = builder.validateHttpHeaders;
        propagateSettings = builder.propagateSettings;
        sendDetector = DEFAULT_SEND_DETECTOR;
        messageMap = new IntObjectHashMap<FullHttpMessage>();
    }

    /**
     * The streamId is out of scope for the HTTP message flow and will no longer be tracked
     * @param streamId The stream id to remove associated state with
     */
    protected void removeMessage(int streamId) {
        messageMap.remove(streamId);
    }

    @Override
    public void onStreamRemoved(Http2Stream stream) {
        removeMessage(stream.id());
    }

    /**
     * Set final headers and fire a channel read event
     *
     * @param ctx The context to fire the event on
     * @param msg The message to send
     * @param streamId the streamId of the message which is being fired
     */
    protected void fireChannelRead(ChannelHandlerContext ctx, FullHttpMessage msg, int streamId) {
        removeMessage(streamId);
        HttpHeaderUtil.setContentLength(msg, msg.content().readableBytes());
        ctx.fireChannelRead(msg);
    }

    /**
     * Create a new {@link FullHttpMessage} based upon the current connection parameters
     *
     * @param streamId The stream id to create a message for
     * @param headers The headers associated with {@code streamId}
     * @param validateHttpHeaders
     * <ul>
     * <li>{@code true} to validate HTTP headers in the http-codec</li>
     * <li>{@code false} not to validate HTTP headers in the http-codec</li>
     * </ul>
     * @throws Http2Exception
     */
    protected FullHttpMessage newMessage(int streamId, Http2Headers headers, boolean validateHttpHeaders)
            throws Http2Exception {
        return connection.isServer() ? HttpUtil.toHttpRequest(streamId, headers,
                validateHttpHeaders) : HttpUtil.toHttpResponse(streamId, headers, validateHttpHeaders);
    }

    /**
     * Provides translation between HTTP/2 and HTTP header objects while ensuring the stream
     * is in a valid state for additional headers.
     *
     * @param ctx The context for which this message has been received.
     * Used to send informational header if detected.
     * @param streamId The stream id the {@code headers} apply to
     * @param headers The headers to process
     * @param endOfStream {@code true} if the {@code streamId} has received the end of stream flag
     * @param allowAppend
     * <ul>
     * <li>{@code true} if headers will be appended if the stream already exists.</li>
     * <li>if {@code false} and the stream already exists this method returns {@code null}.</li>
     * </ul>
     * @param appendToTrailer
     * <ul>
     * <li>{@code true} if a message {@code streamId} already exists then the headers
     * should be added to the trailing headers.</li>
     * <li>{@code false} then appends will be done to the initial headers.</li>
     * </ul>
     * @return The object used to track the stream corresponding to {@code streamId}. {@code null} if
     *         {@code allowAppend} is {@code false} and the stream already exists.
     * @throws Http2Exception If the stream id is not in the correct state to process the headers request
     */
    protected FullHttpMessage processHeadersBegin(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                boolean endOfStream, boolean allowAppend, boolean appendToTrailer) throws Http2Exception {
        FullHttpMessage msg = messageMap.get(streamId);
        if (msg == null) {
            msg = newMessage(streamId, headers, validateHttpHeaders);
        } else if (allowAppend) {
            try {
                HttpUtil.addHttp2ToHttpHeaders(streamId, headers, msg, appendToTrailer);
            } catch (Http2Exception e) {
                removeMessage(streamId);
                throw e;
            }
        } else {
            msg = null;
        }

        if (sendDetector.mustSendImmediately(msg)) {
            // Copy the message (if necessary) before sending. The content is not expected to be copied (or used) in
            // this operation but just in case it is used do the copy before sending and the resource may be released
            final FullHttpMessage copy = endOfStream ? null : sendDetector.copyIfNeeded(msg);
            fireChannelRead(ctx, msg, streamId);
            return copy;
        }

        return msg;
    }

    /**
     * After HTTP/2 headers have been processed by {@link #processHeadersBegin} this method either
     * sends the result up the pipeline or retains the message for future processing.
     *
     * @param ctx The context for which this message has been received
     * @param streamId The stream id the {@code objAccumulator} corresponds to
     * @param msg The object which represents all headers/data for corresponding to {@code streamId}
     * @param endOfStream {@code true} if this is the last event for the stream
     */
    private void processHeadersEnd(ChannelHandlerContext ctx, int streamId,
            FullHttpMessage msg, boolean endOfStream) {
        if (endOfStream) {
            fireChannelRead(ctx, msg, streamId);
        } else {
            messageMap.put(streamId, msg);
        }
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
                    throws Http2Exception {
        FullHttpMessage msg = messageMap.get(streamId);
        if (msg == null) {
            throw connectionError(PROTOCOL_ERROR, "Data Frame recieved for unknown stream id %d", streamId);
        }

        ByteBuf content = msg.content();
        final int dataReadableBytes = data.readableBytes();
        if (content.readableBytes() > maxContentLength - dataReadableBytes) {
            throw connectionError(INTERNAL_ERROR,
                            "Content length exceeded max of %d for stream id %d", maxContentLength, streamId);
        }

        content.writeBytes(data, data.readerIndex(), dataReadableBytes);

        if (endOfStream) {
            fireChannelRead(ctx, msg, streamId);
        }

        // All bytes have been processed.
        return dataReadableBytes + padding;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                    boolean endOfStream) throws Http2Exception {
        FullHttpMessage msg = processHeadersBegin(ctx, streamId, headers, endOfStream, true, true);
        if (msg != null) {
            processHeadersEnd(ctx, streamId, msg, endOfStream);
        }
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                    short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
        FullHttpMessage msg = processHeadersBegin(ctx, streamId, headers, endOfStream, true, true);
        if (msg != null) {
            processHeadersEnd(ctx, streamId, msg, endOfStream);
        }
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
        FullHttpMessage msg = messageMap.get(streamId);
        if (msg != null) {
            fireChannelRead(ctx, msg, streamId);
        }
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
            Http2Headers headers, int padding) throws Http2Exception {
        // A push promise should not be allowed to add headers to an existing stream
        FullHttpMessage msg = processHeadersBegin(ctx, promisedStreamId, headers, false, false, false);
        if (msg == null) {
            throw connectionError(PROTOCOL_ERROR, "Push Promise Frame recieved for pre-existing stream id %d",
                            promisedStreamId);
        }

        msg.headers().setInt(HttpUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);

        processHeadersEnd(ctx, promisedStreamId, msg, false);
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
        if (propagateSettings) {
            // Provide an interface for non-listeners to capture settings
            ctx.fireChannelRead(settings);
        }
    }

    /**
     * Allows messages to be sent up the pipeline before the next phase in the
     * HTTP message flow is detected.
     */
    private interface ImmediateSendDetector {
        /**
         * Determine if the response should be sent immediately, or wait for the end of the stream
         *
         * @param msg The response to test
         * @return {@code true} if the message should be sent immediately
         *         {@code false) if we should wait for the end of the stream
         */
        boolean mustSendImmediately(FullHttpMessage msg);

        /**
         * Determine if a copy must be made after an immediate send happens.
         * <p>
         * An example of this use case is if a request is received
         * with a 'Expect: 100-continue' header. The message will be sent immediately,
         * and the data will be queued and sent at the end of the stream.
         *
         * @param msg The message which has just been sent due to {@link #mustSendImmediately(FullHttpMessage)}
         * @return A modified copy of the {@code msg} or {@code null} if a copy is not needed.
         */
        FullHttpMessage copyIfNeeded(FullHttpMessage msg);
    }
}
