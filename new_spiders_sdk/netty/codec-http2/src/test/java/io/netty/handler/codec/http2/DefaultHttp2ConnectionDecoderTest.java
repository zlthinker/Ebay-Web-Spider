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

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http2.Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT;
import static io.netty.handler.codec.http2.Http2CodecUtil.emptyPingBuf;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Stream.State.IDLE;
import static io.netty.handler.codec.http2.Http2Stream.State.OPEN;
import static io.netty.handler.codec.http2.Http2Stream.State.RESERVED_REMOTE;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http2.Http2Exception.ClosedStreamCreationException;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link DefaultHttp2ConnectionDecoder}.
 */
public class DefaultHttp2ConnectionDecoderTest {
    private static final int STREAM_ID = 1;
    private static final int PUSH_STREAM_ID = 2;
    private static final int STREAM_DEPENDENCY_ID = 3;

    private Http2ConnectionDecoder decoder;

    @Mock
    private Http2Connection connection;

    @Mock
    private Http2Connection.Endpoint<Http2RemoteFlowController> remote;

    @Mock
    private Http2Connection.Endpoint<Http2LocalFlowController> local;

    @Mock
    private Http2LocalFlowController localFlow;

    @Mock
    private Http2RemoteFlowController remoteFlow;

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    private ChannelPromise promise;

    @Mock
    private ChannelFuture future;

    @Mock
    private Http2Stream stream;

    @Mock
    private Http2Stream pushStream;

    @Mock
    private Http2FrameListener listener;

    @Mock
    private Http2FrameReader reader;

    @Mock
    private Http2ConnectionEncoder encoder;

    @Mock
    private Http2LifecycleManager lifecycleManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        promise = new DefaultChannelPromise(channel);

        when(channel.isActive()).thenReturn(true);
        when(stream.id()).thenReturn(STREAM_ID);
        when(stream.state()).thenReturn(OPEN);
        when(stream.open(anyBoolean())).thenReturn(stream);
        when(pushStream.id()).thenReturn(PUSH_STREAM_ID);
        doAnswer(new Answer<Http2Stream>() {
            @Override
            public Http2Stream answer(InvocationOnMock in) throws Throwable {
                Http2StreamVisitor visitor = in.getArgumentAt(0, Http2StreamVisitor.class);
                if (!visitor.visit(stream)) {
                    return stream;
                }
                return null;
            }
        }).when(connection).forEachActiveStream(any(Http2StreamVisitor.class));
        when(connection.stream(STREAM_ID)).thenReturn(stream);
        when(connection.requireStream(STREAM_ID)).thenReturn(stream);
        when(connection.local()).thenReturn(local);
        when(local.flowController()).thenReturn(localFlow);
        when(encoder.flowController()).thenReturn(remoteFlow);
        when(connection.remote()).thenReturn(remote);
        when(local.createStream(eq(STREAM_ID))).thenReturn(stream);
        when(local.reservePushStream(eq(PUSH_STREAM_ID), eq(stream))).thenReturn(pushStream);
        when(remote.createStream(eq(STREAM_ID))).thenReturn(stream);
        when(remote.reservePushStream(eq(PUSH_STREAM_ID), eq(stream))).thenReturn(pushStream);
        when(ctx.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
        when(ctx.channel()).thenReturn(channel);
        when(ctx.newSucceededFuture()).thenReturn(future);
        when(ctx.newPromise()).thenReturn(promise);
        when(ctx.write(any())).thenReturn(future);

        decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, reader, listener);
        decoder.lifecycleManager(lifecycleManager);

        // Simulate receiving the initial settings from the remote endpoint.
        decode().onSettingsRead(ctx, new Http2Settings());
        verify(listener).onSettingsRead(eq(ctx), eq(new Http2Settings()));
        assertTrue(decoder.prefaceReceived());
        verify(encoder).writeSettingsAck(eq(ctx), eq(promise));

