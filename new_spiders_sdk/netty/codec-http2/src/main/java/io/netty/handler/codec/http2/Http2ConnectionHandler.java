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

import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_STREAM_ID;
import static io.netty.handler.codec.http2.Http2CodecUtil.connectionPrefaceBuf;
import static io.netty.handler.codec.http2.Http2CodecUtil.getEmbeddedHttp2Exception;
import static io.netty.handler.codec.http2.Http2Error.INTERNAL_ERROR;
import static io.netty.handler.codec.http2.Http2Error.NO_ERROR;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.isStreamError;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.lang.String.format;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http2.Http2Exception.CompositeStreamException;
import io.netty.handler.codec.http2.Http2Exception.StreamException;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

/**
 * Provides the default implementation for processing inbound frame events and delegates to a
 * {@link Http2FrameListener}
 * <p>
 * This class will read HTTP/2 frames and delegate the events to a {@link Http2FrameListener}
 * <p>
 * This interface enforces inbound flow control functionality through
 * {@link Http2LocalFlowController}
 */
public class Http2ConnectionHandler extends ByteToMessageDecoder implements Http2LifecycleManager {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2ConnectionHandler.class);
    private final Http2ConnectionDecoder decoder;
    private final Http2ConnectionEncoder encoder;
    private ChannelFutureListener closeListener;
    private BaseDecoder byteDecoder;

    public Http2ConnectionHandler(boolean server, Http2FrameListener listener) {
        this(new DefaultHttp2Connection(server), listener);
    }

    public Http2ConnectionHandler(Http2Connection connection, Http2FrameListener listener) {
        this(connection, new DefaultHttp2FrameReader(), new DefaultHttp2FrameWriter(), listener);
    }

    public Http2ConnectionHandler(Http2Connection connection, Http2FrameReader frameReader,
                                  Http2FrameWriter frameWriter, Http2FrameListener listener) {
        encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
        decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, frameReader, listener);
    }

    /**
     * Constructor for pre-configured encoder and decoder. Just sets the {@code this} as the
     * {@link Http2LifecycleManager} and builds them.
     */
    public Http2ConnectionHandler(Http2ConnectionDecoder decoder,
                                  Http2ConnectionEncoder encoder) {
        this.decoder = checkNotNull(decoder, "decoder");
        this.encoder = checkNotNull(encoder, "encoder");
        if (encoder.connection() != decoder.connection()) {
            throw new IllegalArgumentException("Encoder and Decoder do not share the same connection object");
        }
    }

    public Http2Connection connection() {
        return encoder.connection();
    }

    public Http2ConnectionDecoder decoder() {
        return decoder;
    }

    public Http2ConnectionEncoder encoder() {
        return encoder;
    }

    private boolean prefaceSent() {
        return byteDecoder != null && byteDecoder.prefaceSent();
    }

    /**
     * Handles the client-side (cleartext) upgrade from HTTP to HTTP/2.
     * Reserves local stream 1 for the HTTP/2 response.
     */
    public void onHttpClientUpgrade() throws Http2Exception {
        if (connection().isServer()) {
            throw connectionError(PROTOCOL_ERROR, "Client-side HTTP upgrade requested for a server");
        }
        if (prefaceSent() || decoder.prefaceReceived()) {
            throw connectionError(PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is sent or received");
        }

        // Create a local stream used for the HTTP cleartext upgrade.
        connection().local().createStream(HTTP_UPGRADE_STREAM_ID).open(true);
    }

    /**
     * Handles the server-side (cleartext) upgrade from HTTP to HTTP/2.
     * @param settings the settings for the remote endpoint.
     */
    public void onHttpServerUpgrade(Http2Settings settings) throws Http2Exception {
        if (!connection().isServer()) {
            throw connectionError(PROTOCOL_ERROR, "Server-side HTTP upgrade requested for a client");
        }
        if (prefaceSent() || decoder.prefaceReceived()) {
            throw connectionError(PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is sent or received");
        }

        // Apply the settings but no ACK is necessary.
        encoder.remoteSettings(settings);

        // Create a stream in the half-closed state.
        connection().remote().createStream(HTTP_UPGRADE_STREAM_ID).open(true);
    }

    private abstract class BaseDecoder {
        public abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception { }
        public void channelActive(ChannelHandlerContext ctx) throws Exception { }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            try {
                final Http2Connection connection = connection();
                // Check if there are streams to avoid the overhead of creating the ChannelFuture.
                if (connection.numActiveStreams() > 0) {
                    final ChannelFuture future = ctx.newSucceededFuture();
                    connection.forEachActiveStream(new Http2StreamVisitor() {
                        @Override
                        public boolean visit(Http2Stream stream) throws Http2Exception {
                            closeStream(stream, future);
                            return true;
                        }
                    });
                }
            } finally {
                try {
                    encoder().close();
                } finally {
                    decoder().close();
                }
            }
        }

        /**
         * Determine if the HTTP/2 connection preface been sent.
         */
        public boolean prefaceSent() {
            return true;
        }
    }

    private final class PrefaceDecoder extends BaseDecoder {
        private ByteBuf clientPrefaceString;
        private boolean prefaceSent;

        public PrefaceDecoder(ChannelHandlerContext ctx) {
            clientPrefaceString = clientPrefaceString(encoder.connection());
            // This handler was just added to the context. In case it was handled after
            // the connection became active, send the connection preface now.
            sendPreface(ctx);
        }

        @Override
        public boolean prefaceSent() {
            return prefaceSent;
        }

        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            try {
                if (readClientPrefaceString(in)) {
                    // After the preface is read, it is time to hand over control to the post initialized decoder.
                    Http2ConnectionHandler.this.byteDecoder = new FrameDecoder();
                    Http2ConnectionHandler.this.byteDecoder.decode(ctx, in, out);
                }
            } catch (Throwable e) {
                onException(ctx, e);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // The channel just became active - send the connection preface to the remote endpoint.
            sendPreface(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            cleanup();
            super.channelInactive(ctx);
        }

        /**
         * Releases the {@code clientPrefaceString}. Any active streams will be left in the open.
         */
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            cleanup();
        }

        /**
         * Releases the {@code clientPrefaceString}. Any active streams will be left in the open.
         */
        private void cleanup() {
            if (clientPrefaceString != null) {
                clientPrefaceString.release();
                clientPrefaceString = null;
            }
        }

        /**
         * Decodes the client connection preface string from the input buffer.
         *
         * @return {@code true} if processing of the client preface string is complete. Since client preface strings can
         *         only be received by servers, returns true immediately for client endpoints.
         */
        private boolean readClientPrefaceString(ByteBuf in) throws Http2Exception {
            if (clientPrefaceString == null) {
                return true;
            }

            int prefaceRemaining = clientPrefaceString.readableBytes();
            int bytesRead = Math.min(in.readableBytes(), prefaceRemaining);

            // If the input so far doesn't match the preface, break the connection.
            if (bytesRead == 0 || !ByteBufUtil.equals(in, in.readerIndex(),
                    clientPrefaceString, clientPrefaceString.readerIndex(), bytesRead)) {
                throw connectionError(PROTOCOL_ERROR, "HTTP/2 client preface string missing or corrupt.");
            }
            in.skipBytes(bytesRead);
            clientPrefaceString.skipBytes(bytesRead);

            if (!clientPrefaceString.isReadable()) {
                // Entire preface has been read.
                clientPrefaceString.release();
                clientPrefaceString = null;
                return true;
            }
            return false;
        }

        /**
         * Sends the HTTP/2 connection preface upon establishment of the connection, if not already sent.
         */
        private void sendPreface(ChannelHandlerContext ctx) {
            if (prefaceSent || !ctx.channel().isActive()) {
                return;
            }

            prefaceSent = true;

            if (!connection().isServer()) {
                // Clients must send the preface string as the first bytes on the connection.
                ctx.write(connectionPrefaceBuf()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

            // Both client and server must send their initial settings.
            encoder.writeSettings(ctx, decoder.localSettings(), ctx.newPromise()).addListener(
                    ChannelFutureListener.CLOSE_ON_FAILURE);
        }
    }

    private final class FrameDecoder extends BaseDecoder {
        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            try {
                decoder.decodeFrame(ctx, in, out);
            } catch (Throwable e) {
                onException(ctx, e);
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // Initialize the encoder and decoder.
        encoder.lifecycleManager(this);
        decoder.lifecycleManager(this);
        byteDecoder = new PrefaceDecoder(ctx);
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        if (byteDecoder != null) {
            byteDecoder.handlerRemoved(ctx);
            byteDecoder = null;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (byteDecoder == null) {
            byteDecoder = new PrefaceDecoder(ctx);
        }
        byteDecoder.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (byteDecoder != null) {
            byteDecoder.channelInactive(ctx);
            byteDecoder = null;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byteDecoder.decode(ctx, in, out);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        // Avoid NotYetConnectedException
        if (!ctx.channel().isActive()) {
            ctx.close(promise);
            return;
        }

        ChannelFuture future = goAway(ctx, null);

        // If there are no active streams, close immediately after the send is complete.
        // Otherwise wait until all streams are inactive.
        if (connection().numActiveStreams() == 0) {
            future.addListener(new ClosingChannelFutureListener(ctx, promise));
        } else {
            closeListener = new ClosingChannelFutureListener(ctx, promise);
        }
    }

    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * Handles {@link Http2Exception} objects that were thrown from other handlers. Ignores all other exceptions.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (getEmbeddedHttp2Exception(cause) != null) {
            // Some exception in the causality chain is an Http2Exception - handle it.
            onException(ctx, cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * Closes the local side of the given stream. If this causes the stream to be closed, adds a
     * hook to close the channel after the given future completes.
     *
     * @param stream the stream to be half closed.
     * @param future If closing, the future after which to close the channel.
     */
    @Override
    public void closeStreamLocal(Http2Stream stream, ChannelFuture future) {
        switch (stream.state()) {
            case HALF_CLOSED_LOCAL:
            case OPEN:
                stream.closeLocalSide();
                break;
            default:
                closeStream(stream, future);
                break;
        }
    }

    /**
     * Closes the remote side of the given stream. If this causes the stream to be closed, adds a
     * hook to close the channel after the given future completes.
     *
     * @param stream the stream to be half closed.
     * @param future If closing, the future after which to close the channel.
     */
    @Override
    public void closeStreamRemote(Http2Stream stream, ChannelFuture future) {
        switch (stream.state()) {
            case HALF_CLOSED_REMOTE:
            case OPEN:
                stream.closeRemoteSide();
                break;
            default:
                closeStream(stream, future);
                break;
        }
    }

    @Override
    public void closeStream(final Http2Stream stream, ChannelFuture future) {
        stream.close();

        future.addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            // If this connection is closing and there are no longer any
            // active streams, close after the current operation completes.
            if (closeListener != null && connection().numActiveStreams() == 0) {
                ChannelFutureListener closeListener = Http2ConnectionHandler.this.closeListener;
                // This method could be called multiple times
                // and we don't want to notify the closeListener multiple times.
                Http2ConnectionHandler.this.closeListener = null;
                closeListener.operationComplete(future);
            }
          }
        });
    }

    /**
     * Central handler for all exceptions caught during HTTP/2 processing.
     */
    @Override
    public void onException(ChannelHandlerContext ctx, Throwable cause) {
        Http2Exception embedded = getEmbeddedHttp2Exception(cause);
        if (isStreamError(embedded)) {
            onStreamError(ctx, cause, (StreamException) embedded);
        } else if (embedded instanceof CompositeStreamException) {
            CompositeStreamException compositException = (CompositeStreamException) embedded;
            for (StreamException streamException : compositException) {
                onStreamError(ctx, cause, streamException);
            }
        } else {
            onConnectionError(ctx, cause, embedded);
        }
    }

    /**
     * Handler for a connection error. Sends a GO_AWAY frame to the remote endpoint. Once all
     * streams are closed, the connection is shut down.
     *
     * @param ctx the channel context
     * @param cause the exception that was caught
     * @param http2Ex the {@link Http2Exception} that is embedded in the causality chain. This may
     *            be {@code null} if it's an unknown exception.
     */
    protected void onConnectionError(ChannelHandlerContext ctx, Throwable cause, Http2Exception http2Ex) {
        if (http2Ex == null) {
            http2Ex = new Http2Exception(INTERNAL_ERROR, cause.getMessage(), cause);
        }
        goAway(ctx, http2Ex).addListener(new ClosingChannelFutureListener(ctx, ctx.newPromise()));
    }

    /**
     * Handler for a stream error. Sends a {@code RST_STREAM} frame to the remote endpoint and closes the
     * stream.
     *
     * @param ctx the channel context
     * @param cause the exception that was caught
     * @param http2Ex the {@link StreamException} that is embedded in the causality chain.
     */
    protected void onStreamError(ChannelHandlerContext ctx, Throwable cause, StreamException http2Ex) {
        resetStream(ctx, http2Ex.streamId(), http2Ex.error().code(), ctx.newPromise());
    }

    protected Http2FrameWriter frameWriter() {
        return encoder().frameWriter();
    }

    @Override
    public ChannelFuture resetStream(final ChannelHandlerContext ctx, int streamId, long errorCode,
            final ChannelPromise promise) {
        final Http2Stream stream = connection().stream(streamId);
        if (stream == null || stream.isResetSent()) {
            // Don't write a RST_STREAM frame if we are not aware of the stream, or if we have already written one.
            return promise.setSuccess();
        }

        ChannelFuture future = frameWriter().writeRstStream(ctx, streamId, errorCode, promise);
        ctx.flush();

        // Synchronously set the resetSent flag to prevent any subsequent calls
        // from resulting in multiple reset frames being sent.
        stream.resetSent();

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    closeStream(stream, promise);
                } else {
                    // The connection will be closed and so no need to change the resetSent flag to false.
                    onConnectionError(ctx, future.cause(), null);
                }
            }
        });

        return future;
    }

    @Override
    public ChannelFuture goAway(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode,
                                final ByteBuf debugData, ChannelPromise promise) {
        try {
            final Http2Connection connection = connection();
            if (connection.goAwaySent() && connection.remote().lastKnownStream() < lastStreamId) {
                throw connectionError(PROTOCOL_ERROR, "Last stream identifier must not increase between " +
                                                      "sending multiple GOAWAY frames (was '%d', is '%d').",
                                                      connection.remote().lastKnownStream(),
                                                      lastStreamId);
            }
            connection.goAwaySent(lastStreamId, errorCode, debugData);

            ChannelFuture future = frameWriter().writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
            ctx.flush();

            future.addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        String msg = format("Sending GOAWAY failed: lastStreamId '%d', errorCode '%d', " +
                                            "debugData '%s'.", lastStreamId, errorCode, debugData);
                        logger.error(msg, future.cause());
                        ctx.channel().close();
                    }
                }
            });

            return future;
        } catch (Http2Exception e) {
            debugData.release();
            return promise.setFailure(e);
        }
    }

    /**
     * Close the remote endpoint with with a {@code GO_AWAY} frame.
     */
    private ChannelFuture goAway(ChannelHandlerContext ctx, Http2Exception cause) {
        long errorCode = cause != null ? cause.error().code() : NO_ERROR.code();
        ByteBuf debugData = Http2CodecUtil.toByteBuf(ctx, cause);
        int lastKnownStream = connection().remote().lastStreamCreated();
        return goAway(ctx, lastKnownStream, errorCode, debugData, ctx.newPromise());
    }

    /**
     * Returns the client preface string if this is a client connection, otherwise returns {@code null}.
     */
    private static ByteBuf clientPrefaceString(Http2Connection connection) {
        return connection.isServer() ? connectionPrefaceBuf() : null;
    }

    /**
     * Closes the channel when the future completes.
     */
    private static final class ClosingChannelFutureListener implements ChannelFutureListener {
        private final ChannelHandlerContext ctx;
        private final ChannelPromise promise;

        ClosingChannelFutureListener(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.ctx = ctx;
            this.promise = promise;
        }

        @Override
        public void operationComplete(ChannelFuture sentGoAwayFuture) throws Exception {
            ctx.close(promise);
        }
    }
}
