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

import static io.netty.util.CharsetUtil.UTF_8;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.EventExecutor;

/**
 * Constants and utility method used for encoding/decoding HTTP2 frames.
 */
public final class Http2CodecUtil {

    private static final byte[] CONNECTION_PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(UTF_8);
    private static final byte[] EMPTY_PING = new byte[8];

    public static final int CONNECTION_STREAM_ID = 0;
    public static final int HTTP_UPGRADE_STREAM_ID = 1;
    public static final String HTTP_UPGRADE_SETTINGS_HEADER = "HTTP2-Settings";
    public static final String HTTP_UPGRADE_PROTOCOL_NAME = "h2c-17";
    public static final String TLS_UPGRADE_PROTOCOL_NAME = "h2-17";

    public static final int PING_FRAME_PAYLOAD_LENGTH = 8;
    public static final short MAX_UNSIGNED_BYTE = 0xFF;
    public static final int MAX_UNSIGNED_SHORT = 0xFFFF;
    public static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;
    public static final int FRAME_HEADER_LENGTH = 9;
    public static final int SETTING_ENTRY_LENGTH = 6;
    public static final int PRIORITY_ENTRY_LENGTH = 5;
    public static final int INT_FIELD_LENGTH = 4;
    public static final short MAX_WEIGHT = 256;
    public static final short MIN_WEIGHT = 1;

    private static final int MAX_PADDING_LENGTH_LENGTH = 1;
    public static final int DATA_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH;
    public static final int HEADERS_FRAME_HEADER_LENGTH =
            FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH + 1;
    public static final int PRIORITY_FRAME_LENGTH = FRAME_HEADER_LENGTH + PRIORITY_ENTRY_LENGTH;
    public static final int RST_STREAM_FRAME_LENGTH = FRAME_HEADER_LENGTH + INT_FIELD_LENGTH;
    public static final int PUSH_PROMISE_FRAME_HEADER_LENGTH =
            FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH;
    public static final int GO_AWAY_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH + 2 * INT_FIELD_LENGTH;
    public static final int WINDOW_UPDATE_FRAME_LENGTH = FRAME_HEADER_LENGTH + INT_FIELD_LENGTH;
    public static final int CONTINUATION_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH;

    public static final char SETTINGS_HEADER_TABLE_SIZE = 1;
    public static final char SETTINGS_ENABLE_PUSH = 2;
    public static final char SETTINGS_MAX_CONCURRENT_STREAMS = 3;
    public static final char SETTINGS_INITIAL_WINDOW_SIZE = 4;
    public static final char SETTINGS_MAX_FRAME_SIZE = 5;
    public static final char SETTINGS_MAX_HEADER_LIST_SIZE = 6;
    public static final int NUM_STANDARD_SETTINGS = 6;

    public static final int MAX_HEADER_TABLE_SIZE = Integer.MAX_VALUE; // Size limited by HPACK library
    public static final long MAX_CONCURRENT_STREAMS = MAX_UNSIGNED_INT;
    public static final int MAX_INITIAL_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int MAX_FRAME_SIZE_LOWER_BOUND = 0x4000;
    public static final int MAX_FRAME_SIZE_UPPER_BOUND = 0xFFFFFF;
    public static final long MAX_HEADER_LIST_SIZE = Long.MAX_VALUE;

    public static final long MIN_HEADER_TABLE_SIZE = 0;
    public static final long MIN_CONCURRENT_STREAMS = 0;
    public static final int MIN_INITIAL_WINDOW_SIZE = 0;
    public static final long MIN_HEADER_LIST_SIZE = 0;

    public static final int DEFAULT_WINDOW_SIZE = 65535;
    public static final boolean DEFAULT_ENABLE_PUSH = true;
    public static final short DEFAULT_PRIORITY_WEIGHT = 16;
    public static final int DEFAULT_HEADER_TABLE_SIZE = 4096;
    public static final int DEFAULT_MAX_HEADER_SIZE = 8192;
    public static final int DEFAULT_MAX_FRAME_SIZE = MAX_FRAME_SIZE_LOWER_BOUND;

    /**
     * Indicates whether or not the given value for max frame size falls within the valid range.
     */
    public static boolean isMaxFrameSizeValid(int maxFrameSize) {
        return maxFrameSize >= MAX_FRAME_SIZE_LOWER_BOUND && maxFrameSize <= MAX_FRAME_SIZE_UPPER_BOUND;
    }

    /**
     * Returns a buffer containing the the {@link #CONNECTION_PREFACE}.
     */
    public static ByteBuf connectionPrefaceBuf() {
        // Return a duplicate so that modifications to the reader index will not affect the original buffer.
        return Unpooled.wrappedBuffer(CONNECTION_PREFACE);
    }

    /**
     * Returns a buffer filled with all zeros that is the appropriate length for a PING frame.
     */
    public static ByteBuf emptyPingBuf() {
        // Return a duplicate so that modifications to the reader index will not affect the original buffer.
        return Unpooled.wrappedBuffer(EMPTY_PING);
    }

