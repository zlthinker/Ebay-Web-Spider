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
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.netty.util.ReferenceCountUtil.*;
import static org.junit.Assert.*;

public class ReplayingDecoderTest {

    @Test
    public void testLineProtocol() {
        EmbeddedChannel ch = new EmbeddedChannel(new LineDecoder());

        // Ordinary input
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[] { 'A' }));
        assertNull(ch.readInbound());
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[] { 'B' }));
        assertNull(ch.readInbound());
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[] { 'C' }));
        assertNull(ch.readInbound());
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[] { '\n' }));
        assertEquals(Unpooled.wrappedBuffer(new byte[] { 'A', 'B', 'C' }), ch.readInbound());

        // Truncated input
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[] { 'A' }));
        assertNull(ch.readInbound());

        ch.finish();
        assertNull(ch.readInbound());
    }

    private static final class LineDecoder extends ReplayingDecoder<Void> {

        LineDecoder() {
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            ByteBuf msg = in.readBytes(in.bytesBefore((byte) '\n'));
            out.add(msg);
            in.skipBytes(1);
        }
    }

    @Test
    public void testReplacement() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new BloatedLineDecoder());

        // "AB" should be forwarded to LineDecoder by BloatedLineDecoder.
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[]{'A', 'B'}));
        assertNull(ch.readInbound());

        // "C\n" should be appended to "AB" so that LineDecoder decodes it correctly.
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[]{'C', '\n'}));
        assertEquals(releaseLater(Unpooled.wrappedBuffer(new byte[] { 'A', 'B', 'C' })),
                releaseLater(ch.readInbound()));

        ch.finish();
        assertNull(ch.readInbound());
    }

    private static final class BloatedLineDecoder extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.pipeline().replace(this, "less-bloated", new LineDecoder());
            ctx.pipeline().fireChannelRead(msg);
        }
    }

    @Test
    public void testSingleDecode() throws Exception {
        LineDecoder decoder = new LineDecoder();
        decoder.setSingleDecode(true);
        EmbeddedChannel ch = new EmbeddedChannel(decoder);

        // "C\n" should be appended to "AB" so that LineDecoder decodes it correctly.
        ch.writeInbound(Unpooled.wrappedBuffer(new byte[]{'C', '\n' , 'B', '\n'}));
        assertEquals(releaseLater(Unpooled.wrappedBuffer(new byte[] {'C' })), releaseLater(ch.readInbound()));
        assertNull("Must be null as it must only decode one frame", ch.readInbound());

        ch.read();
        ch.finish();
        assertEquals(releaseLater(Unpooled.wrappedBuffer(new byte[] {'B' })), releaseLater(ch.readInbound()));
        assertNull(ch.readInbound());
    }

    @Test
    public void testRemoveItself() {
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayingDecoder() {
            private boolean removed;

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                assertFalse(removed);
                in.readByte();
                ctx.pipeline().remove(this);
                removed = true;
            }
        });

        ByteBuf buf = Unpooled.wrappedBuffer(new byte[] {'a', 'b', 'c'});
        channel.writeInbound(buf.copy());
        ByteBuf b = channel.readInbound();
        assertEquals(b, buf.skipBytes(1));
        b.release();
        buf.release();
    }

    @Test
    public void testRemoveItselfWithReplayError() {
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayingDecoder() {
            private boolean removed;

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                assertFalse(removed);
                ctx.pipeline().remove(this);

                in.readBytes(1000);

                removed = true;
            }
        });

        ByteBuf buf = Unpooled.wrappedBuffer(new byte[] {'a', 'b', 'c'});
        channel.writeInbound(buf.copy());
        ByteBuf b = channel.readInbound();

        assertEquals("Expect to have still all bytes in the buffer", b, buf);
        b.release();
        buf.release();
    }

    @Test
    public void testRemoveItselfWriteBuffer() {
        final ByteBuf buf = Unpooled.buffer().writeBytes(new byte[] {'a', 'b', 'c'});
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayingDecoder() {
            private boolean removed;

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                assertFalse(removed);
                in.readByte();
                ctx.pipeline().remove(this);

                // This should not let it keep call decode
                buf.writeByte('d');
                removed = true;
            }
        });

        channel.writeInbound(buf.copy());
        ByteBuf b = channel.readInbound();
        assertEquals(b, Unpooled.wrappedBuffer(new byte[] { 'b', 'c'}));
        b.release();
        buf.release();
    }

    @Test
    public void testFireChannelReadCompleteOnInactive() throws InterruptedException {
        final BlockingQueue<Integer> queue = new LinkedBlockingDeque<Integer>();
        final ByteBuf buf = releaseLater(Unpooled.buffer().writeBytes(new byte[]{'a', 'b'}));
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayingDecoder<Integer>() {

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                in.skipBytes(in.readableBytes());
                if (!ctx.channel().isActive()) {
                    out.add("data");
                }
            }
        }, new ChannelHandlerAdapter() {
            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                queue.add(3);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                queue.add(1);
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                if (!ctx.channel().isActive()) {
                    queue.add(2);
                }
            }
        });
        assertFalse(channel.writeInbound(buf));
        channel.finish();
        assertEquals(1, (int) queue.take());
        assertEquals(1, (int) queue.take());
        assertEquals(2, (int) queue.take());
        assertEquals(3, (int) queue.take());
        assertTrue(queue.isEmpty());
    }
}
