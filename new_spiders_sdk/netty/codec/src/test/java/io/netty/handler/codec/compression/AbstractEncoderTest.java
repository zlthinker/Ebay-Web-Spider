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
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public abstract class AbstractEncoderTest extends AbstractCompressionTest {

    protected EmbeddedChannel channel;

    /**
     * Decompresses data with some external library.
     */
    protected abstract ByteBuf decompress(ByteBuf compressed, int originalLength) throws Exception;

    @Before
    public abstract void initChannel();

    @DataPoints("smallData")
    public static ByteBuf[] smallData() {
        ByteBuf heap = Unpooled.wrappedBuffer(BYTES_SMALL);
        ByteBuf direct = Unpooled.directBuffer(BYTES_SMALL.length);
        direct.writeBytes(BYTES_SMALL);
        return new ByteBuf[] {heap, direct};
    }

    @DataPoints("largeData")
    public static ByteBuf[] largeData() {
        ByteBuf heap = Unpooled.wrappedBuffer(BYTES_LARGE);
        ByteBuf direct = Unpooled.directBuffer(BYTES_LARGE.length);
        direct.writeBytes(BYTES_LARGE);
        return new ByteBuf[] {heap, direct};
    }

    @Theory
    public void testCompressionOfSmallChunkOfData(@FromDataPoints("smallData") ByteBuf data) throws Exception {
        testCompression(data);
    }

    @Theory
    public void testCompressionOfLargeChunkOfData(@FromDataPoints("largeData") ByteBuf data) throws Exception {
        testCompression(data);
    }

    @Theory
    public void testCompressionOfBatchedFlowOfData(@FromDataPoints("largeData") ByteBuf data) throws Exception {
        testCompressionOfBatchedFlow(data);
    }

    protected void testCompression(final ByteBuf data) throws Exception {
        final int dataLength = data.readableBytes();
        assertTrue(channel.writeOutbound(data.retain()));
        assertTrue(channel.finish());

        ByteBuf decompressed = readDecompressed(dataLength);
        assertEquals(data.resetReaderIndex(), decompressed);

        decompressed.release();
        data.release();
    }

    protected void testCompressionOfBatchedFlow(final ByteBuf data) throws Exception {
        final int dataLength = data.readableBytes();
        int written = 0, length = rand.nextInt(100);
        while (written + length < dataLength) {
            ByteBuf in = data.slice(written, length);
            assertTrue(channel.writeOutbound(in.retain()));
            written += length;
            length = rand.nextInt(100);
        }
        ByteBuf in = data.slice(written, dataLength - written);
        assertTrue(channel.writeOutbound(in.retain()));
        assertTrue(channel.finish());

        ByteBuf decompressed = readDecompressed(dataLength);
        assertEquals(data, decompressed);

        decompressed.release();
        data.release();
    }

    protected ByteBuf readDecompressed(final int dataLength) throws Exception {
        CompositeByteBuf compressed = Unpooled.compositeBuffer();
        ByteBuf msg;
        while ((msg = channel.readOutbound()) != null) {
            compressed.addComponent(msg);
            compressed.writerIndex(compressed.writerIndex() + msg.readableBytes());
        }
        ByteBuf decompressed =  decompress(compressed, dataLength);
        compressed.release();
        return decompressed;
    }
}
