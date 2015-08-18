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
package io.netty.buffer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.netty.buffer.Unpooled.*;
import static io.netty.util.ReferenceCountUtil.*;
import static io.netty.util.internal.EmptyArrays.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * An abstract test class for composite channel buffers
 */
public abstract class AbstractCompositeByteBufTest extends AbstractByteBufTest {

    private final ByteOrder order;

    protected AbstractCompositeByteBufTest(ByteOrder order) {
        if (order == null) {
            throw new NullPointerException("order");
        }
        this.order = order;
    }

    private List<ByteBuf> buffers;
    private ByteBuf buffer;

    @Override
    protected ByteBuf newBuffer(int length) {
        buffers = new ArrayList<ByteBuf>();
        for (int i = 0; i < length + 45; i += 45) {
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[1]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[2]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[3]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[4]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[5]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[6]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[7]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[8]));
            buffers.add(EMPTY_BUFFER);
            buffers.add(wrappedBuffer(new byte[9]));
            buffers.add(EMPTY_BUFFER);
        }

        buffer = wrappedBuffer(Integer.MAX_VALUE, buffers.toArray(new ByteBuf[buffers.size()])).order(order);

        // Truncate to the requested capacity.
        buffer.capacity(length);

        assertEquals(length, buffer.capacity());
        assertEquals(length, buffer.readableBytes());
        assertFalse(buffer.isWritable());
        buffer.writerIndex(0);
        return buffer;
    }

    @Override
    protected ByteBuf[] components() {
        return buffers.toArray(new ByteBuf[buffers.size()]);
    }

    // Composite buffer does not waste bandwidth on discardReadBytes, but
    // the test will fail in strict mode.
    @Override
    protected boolean discardReadBytesDoesNotMoveWritableBytes() {
        return false;
    }

    /**
     * Tests the "getBufferFor" method
     */
    @Test
    public void testComponentAtOffset() {
        CompositeByteBuf buf = releaseLater((CompositeByteBuf) wrappedBuffer(new byte[]{1, 2, 3, 4, 5},
                new byte[]{4, 5, 6, 7, 8, 9, 26}));

        //Ensure that a random place will be fine
        assertEquals(5, buf.componentAtOffset(2).capacity());

        //Loop through each byte

        byte index = 0;

        while (index < buf.capacity()) {
            ByteBuf _buf = buf.componentAtOffset(index++);
            assertNotNull(_buf);
            assertTrue(_buf.capacity() > 0);
            assertNotNull(_buf.getByte(0));
            assertNotNull(_buf.getByte(_buf.readableBytes() - 1));
        }
    }

    @Test
    public void testDiscardReadBytes3() {
        ByteBuf a, b;
        a = wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).order(order);
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 0, 5).order(order),
                wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 5, 5).order(order)));
        a.skipBytes(6);
        a.markReaderIndex();
        b.skipBytes(6);
        b.markReaderIndex();
        assertEquals(a.readerIndex(), b.readerIndex());
        a.readerIndex(a.readerIndex() - 1);
        b.readerIndex(b.readerIndex() - 1);
        assertEquals(a.readerIndex(), b.readerIndex());
        a.writerIndex(a.writerIndex() - 1);
        a.markWriterIndex();
        b.writerIndex(b.writerIndex() - 1);
        b.markWriterIndex();
        assertEquals(a.writerIndex(), b.writerIndex());
        a.writerIndex(a.writerIndex() + 1);
        b.writerIndex(b.writerIndex() + 1);
        assertEquals(a.writerIndex(), b.writerIndex());
        assertTrue(ByteBufUtil.equals(a, b));
        // now discard
        a.discardReadBytes();
        b.discardReadBytes();
        assertEquals(a.readerIndex(), b.readerIndex());
        assertEquals(a.writerIndex(), b.writerIndex());
        assertTrue(ByteBufUtil.equals(a, b));
        a.resetReaderIndex();
        b.resetReaderIndex();
        assertEquals(a.readerIndex(), b.readerIndex());
        a.resetWriterIndex();
        b.resetWriterIndex();
        assertEquals(a.writerIndex(), b.writerIndex());
        assertTrue(ByteBufUtil.equals(a, b));
    }

    @Test
    public void testAutoConsolidation() {
        CompositeByteBuf buf = releaseLater(compositeBuffer(2));

        buf.addComponent(wrappedBuffer(new byte[] { 1 }));
        assertEquals(1, buf.numComponents());

        buf.addComponent(wrappedBuffer(new byte[] { 2, 3 }));
        assertEquals(2, buf.numComponents());

        buf.addComponent(wrappedBuffer(new byte[] { 4, 5, 6 }));

        assertEquals(1, buf.numComponents());
        assertTrue(buf.hasArray());
        assertNotNull(buf.array());
        assertEquals(0, buf.arrayOffset());
    }

    @Test
    public void testCompositeToSingleBuffer() {
        CompositeByteBuf buf = releaseLater(compositeBuffer(3));

        buf.addComponent(wrappedBuffer(new byte[] {1, 2, 3}));
        assertEquals(1, buf.numComponents());

        buf.addComponent(wrappedBuffer(new byte[] {4}));
        assertEquals(2, buf.numComponents());

        buf.addComponent(wrappedBuffer(new byte[] {5, 6}));
        assertEquals(3, buf.numComponents());

        // NOTE: hard-coding 6 here, since it seems like addComponent doesn't bump the writer index.
        // I'm unsure as to whether or not this is correct behavior
        ByteBuffer nioBuffer = buf.nioBuffer(0, 6);
        byte[] bytes = nioBuffer.array();
        assertEquals(6, bytes.length);
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, bytes);
    }

    @Test
    public void testFullConsolidation() {
        CompositeByteBuf buf = releaseLater(compositeBuffer(Integer.MAX_VALUE));
        buf.addComponent(wrappedBuffer(new byte[] { 1 }));
        buf.addComponent(wrappedBuffer(new byte[] { 2, 3 }));
        buf.addComponent(wrappedBuffer(new byte[] { 4, 5, 6 }));
        buf.consolidate();

        assertEquals(1, buf.numComponents());
        assertTrue(buf.hasArray());
        assertNotNull(buf.array());
        assertEquals(0, buf.arrayOffset());
    }

    @Test
    public void testRangedConsolidation() {
        CompositeByteBuf buf = releaseLater(compositeBuffer(Integer.MAX_VALUE));
        buf.addComponent(wrappedBuffer(new byte[] { 1 }));
        buf.addComponent(wrappedBuffer(new byte[] { 2, 3 }));
        buf.addComponent(wrappedBuffer(new byte[] { 4, 5, 6 }));
        buf.addComponent(wrappedBuffer(new byte[] { 7, 8, 9, 10 }));
        buf.consolidate(1, 2);

        assertEquals(3, buf.numComponents());
        assertEquals(wrappedBuffer(new byte[] { 1 }), buf.component(0));
        assertEquals(wrappedBuffer(new byte[] { 2, 3, 4, 5, 6 }), buf.component(1));
        assertEquals(wrappedBuffer(new byte[] { 7, 8, 9, 10 }), buf.component(2));
    }

    @Test
    public void testCompositeWrappedBuffer() {
        ByteBuf header = releaseLater(buffer(12)).order(order);
        ByteBuf payload = releaseLater(buffer(512)).order(order);

        header.writeBytes(new byte[12]);
        payload.writeBytes(new byte[512]);

        ByteBuf buffer = releaseLater(wrappedBuffer(header, payload));

        assertEquals(12, header.readableBytes());
        assertEquals(512, payload.readableBytes());

        assertEquals(12 + 512, buffer.readableBytes());
        assertEquals(2, buffer.nioBufferCount());
    }

    @Test
    public void testSeveralBuffersEquals() {
        ByteBuf a, b;
        // XXX Same tests with several buffers in wrappedCheckedBuffer
        // Different length.
        a = releaseLater(wrappedBuffer(new byte[] { 1 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 1 }).order(order),
                wrappedBuffer(new byte[] { 2 }).order(order)));
        assertFalse(ByteBufUtil.equals(a, b));

        // Same content, same firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[]{1}).order(order),
                wrappedBuffer(new byte[]{2}).order(order),
                wrappedBuffer(new byte[]{3}).order(order)));
        assertTrue(ByteBufUtil.equals(a, b));

        // Same content, different firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4 }, 1, 2).order(order),
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4 }, 3, 1).order(order)));
        assertTrue(ByteBufUtil.equals(a, b));

        // Different content, same firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 1, 2 }).order(order),
                wrappedBuffer(new byte[] { 4 }).order(order)));
        assertFalse(ByteBufUtil.equals(a, b));

        // Different content, different firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 0, 1, 2, 4, 5 }, 1, 2).order(order),
                wrappedBuffer(new byte[] { 0, 1, 2, 4, 5 }, 3, 1).order(order)));
        assertFalse(ByteBufUtil.equals(a, b));

        // Same content, same firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 1, 2, 3 }).order(order),
                wrappedBuffer(new byte[] { 4, 5, 6 }).order(order),
                wrappedBuffer(new byte[] { 7, 8, 9, 10 }).order(order)));
        assertTrue(ByteBufUtil.equals(a, b));

        // Same content, different firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, 1, 5).order(order),
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, 6, 5).order(order)));
        assertTrue(ByteBufUtil.equals(a, b));

        // Different content, same firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 1, 2, 3, 4, 6 }).order(order),
                wrappedBuffer(new byte[] { 7, 8, 5, 9, 10 }).order(order)));
        assertFalse(ByteBufUtil.equals(a, b));

        // Different content, different firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }).order(order));
        b = releaseLater(wrappedBuffer(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 6, 7, 8, 5, 9, 10, 11 }, 1, 5).order(order),
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 6, 7, 8, 5, 9, 10, 11 }, 6, 5).order(order)));
        assertFalse(ByteBufUtil.equals(a, b));
    }

    @Test
    public void testWrappedBuffer() {

        assertEquals(16, wrappedBuffer(wrappedBuffer(ByteBuffer.allocateDirect(16))).capacity());

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                wrappedBuffer(wrappedBuffer(new byte[][] { new byte[] { 1, 2, 3 } }).order(order)));

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                releaseLater(wrappedBuffer(wrappedBuffer(
                        new byte[] { 1 },
                        new byte[] { 2 },
                        new byte[] { 3 }).order(order))));

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                wrappedBuffer(new ByteBuf[] {
                        wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)
                }));

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                releaseLater(wrappedBuffer(
                        wrappedBuffer(new byte[] { 1 }).order(order),
                        wrappedBuffer(new byte[] { 2 }).order(order),
                        wrappedBuffer(new byte[] { 3 }).order(order))));

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                wrappedBuffer(wrappedBuffer(new ByteBuffer[] {
                        ByteBuffer.wrap(new byte[] { 1, 2, 3 })
                })));

        assertEquals(
                wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3 }).order(order)),
                releaseLater(wrappedBuffer(wrappedBuffer(
                        ByteBuffer.wrap(new byte[] { 1 }),
                        ByteBuffer.wrap(new byte[] { 2 }),
                        ByteBuffer.wrap(new byte[] { 3 })))));
    }

    @Test
    public void testWrittenBuffersEquals() {
        //XXX Same tests than testEquals with written AggregateChannelBuffers
        ByteBuf a, b;
        // Different length.
        a = releaseLater(wrappedBuffer(new byte[] { 1  })).order(order);
        b = releaseLater(wrappedBuffer(wrappedBuffer(new byte[] { 1 }, new byte[1])).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 1);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 2 })).order(order));
        assertFalse(ByteBufUtil.equals(a, b));

        // Same content, same firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(wrappedBuffer(new byte[] { 1 }, new byte[2]))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 2);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 2 })).order(order));
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 3 })).order(order));
        assertTrue(ByteBufUtil.equals(a, b));

        // Same content, different firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(wrappedBuffer(new byte[] { 0, 1, 2, 3, 4 }, 1, 3))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 1);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 0, 1, 2, 3, 4 }, 3, 1)).order(order));
        assertTrue(ByteBufUtil.equals(a, b));

        // Different content, same firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(wrappedBuffer(new byte[] { 1, 2 }, new byte[1])).order(order)));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 1);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 4 })).order(order));
        assertFalse(ByteBufUtil.equals(a, b));

        // Different content, different firstIndex, short length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(wrappedBuffer(new byte[] { 0, 1, 2, 4, 5 }, 1, 3))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 1);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 0, 1, 2, 4, 5 }, 3, 1)).order(order));
        assertFalse(ByteBufUtil.equals(a, b));

        // Same content, same firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(wrappedBuffer(new byte[] { 1, 2, 3 }, new byte[7]))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 7);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 4, 5, 6 })).order(order));
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 7, 8, 9, 10 })).order(order));
        assertTrue(ByteBufUtil.equals(a, b));

        // Same content, different firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, 1, 10))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 5);
        b.writeBytes(releaseLater(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, 6, 5)).order(order));
        assertTrue(ByteBufUtil.equals(a, b));

        // Different content, same firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })).order(order);
        b = releaseLater(wrappedBuffer(wrappedBuffer(new byte[] { 1, 2, 3, 4, 6 }, new byte[5])).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 5);
        b.writeBytes(releaseLater(wrappedBuffer(new byte[] { 7, 8, 5, 9, 10 })).order(order));
        assertFalse(ByteBufUtil.equals(a, b));

        // Different content, different firstIndex, long length.
        a = releaseLater(wrappedBuffer(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })).order(order);
        b = releaseLater(wrappedBuffer(releaseLater(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 6, 7, 8, 5, 9, 10, 11 }, 1, 10))).order(order));
        // to enable writeBytes
        b.writerIndex(b.writerIndex() - 5);
        b.writeBytes(releaseLater(
                wrappedBuffer(new byte[] { 0, 1, 2, 3, 4, 6, 7, 8, 5, 9, 10, 11 }, 6, 5)).order(order));
        assertFalse(ByteBufUtil.equals(a, b));
    }

    @Test
    public void testEmptyBuffer() {
        ByteBuf b = releaseLater(wrappedBuffer(new byte[]{1, 2}, new byte[]{3, 4}));
        b.readBytes(new byte[4]);
        b.readBytes(EMPTY_BYTES);
    }

    // Test for https://github.com/netty/netty/issues/1060
    @Test
    public void testReadWithEmptyCompositeBuffer() {
        ByteBuf buf = releaseLater(compositeBuffer());
        int n = 65;
        for (int i = 0; i < n; i ++) {
            buf.writeByte(1);
            assertEquals(1, buf.readByte());
        }
    }

    @Test
    public void testComponentMustBeSlice() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(buffer(4).setIndex(1, 3));
        assertThat(buf.component(0), is(instanceOf(SlicedByteBuf.class)));
        assertThat(buf.component(0).capacity(), is(2));
        assertThat(buf.component(0).maxCapacity(), is(2));
    }

    @Test
    public void testReferenceCounts1() {
        ByteBuf c1 = buffer().writeByte(1);
        ByteBuf c2 = buffer().writeByte(2).retain();
        ByteBuf c3 = buffer().writeByte(3).retain(2);

        CompositeByteBuf buf = releaseLater(compositeBuffer());
        assertThat(buf.refCnt(), is(1));
        buf.addComponents(c1, c2, c3);

        assertThat(buf.refCnt(), is(1));

        // Ensure that c[123]'s refCount did not change.
        assertThat(c1.refCnt(), is(1));
        assertThat(c2.refCnt(), is(2));
        assertThat(c3.refCnt(), is(3));

        assertThat(buf.component(0).refCnt(), is(1));
        assertThat(buf.component(1).refCnt(), is(2));
        assertThat(buf.component(2).refCnt(), is(3));

        c3.release(2);
        c2.release();
    }

    @Test
    public void testReferenceCounts2() {
        ByteBuf c1 = buffer().writeByte(1);
        ByteBuf c2 = buffer().writeByte(2).retain();
        ByteBuf c3 = buffer().writeByte(3).retain(2);

        CompositeByteBuf bufA = compositeBuffer();
        bufA.addComponents(c1, c2, c3).writerIndex(3);

        CompositeByteBuf bufB = compositeBuffer();
        bufB.addComponents(bufA);

        // Ensure that bufA.refCnt() did not change.
        assertThat(bufA.refCnt(), is(1));

        // Ensure that c[123]'s refCnt did not change.
        assertThat(c1.refCnt(), is(1));
        assertThat(c2.refCnt(), is(2));
        assertThat(c3.refCnt(), is(3));

        // This should decrease bufA.refCnt().
        bufB.release();
        assertThat(bufB.refCnt(), is(0));

        // Ensure bufA.refCnt() changed.
        assertThat(bufA.refCnt(), is(0));

        // Ensure that c[123]'s refCnt also changed due to the deallocation of bufA.
        assertThat(c1.refCnt(), is(0));
        assertThat(c2.refCnt(), is(1));
        assertThat(c3.refCnt(), is(2));

        c3.release(2);
        c2.release();
    }

    @Test
    public void testReferenceCounts3() {
        ByteBuf c1 = buffer().writeByte(1);
        ByteBuf c2 = buffer().writeByte(2).retain();
        ByteBuf c3 = buffer().writeByte(3).retain(2);

        CompositeByteBuf buf = releaseLater(compositeBuffer());
        assertThat(buf.refCnt(), is(1));

        List<ByteBuf> components = new ArrayList<ByteBuf>();
        Collections.addAll(components, c1, c2, c3);
        buf.addComponents(components);

        // Ensure that c[123]'s refCount did not change.
        assertThat(c1.refCnt(), is(1));
        assertThat(c2.refCnt(), is(2));
        assertThat(c3.refCnt(), is(3));

        assertThat(buf.component(0).refCnt(), is(1));
        assertThat(buf.component(1).refCnt(), is(2));
        assertThat(buf.component(2).refCnt(), is(3));

        c3.release(2);
        c2.release();
    }

    @Test
    public void testNestedLayout() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(
                compositeBuffer()
                        .addComponent(wrappedBuffer(new byte[]{1, 2}))
                        .addComponent(wrappedBuffer(new byte[]{3, 4})).slice(1, 2));

        ByteBuffer[] nioBuffers = buf.nioBuffers(0, 2);
        assertThat(nioBuffers.length, is(2));
        assertThat(nioBuffers[0].remaining(), is(1));
        assertThat(nioBuffers[0].get(), is((byte) 2));
        assertThat(nioBuffers[1].remaining(), is(1));
        assertThat(nioBuffers[1].get(), is((byte) 3));
    }

    @Test
    public void testRemoveLastComponent() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(wrappedBuffer(new byte[]{1, 2}));
        assertEquals(1, buf.numComponents());
        buf.removeComponent(0);
        assertEquals(0, buf.numComponents());
    }

    @Test
    public void testCopyEmpty() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        assertEquals(0, buf.numComponents());
        assertEquals(0, releaseLater(buf.copy()).readableBytes());
    }

    @Test
    public void testDuplicateEmpty() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        assertEquals(0, buf.numComponents());
        assertEquals(0, releaseLater(buf.duplicate()).readableBytes());
    }

    @Test
    public void testRemoveLastComponentWithOthersLeft() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(wrappedBuffer(new byte[]{1, 2}));
        buf.addComponent(wrappedBuffer(new byte[]{1, 2}));
        assertEquals(2, buf.numComponents());
        buf.removeComponent(1);
        assertEquals(1, buf.numComponents());
    }

    @Test
    public void testGatheringWritesHeap() throws Exception {
        testGatheringWrites(buffer().order(order), buffer().order(order));
    }

    @Test
    public void testGatheringWritesDirect() throws Exception {
        testGatheringWrites(directBuffer().order(order), directBuffer().order(order));
    }

    @Test
    public void testGatheringWritesMixes() throws Exception {
        testGatheringWrites(buffer().order(order), directBuffer().order(order));
    }

    @Test
    public void testGatheringWritesHeapPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.heapBuffer().order(order));
    }

    @Test
    public void testGatheringWritesDirectPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.directBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order));
    }

    @Test
    public void testGatheringWritesMixesPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order));
    }

    private static void testGatheringWrites(ByteBuf buf1, ByteBuf buf2) throws Exception {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(buf1.writeBytes(new byte[]{1, 2}));
        buf.addComponent(buf2.writeBytes(new byte[]{1, 2}));
        buf.writerIndex(3);
        buf.readerIndex(1);

        TestGatheringByteChannel channel = new TestGatheringByteChannel();

        buf.readBytes(channel, 2);

        byte[] data = new byte[2];
        buf.getBytes(1, data);
        assertArrayEquals(data, channel.writtenBytes());
    }

    @Test
    public void testGatheringWritesPartialHeap() throws Exception {
        testGatheringWritesPartial(buffer().order(order), buffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialDirect() throws Exception {
        testGatheringWritesPartial(directBuffer().order(order), directBuffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialMixes() throws Exception {
        testGatheringWritesPartial(buffer().order(order), directBuffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialHeapSlice() throws Exception {
        testGatheringWritesPartial(buffer().order(order), buffer().order(order), true);
    }

    @Test
    public void testGatheringWritesPartialDirectSlice() throws Exception {
        testGatheringWritesPartial(directBuffer().order(order), directBuffer().order(order), true);
    }

    @Test
    public void testGatheringWritesPartialMixesSlice() throws Exception {
        testGatheringWritesPartial(buffer().order(order), directBuffer().order(order), true);
    }

    @Test
    public void testGatheringWritesPartialHeapPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.heapBuffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialDirectPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.directBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialMixesPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order), false);
    }

    @Test
    public void testGatheringWritesPartialHeapPooledSliced() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.heapBuffer().order(order), true);
    }

    @Test
    public void testGatheringWritesPartialDirectPooledSliced() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.directBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order), true);
    }

    @Test
    public void testGatheringWritesPartialMixesPooledSliced() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer().order(order),
                PooledByteBufAllocator.DEFAULT.directBuffer().order(order), true);
    }

    private static void testGatheringWritesPartial(ByteBuf buf1, ByteBuf buf2, boolean slice) throws Exception {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf1.writeBytes(new byte[]{1, 2, 3, 4});
        buf2.writeBytes(new byte[]{1, 2, 3, 4});
        if (slice) {
            buf1 = buf1.readerIndex(1).slice();
            buf2 = buf2.writerIndex(3).slice();
            buf.addComponent(buf1);
            buf.addComponent(buf2);
            buf.writerIndex(6);
        } else {
            buf.addComponent(buf1);
            buf.addComponent(buf2);
            buf.writerIndex(7);
            buf.readerIndex(1);
        }

        TestGatheringByteChannel channel = new TestGatheringByteChannel(1);

        while (buf.isReadable()) {
            buf.readBytes(channel, buf.readableBytes());
        }

        byte[] data = new byte[6];

        if (slice) {
            buf.getBytes(0, data);
        } else {
            buf.getBytes(1, data);
        }
        assertArrayEquals(data, channel.writtenBytes());
    }

    @Test
    public void testGatheringWritesSingleHeap() throws Exception {
        testGatheringWritesSingleBuf(buffer().order(order));
    }

    @Test
    public void testGatheringWritesSingleDirect() throws Exception {
        testGatheringWritesSingleBuf(directBuffer().order(order));
    }

    private static void testGatheringWritesSingleBuf(ByteBuf buf1) throws Exception {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        buf.addComponent(buf1.writeBytes(new byte[]{1, 2, 3, 4}));
        buf.writerIndex(3);
        buf.readerIndex(1);

        TestGatheringByteChannel channel = new TestGatheringByteChannel();
        buf.readBytes(channel, 2);

        byte[] data = new byte[2];
        buf.getBytes(1, data);
        assertArrayEquals(data, channel.writtenBytes());
    }

    @Override
    @Test
    public void testInternalNioBuffer() {
        // ignore
    }

    @Test
    public void testisDirectMultipleBufs() {
        CompositeByteBuf buf = releaseLater(compositeBuffer());
        assertFalse(buf.isDirect());

        buf.addComponent(directBuffer().writeByte(1));

        assertTrue(buf.isDirect());
        buf.addComponent(directBuffer().writeByte(1));
        assertTrue(buf.isDirect());

        buf.addComponent(buffer().writeByte(1));
        assertFalse(buf.isDirect());
    }

    // See https://github.com/netty/netty/issues/1976
    @Test
    public void testDiscardSomeReadBytes() {
        CompositeByteBuf cbuf = releaseLater(compositeBuffer());
        int len = 8 * 4;
        for (int i = 0; i < len; i += 4) {
            ByteBuf buf = buffer().writeInt(i);
            cbuf.capacity(cbuf.writerIndex()).addComponent(buf).writerIndex(i + 4);
        }
        cbuf.writeByte(1);

        byte[] me = new byte[len];
        cbuf.readBytes(me);
        cbuf.readByte();

        cbuf.discardSomeReadBytes();
    }

    @Test
    public void testAddEmptyBufferRelease() {
        CompositeByteBuf cbuf = compositeBuffer();
        ByteBuf buf = buffer();
        assertEquals(1, buf.refCnt());
        cbuf.addComponent(buf);
        assertEquals(1, buf.refCnt());

        cbuf.release();
        assertEquals(0, buf.refCnt());
    }

    @Test
    public void testAddEmptyBuffersRelease() {
        CompositeByteBuf cbuf = compositeBuffer();
        ByteBuf buf = buffer();
        ByteBuf buf2 = buffer().writeInt(1);
        ByteBuf buf3 = buffer();

        assertEquals(1, buf.refCnt());
        assertEquals(1, buf2.refCnt());
        assertEquals(1, buf3.refCnt());

        cbuf.addComponents(buf, buf2, buf3);
        assertEquals(1, buf.refCnt());
        assertEquals(1, buf2.refCnt());
        assertEquals(1, buf3.refCnt());

        cbuf.release();
        assertEquals(0, buf.refCnt());
        assertEquals(0, buf2.refCnt());
        assertEquals(0, buf3.refCnt());
    }

    @Test
    public void testAddEmptyBufferInMiddle() {
        CompositeByteBuf cbuf = compositeBuffer();
        ByteBuf buf1 = buffer().writeByte((byte) 1);
        cbuf.addComponent(buf1).writerIndex(cbuf.writerIndex() + buf1.readableBytes());
        ByteBuf buf2 = EMPTY_BUFFER;
        cbuf.addComponent(buf2).writerIndex(cbuf.writerIndex() + buf2.readableBytes());
        ByteBuf buf3 = buffer().writeByte((byte) 2);
        cbuf.addComponent(buf3).writerIndex(cbuf.writerIndex() + buf3.readableBytes());

        assertEquals(2, cbuf.readableBytes());
        assertEquals((byte) 1, cbuf.readByte());
        assertEquals((byte) 2, cbuf.readByte());

        assertSame(EMPTY_BUFFER, cbuf.internalComponent(1));
        assertNotSame(EMPTY_BUFFER, cbuf.internalComponentAtOffset(1));
        cbuf.release();
    }
}