        // Simulate receiving the SETTINGS ACK for the initial settings.
        decode().onSettingsAckRead(ctx);
    }

    @Test
    public void dataReadAfterGoAwayShouldApplyFlowControl() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        final ByteBuf data = dummyData();
        int padding = 10;
        int processedBytes = data.readableBytes() + padding;
        mockFlowControl(processedBytes);
        try {
            decode().onDataRead(ctx, STREAM_ID, data, padding, true);
            verify(localFlow).receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(padding), eq(true));
            verify(localFlow).consumeBytes(eq(ctx), eq(stream), eq(processedBytes));

            // Verify that the event was absorbed and not propagated to the observer.
            verify(listener, never()).onDataRead(eq(ctx), anyInt(), any(ByteBuf.class), anyInt(), anyBoolean());
        } finally {
            data.release();
        }
    }

    @Test
    public void emptyDataFrameShouldApplyFlowControl() throws Exception {
        final ByteBuf data = EMPTY_BUFFER;
        int padding = 0;
        int processedBytes = data.readableBytes() + padding;
        mockFlowControl(processedBytes);
        try {
            decode().onDataRead(ctx, STREAM_ID, data, padding, true);
            verify(localFlow).receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(padding), eq(true));

            // No bytes were consumed, so there's no window update needed.
            verify(localFlow, never()).consumeBytes(eq(ctx), eq(stream), eq(processedBytes));

            // Verify that the empty data event was propagated to the observer.
            verify(listener).onDataRead(eq(ctx), eq(STREAM_ID), eq(data), eq(padding), eq(true));
        } finally {
            data.release();
        }
    }

    @Test(expected = Http2Exception.class)
    public void dataReadForStreamInInvalidStateShouldThrow() throws Exception {
        // Throw an exception when checking stream state.
        when(stream.state()).thenReturn(Http2Stream.State.CLOSED);
        final ByteBuf data = dummyData();
        try {
            decode().onDataRead(ctx, STREAM_ID, data, 10, true);
        } finally {
            data.release();
        }
    }

    @Test
    public void dataReadAfterGoAwayForStreamInInvalidStateShouldIgnore() throws Exception {
        // Throw an exception when checking stream state.
        when(stream.state()).thenReturn(Http2Stream.State.CLOSED);
        when(connection.goAwaySent()).thenReturn(true);
        final ByteBuf data = dummyData();
        try {
            decode().onDataRead(ctx, STREAM_ID, data, 10, true);
            verify(localFlow).receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(10), eq(true));
            verify(listener, never()).onDataRead(eq(ctx), anyInt(), any(ByteBuf.class), anyInt(), anyBoolean());
        } finally {
            data.release();
        }
    }

    @Test
    public void dataReadAfterRstStreamForStreamInInvalidStateShouldIgnore() throws Exception {
        // Throw an exception when checking stream state.
        when(stream.state()).thenReturn(Http2Stream.State.CLOSED);
        when(stream.isResetSent()).thenReturn(true);
        final ByteBuf data = dummyData();
        try {
            decode().onDataRead(ctx, STREAM_ID, data, 10, true);
            verify(localFlow).receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(10), eq(true));
            verify(listener, never()).onDataRead(eq(ctx), anyInt(), any(ByteBuf.class), anyInt(), anyBoolean());
        } finally {
            data.release();
        }
    }

    @Test
    public void dataReadWithEndOfStreamShouldcloseStreamRemote() throws Exception {
        final ByteBuf data = dummyData();
        try {
            decode().onDataRead(ctx, STREAM_ID, data, 10, true);
            verify(localFlow).receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(10), eq(true));
            verify(lifecycleManager).closeStreamRemote(eq(stream), eq(future));
            verify(listener).onDataRead(eq(ctx), eq(STREAM_ID), eq(data), eq(10), eq(true));
        } finally {
            data.release();
        }
    }

    @Test
    public void errorDuringDeliveryShouldReturnCorrectNumberOfBytes() throws Exception {
        final ByteBuf data = dummyData();
        final int padding = 10;
        final AtomicInteger unprocessed = new AtomicInteger(data.readableBytes() + padding);
        doAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock in) throws Throwable {
                return unprocessed.get();
            }
        }).when(localFlow).unconsumedBytes(eq(stream));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock in) throws Throwable {
                int delta = (Integer) in.getArguments()[2];
                int newValue = unprocessed.addAndGet(-delta);
                if (newValue < 0) {
                    throw new RuntimeException("Returned too many bytes");
                }
                return null;
            }
        }).when(localFlow).consumeBytes(eq(ctx), eq(stream), anyInt());
        // When the listener callback is called, process a few bytes and then throw.
        doAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock in) throws Throwable {
                localFlow.consumeBytes(ctx, stream, 4);
                throw new RuntimeException("Fake Exception");
            }
        }).when(listener).onDataRead(eq(ctx), eq(STREAM_ID), any(ByteBuf.class), eq(10), eq(true));
        try {
            decode().onDataRead(ctx, STREAM_ID, data, padding, true);
            fail("Expected exception");
        } catch (RuntimeException cause) {
            verify(localFlow)
                    .receiveFlowControlledFrame(eq(ctx), eq(stream), eq(data), eq(padding), eq(true));
            verify(lifecycleManager).closeStreamRemote(eq(stream), eq(future));
            verify(listener).onDataRead(eq(ctx), eq(STREAM_ID), eq(data), eq(padding), eq(true));
            assertEquals(0, localFlow.unconsumedBytes(stream));
        } finally {
            data.release();
        }
    }

    @Test
    public void headersReadAfterGoAwayShouldBeIgnored() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, 0, false);
        verify(remote, never()).createStream(eq(STREAM_ID));
        verify(stream, never()).open(anyBoolean());

        // Verify that the event was absorbed and not propagated to the oberver.
        verify(listener, never()).onHeadersRead(eq(ctx), anyInt(), any(Http2Headers.class), anyInt(), anyBoolean());
        verify(remote, never()).createStream(anyInt());
        verify(stream, never()).open(anyBoolean());
    }

    @Test
    public void headersReadForUnknownStreamShouldCreateStream() throws Exception {
        final int streamId = 5;
        when(remote.createStream(eq(streamId))).thenReturn(stream);
        decode().onHeadersRead(ctx, streamId, EmptyHttp2Headers.INSTANCE, 0, false);
        verify(remote).createStream(eq(streamId));
        verify(stream).open(eq(false));
        verify(listener).onHeadersRead(eq(ctx), eq(streamId), eq(EmptyHttp2Headers.INSTANCE), eq(0),
                eq(DEFAULT_PRIORITY_WEIGHT), eq(false), eq(0), eq(false));
    }

    @Test
    public void headersReadForUnknownStreamShouldCreateHalfClosedStream() throws Exception {
        final int streamId = 5;
        when(remote.createStream(eq(streamId))).thenReturn(stream);
        decode().onHeadersRead(ctx, streamId, EmptyHttp2Headers.INSTANCE, 0, true);
        verify(remote).createStream(eq(streamId));
        verify(stream).open(eq(true));
        verify(listener).onHeadersRead(eq(ctx), eq(streamId), eq(EmptyHttp2Headers.INSTANCE), eq(0),
                eq(DEFAULT_PRIORITY_WEIGHT), eq(false), eq(0), eq(true));
    }

    @Test
    public void headersReadForPromisedStreamShouldHalfOpenStream() throws Exception {
        when(stream.state()).thenReturn(RESERVED_REMOTE);
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, 0, false);
        verify(stream).open(false);
        verify(listener).onHeadersRead(eq(ctx), eq(STREAM_ID), eq(EmptyHttp2Headers.INSTANCE), eq(0),
                eq(DEFAULT_PRIORITY_WEIGHT), eq(false), eq(0), eq(false));
    }

    @Test
    public void headersReadForPromisedStreamShouldCloseStream() throws Exception {
        when(stream.state()).thenReturn(RESERVED_REMOTE);
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, 0, true);
        verify(stream).open(true);
        verify(lifecycleManager).closeStreamRemote(eq(stream), eq(future));
        verify(listener).onHeadersRead(eq(ctx), eq(STREAM_ID), eq(EmptyHttp2Headers.INSTANCE), eq(0),
                eq(DEFAULT_PRIORITY_WEIGHT), eq(false), eq(0), eq(true));
    }

    @Test
    public void headersDependencyNotCreatedShouldCreateAndSucceed() throws Exception {
        final short weight = 1;
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, STREAM_DEPENDENCY_ID,
                weight, true, 0, true);
        verify(listener).onHeadersRead(eq(ctx), eq(STREAM_ID), eq(EmptyHttp2Headers.INSTANCE), eq(STREAM_DEPENDENCY_ID),
                eq(weight), eq(true), eq(0), eq(true));
        verify(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq(weight), eq(true));
        verify(lifecycleManager).closeStreamRemote(eq(stream), any(ChannelFuture.class));
    }

    @Test
    public void headersDependencyPreviouslyCreatedStreamShouldSucceed() throws Exception {
        final short weight = 1;
        doAnswer(new Answer<Http2Stream>() {
            @Override
            public Http2Stream answer(InvocationOnMock in) throws Throwable {
                throw new ClosedStreamCreationException(Http2Error.INTERNAL_ERROR);
            }
        }).when(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq(weight), eq(true));
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, STREAM_DEPENDENCY_ID,
                weight, true, 0, true);
        verify(listener).onHeadersRead(eq(ctx), eq(STREAM_ID), eq(EmptyHttp2Headers.INSTANCE), eq(STREAM_DEPENDENCY_ID),
                eq(weight), eq(true), eq(0), eq(true));
        verify(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq(weight), eq(true));
        verify(lifecycleManager).closeStreamRemote(eq(stream), any(ChannelFuture.class));
    }

    @Test(expected = RuntimeException.class)
    public void headersDependencyInvalidStreamShouldFail() throws Exception {
        final short weight = 1;
        doAnswer(new Answer<Http2Stream>() {
            @Override
            public Http2Stream answer(InvocationOnMock in) throws Throwable {
                throw new RuntimeException("Fake Exception");
            }
        }).when(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq(weight), eq(true));
        decode().onHeadersRead(ctx, STREAM_ID, EmptyHttp2Headers.INSTANCE, STREAM_DEPENDENCY_ID,
                weight, true, 0, true);
        verify(listener, never()).onHeadersRead(any(ChannelHandlerContext.class), anyInt(), any(Http2Headers.class),
                anyInt(), anyShort(), anyBoolean(), anyInt(), anyBoolean());
        verify(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq(weight), eq(true));
        verify(lifecycleManager, never()).closeStreamRemote(eq(stream), any(ChannelFuture.class));
    }

    @Test
    public void pushPromiseReadAfterGoAwayShouldBeIgnored() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        decode().onPushPromiseRead(ctx, STREAM_ID, PUSH_STREAM_ID, EmptyHttp2Headers.INSTANCE, 0);
        verify(remote, never()).reservePushStream(anyInt(), any(Http2Stream.class));
        verify(listener, never()).onPushPromiseRead(eq(ctx), anyInt(), anyInt(), any(Http2Headers.class), anyInt());
    }

    @Test
    public void pushPromiseReadShouldSucceed() throws Exception {
        decode().onPushPromiseRead(ctx, STREAM_ID, PUSH_STREAM_ID, EmptyHttp2Headers.INSTANCE, 0);
        verify(remote).reservePushStream(eq(PUSH_STREAM_ID), eq(stream));
        verify(listener).onPushPromiseRead(eq(ctx), eq(STREAM_ID), eq(PUSH_STREAM_ID),
                eq(EmptyHttp2Headers.INSTANCE), eq(0));
    }

    @Test
    public void priorityReadAfterGoAwayShouldBeIgnored() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        decode().onPriorityRead(ctx, STREAM_ID, 0, (short) 255, true);
        verify(stream, never()).setPriority(anyInt(), anyShort(), anyBoolean());
        verify(listener, never()).onPriorityRead(eq(ctx), anyInt(), anyInt(), anyShort(), anyBoolean());
    }

    @Test
    public void priorityReadShouldSucceed() throws Exception {
        when(connection.stream(STREAM_ID)).thenReturn(null);
        when(connection.requireStream(STREAM_ID)).thenReturn(null);
        decode().onPriorityRead(ctx, STREAM_ID, STREAM_DEPENDENCY_ID, (short) 255, true);
        verify(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
        verify(listener).onPriorityRead(eq(ctx), eq(STREAM_ID), eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
        verify(remote).createStream(STREAM_ID);
        verify(stream, never()).open(anyBoolean());
    }

    @Test
    public void priorityReadOnPreviouslyExistingStreamShouldSucceed() throws Exception {
        doAnswer(new Answer<Http2Stream>() {
            @Override
            public Http2Stream answer(InvocationOnMock in) throws Throwable {
                throw new ClosedStreamCreationException(Http2Error.INTERNAL_ERROR);
            }
        }).when(remote).createStream(eq(STREAM_ID));
        when(connection.stream(STREAM_ID)).thenReturn(null);
        when(connection.requireStream(STREAM_ID)).thenReturn(null);
        // Just return the stream object as the connection stream to ensure the dependent stream "exists"
        when(connection.stream(STREAM_DEPENDENCY_ID)).thenReturn(stream);
        when(connection.requireStream(STREAM_DEPENDENCY_ID)).thenReturn(stream);
        decode().onPriorityRead(ctx, STREAM_ID, STREAM_DEPENDENCY_ID, (short) 255, true);
        verify(stream, never()).setPriority(anyInt(), anyShort(), anyBoolean());
        verify(listener).onPriorityRead(eq(ctx), eq(STREAM_ID), eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
        verify(remote).createStream(STREAM_ID);
    }

    @Test
    public void priorityReadOnPreviouslyParentExistingStreamShouldSucceed() throws Exception {
        doAnswer(new Answer<Http2Stream>() {
            @Override
            public Http2Stream answer(InvocationOnMock in) throws Throwable {
                throw new ClosedStreamCreationException(Http2Error.INTERNAL_ERROR);
            }
        }).when(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
        when(connection.stream(STREAM_ID)).thenReturn(stream);
        when(connection.requireStream(STREAM_ID)).thenReturn(stream);
        decode().onPriorityRead(ctx, STREAM_ID, STREAM_DEPENDENCY_ID, (short) 255, true);
        verify(stream).setPriority(eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
        verify(listener).onPriorityRead(eq(ctx), eq(STREAM_ID), eq(STREAM_DEPENDENCY_ID), eq((short) 255), eq(true));
    }

    @Test
    public void windowUpdateReadAfterGoAwayShouldBeIgnored() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        decode().onWindowUpdateRead(ctx, STREAM_ID, 10);
        verify(remoteFlow, never()).incrementWindowSize(eq(ctx), any(Http2Stream.class), anyInt());
        verify(listener, never()).onWindowUpdateRead(eq(ctx), anyInt(), anyInt());
    }

    @Test(expected = Http2Exception.class)
    public void windowUpdateReadForUnknownStreamShouldThrow() throws Exception {
        when(connection.requireStream(5)).thenThrow(connectionError(PROTOCOL_ERROR, ""));
        decode().onWindowUpdateRead(ctx, 5, 10);
    }

    @Test
    public void windowUpdateReadShouldSucceed() throws Exception {
        decode().onWindowUpdateRead(ctx, STREAM_ID, 10);
        verify(remoteFlow).incrementWindowSize(eq(ctx), eq(stream), eq(10));
        verify(listener).onWindowUpdateRead(eq(ctx), eq(STREAM_ID), eq(10));
    }

    @Test
    public void rstStreamReadAfterGoAwayShouldSucceed() throws Exception {
        when(connection.goAwaySent()).thenReturn(true);
        decode().onRstStreamRead(ctx, STREAM_ID, PROTOCOL_ERROR.code());
        verify(lifecycleManager).closeStream(eq(stream), eq(future));
        verify(listener).onRstStreamRead(eq(ctx), anyInt(), anyLong());
    }

    @Test(expected = Http2Exception.class)
    public void rstStreamReadForUnknownStreamShouldThrow() throws Exception {
        when(connection.requireStream(5)).thenThrow(connectionError(PROTOCOL_ERROR, ""));
        decode().onRstStreamRead(ctx, 5, PROTOCOL_ERROR.code());
    }

    @Test
    public void rstStreamReadShouldCloseStream() throws Exception {
        decode().onRstStreamRead(ctx, STREAM_ID, PROTOCOL_ERROR.code());
        verify(lifecycleManager).closeStream(eq(stream), eq(future));
        verify(listener).onRstStreamRead(eq(ctx), eq(STREAM_ID), eq(PROTOCOL_ERROR.code()));
    }

    @Test(expected = Http2Exception.class)
    public void rstStreamOnIdleStreamShouldThrow() throws Exception {
        when(stream.state()).thenReturn(IDLE);
        decode().onRstStreamRead(ctx, STREAM_ID, PROTOCOL_ERROR.code());
        verify(lifecycleManager).closeStream(eq(stream), eq(future));
        verify(listener, never()).onRstStreamRead(any(ChannelHandlerContext.class), anyInt(), anyLong());
    }

    @Test
    public void pingReadWithAckShouldNotifylistener() throws Exception {
        decode().onPingAckRead(ctx, emptyPingBuf());
        verify(listener).onPingAckRead(eq(ctx), eq(emptyPingBuf()));
    }

    @Test
    public void pingReadShouldReplyWithAck() throws Exception {
        decode().onPingRead(ctx, emptyPingBuf());
        verify(encoder).writePing(eq(ctx), eq(true), eq(emptyPingBuf()), eq(promise));
        verify(listener, never()).onPingAckRead(eq(ctx), any(ByteBuf.class));
    }

    @Test
    public void settingsReadWithAckShouldNotifylistener() throws Exception {
        decode().onSettingsAckRead(ctx);
        // Take into account the time this was called during setup().
        verify(listener, times(2)).onSettingsAckRead(eq(ctx));
    }

    @Test
    public void settingsReadShouldSetValues() throws Exception {
        when(connection.isServer()).thenReturn(true);
        Http2Settings settings = new Http2Settings();
        settings.pushEnabled(true);
        settings.initialWindowSize(123);
        settings.maxConcurrentStreams(456);
        settings.headerTableSize(789);
        decode().onSettingsRead(ctx, settings);
        verify(encoder).remoteSettings(settings);
        verify(listener).onSettingsRead(eq(ctx), eq(settings));
    }

    @Test
    public void goAwayShouldReadShouldUpdateConnectionState() throws Exception {
        decode().onGoAwayRead(ctx, 1, 2L, EMPTY_BUFFER);
        verify(connection).goAwayReceived(eq(1), eq(2L), eq(EMPTY_BUFFER));
        verify(listener).onGoAwayRead(eq(ctx), eq(1), eq(2L), eq(EMPTY_BUFFER));
    }

    private static ByteBuf dummyData() {
        // The buffer is purposely 8 bytes so it will even work for a ping frame.
        return wrappedBuffer("abcdefgh".getBytes(UTF_8));
    }

    /**
     * Calls the decode method on the handler and gets back the captured internal listener
     */
    private Http2FrameListener decode() throws Exception {
        ArgumentCaptor<Http2FrameListener> internallistener = ArgumentCaptor.forClass(Http2FrameListener.class);
        doNothing().when(reader).readFrame(eq(ctx), any(ByteBuf.class), internallistener.capture());
        decoder.decodeFrame(ctx, EMPTY_BUFFER, Collections.emptyList());
        return internallistener.getValue();
    }

    private void mockFlowControl(final int processedBytes) throws Http2Exception {
        doAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return processedBytes;
            }
        }).when(listener).onDataRead(any(ChannelHandlerContext.class), anyInt(),
                any(ByteBuf.class), anyInt(), anyBoolean());
    }
}