    /**
     * Iteratively looks through the causaility chain for the given exception and returns the first
     * {@link Http2Exception} or {@code null} if none.
     */
    public static Http2Exception getEmbeddedHttp2Exception(Throwable cause) {
        while (cause != null) {
            if (cause instanceof Http2Exception) {
                return (Http2Exception) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * Creates a buffer containing the error message from the given exception. If the cause is
     * {@code null} returns an empty buffer.
     */
    public static ByteBuf toByteBuf(ChannelHandlerContext ctx, Throwable cause) {
        if (cause == null || cause.getMessage() == null) {
            return Unpooled.EMPTY_BUFFER;
        }

        // Create the debug message.
        byte[] msg = cause.getMessage().getBytes(UTF_8);
        ByteBuf debugData = ctx.alloc().buffer(msg.length);
        debugData.writeBytes(msg);
        return debugData;
    }

    /**
     * Reads a big-endian (31-bit) integer from the buffer.
     */
    public static int readUnsignedInt(ByteBuf buf) {
        return (buf.readByte() & 0x7F) << 24 | (buf.readByte() & 0xFF) << 16
                | (buf.readByte() & 0xFF) << 8 | buf.readByte() & 0xFF;
    }

    /**
     * Writes a big-endian (32-bit) unsigned integer to the buffer.
     */
    public static void writeUnsignedInt(long value, ByteBuf out) {
        out.writeByte((int) (value >> 24 & 0xFF));
        out.writeByte((int) (value >> 16 & 0xFF));
        out.writeByte((int) (value >> 8 & 0xFF));
        out.writeByte((int) (value & 0xFF));
    }

    /**
     * Writes a big-endian (16-bit) unsigned integer to the buffer.
     */
    public static void writeUnsignedShort(int value, ByteBuf out) {
        out.writeByte(value >> 8 & 0xFF);
        out.writeByte(value & 0xFF);
    }

    /**
     * Writes an HTTP/2 frame header to the output buffer.
     */
    public static void writeFrameHeader(ByteBuf out, int payloadLength, byte type,
            Http2Flags flags, int streamId) {
        out.ensureWritable(FRAME_HEADER_LENGTH + payloadLength);
        writeFrameHeaderInternal(out, payloadLength, type, flags, streamId);
    }

    static void writeFrameHeaderInternal(ByteBuf out, int payloadLength, byte type,
            Http2Flags flags, int streamId) {
        out.writeMedium(payloadLength);
        out.writeByte(type);
        out.writeByte(flags.value());
        out.writeInt(streamId);
    }

    /**
     * Provides the ability to associate the outcome of multiple {@link ChannelPromise}
     * objects into a single {@link ChannelPromise} object.
     */
    static class SimpleChannelPromiseAggregator extends DefaultChannelPromise {
        private final ChannelPromise promise;
        private int expectedCount;
        private int successfulCount;
        private int failureCount;
        private boolean doneAllocating;

        SimpleChannelPromiseAggregator(ChannelPromise promise, Channel c, EventExecutor e) {
            super(c, e);
            assert promise != null;
            this.promise = promise;
        }

        /**
         * Allocate a new promise which will be used to aggregate the overall success of this promise aggregator.
         * @return A new promise which will be aggregated.
         * {@code null} if {@link #doneAllocatingPromises()} was previously called.
         */
        public ChannelPromise newPromise() {
            if (doneAllocating) {
                throw new IllegalStateException("Done allocating. No more promises can be allocated.");
            }
            ++expectedCount;
            return this;
        }

        /**
         * Signify that no more {@link #newPromise()} allocations will be made.
         * The aggregation can not be successful until this method is called.
         * @return The promise that is the aggregation of all promises allocated with {@link #newPromise()}.
         */
        public ChannelPromise doneAllocatingPromises() {
            if (!doneAllocating) {
                doneAllocating = true;
                if (successfulCount == expectedCount) {
                    promise.setSuccess();
                    return super.setSuccess();
                }
            }
            return this;
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            if (allowNotificationEvent()) {
                ++failureCount;
                if (failureCount == 1) {
                    promise.tryFailure(cause);
                    return super.tryFailure(cause);
                }
                // TODO: We break the interface a bit here.
                // Multiple failure events can be processed without issue because this is an aggregation.
                return true;
            }
            return false;
        }

        /**
         * Fail this object if it has not already been failed.
         * <p>
         * This method will NOT throw an {@link IllegalStateException} if called multiple times
         * because that may be expected.
         */
        @Override
        public ChannelPromise setFailure(Throwable cause) {
            if (allowNotificationEvent()) {
                ++failureCount;
                if (failureCount == 1) {
                    promise.setFailure(cause);
                    return super.setFailure(cause);
                }
            }
            return this;
        }

        private boolean allowNotificationEvent() {
            return successfulCount + failureCount < expectedCount;
        }

        @Override
        public ChannelPromise setSuccess(Void result) {
            if (allowNotificationEvent()) {
                ++successfulCount;
                if (successfulCount == expectedCount && doneAllocating) {
                    promise.setSuccess(result);
                    return super.setSuccess(result);
                }
            }
            return this;
        }

        @Override
        public boolean trySuccess(Void result) {
            if (allowNotificationEvent()) {
                ++successfulCount;
                if (successfulCount == expectedCount && doneAllocating) {
                    promise.trySuccess(result);
                    return super.trySuccess(result);
                }
                // TODO: We break the interface a bit here.
                // Multiple success events can be processed without issue because this is an aggregation.
                return true;
            }
            return false;
        }
    }

    private Http2CodecUtil() { }
}
