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
package io.netty.buffer;


import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ScatteringByteChannel;

import static io.netty.buffer.Unpooled.*;
import static io.netty.util.ReferenceCountUtil.*;
import static org.junit.Assert.*;

public class FixedCompositeByteBufTest {

    private static ByteBuf newBuffer(ByteBuf... buffers) {
        return releaseLater(new FixedCompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, buffers));
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetBoolean() {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setBoolean(0, true);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetByte() {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setByte(0, 1);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetBytesWithByteBuf() {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setBytes(0, wrappedBuffer(new byte[4]));
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetBytesWithByteBuffer() {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setBytes(0, ByteBuffer.wrap(new byte[4]));
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetBytesWithInputStream() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setBytes(0, new ByteArrayInputStream(new byte[4]), 4);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetBytesWithChannel() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setBytes(0, new ScatteringByteChannel() {
            @Override
            public long read(ByteBuffer[] dsts, int offset, int length) {
                return 0;
            }

            @Override
            public long read(ByteBuffer[] dsts) {
                return 0;
            }

            @Override
            public int read(ByteBuffer dst) {
                return 0;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
            }
        }, 4);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetChar() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setChar(0, 'b');
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetDouble() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setDouble(0, 1);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetFloat() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setFloat(0, 1);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetInt() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setInt(0, 1);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetLong() {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setLong(0, 1);
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testSetMedium() throws IOException {
        ByteBuf buf = newBuffer(wrappedBuffer(new byte[8]));
        buf.setMedium(0, 1);
    }

    @Test
    public void testGatheringWritesHeap() throws Exception {
        testGatheringWrites(buffer(), buffer());
    }

    @Test
    public void testGatheringWritesDirect() throws Exception {
        testGatheringWrites(directBuffer(), directBuffer());
    }

    @Test
    public void testGatheringWritesMixes() throws Exception {
        testGatheringWrites(buffer(), directBuffer());
    }

    @Test
    public void testGatheringWritesHeapPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.heapBuffer(),
                PooledByteBufAllocator.DEFAULT.heapBuffer());
    }

    @Test
    public void testGatheringWritesDirectPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.directBuffer(),
                PooledByteBufAllocator.DEFAULT.directBuffer());
    }

    @Test
    public void testGatheringWritesMixesPooled() throws Exception {
        testGatheringWrites(PooledByteBufAllocator.DEFAULT.heapBuffer(),
                PooledByteBufAllocator.DEFAULT.directBuffer());
    }

    private static void testGatheringWrites(ByteBuf buf1, ByteBuf buf2) throws Exception {
        CompositeByteBuf buf = compositeBuffer();
        buf.addComponent(buf1.writeBytes(new byte[]{1, 2}));
        buf.addComponent(buf2.writeBytes(new byte[]{1, 2}));
        buf.writerIndex(3);
        buf.readerIndex(1);

        AbstractByteBufTest.TestGatheringByteChannel channel = new AbstractByteBufTest.TestGatheringByteChannel();
        buf.readBytes(channel, 2);

        byte[] data = new byte[2];
        buf.getBytes(1, data);
        buf.release();

        assertArrayEquals(data, channel.writtenBytes());
    }

    @Test
    public void testGatheringWritesPartialHeap() throws Exception {
        testGatheringWritesPartial(buffer(), buffer());
    }

    @Test
    public void testGatheringWritesPartialDirect() throws Exception {
        testGatheringWritesPartial(directBuffer(), directBuffer());
    }

    @Test
    public void testGatheringWritesPartialMixes() throws Exception {
        testGatheringWritesPartial(buffer(), directBuffer());
    }

    @Test
    public void testGatheringWritesPartialHeapPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer(),
                PooledByteBufAllocator.DEFAULT.heapBuffer());
    }

    @Test
    public void testGatheringWritesPartialDirectPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.directBuffer(),
                PooledByteBufAllocator.DEFAULT.directBuffer());
    }

    @Test
    public void testGatheringWritesPartialMixesPooled() throws Exception {
        testGatheringWritesPartial(PooledByteBufAllocator.DEFAULT.heapBuffer(),
                PooledByteBufAllocator.DEFAULT.directBuffer());
    }

    private static void testGatheringWritesPartial(ByteBuf buf1, ByteBuf buf2) throws Exception {
        buf1.writeBytes(new byte[]{1, 2, 3, 4});
        buf2.writeBytes(new byte[]{1, 2, 3, 4});
        ByteBuf buf = newBuffer(buf1, buf2);
        AbstractByteBufTest.TestGatheringByteChannel channel = new AbstractByteBufTest.TestGatheringByteChannel(1);

        while (buf.isReadable()) {
            buf.readBytes(channel, buf.readableBytes());
        }

        byte[] data = new byte[8];
        buf.getBytes(0, data);
        assertArrayEquals(data, channel.writtenBytes());
        buf.release();
    }

    @Test
    public void testGatheringWritesSingleHeap() throws Exception {
        testGatheringWritesSingleBuf(buffer());
    }

    @Test
    public void testGatheringWritesSingleDirect() throws Exception {
        testGatheringWritesSingleBuf(directBuffer());
    }

    private static void testGatheringWritesSingleBuf(ByteBuf buf1) throws Exception {
        ByteBuf buf = newBuffer(buf1.writeBytes(new byte[]{1, 2, 3, 4}));
        buf.readerIndex(1);

        AbstractByteBufTest.TestGatheringByteChannel channel = new AbstractByteBufTest.TestGatheringByteChannel();
        buf.readBytes(channel, 2);

        byte[] data = new byte[2];
        buf.getBytes(1, data);
        assertArrayEquals(data, channel.writtenBytes());

        buf.release();
    }
}
