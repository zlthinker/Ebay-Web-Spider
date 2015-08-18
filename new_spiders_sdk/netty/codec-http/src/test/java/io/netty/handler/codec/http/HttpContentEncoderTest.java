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

package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class HttpContentEncoderTest {

    private static final class TestEncoder extends HttpContentEncoder {
        @Override
        protected Result beginEncode(HttpResponse headers, CharSequence acceptEncoding) {
            return new Result("test", new EmbeddedChannel(new MessageToByteEncoder<ByteBuf>() {
                @Override
                protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
                    out.writeBytes(String.valueOf(in.readableBytes()).getBytes(CharsetUtil.US_ASCII));
                    in.skipBytes(in.readableBytes());
                }
            }));
        }
    }

    @Test
    public void testSplitContent() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        ch.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[3])));
        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[2])));
        ch.writeOutbound(new DefaultLastHttpContent(Unpooled.wrappedBuffer(new byte[1])));

        assertEncodedResponse(ch);

        HttpContent chunk;
        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("3"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("2"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("1"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().isReadable(), is(false));
        assertThat(chunk, is(instanceOf(LastHttpContent.class)));
        chunk.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    @Test
    public void testChunkedContent() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        ch.writeOutbound(res);

        assertEncodedResponse(ch);

        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[3])));
        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[2])));
        ch.writeOutbound(new DefaultLastHttpContent(Unpooled.wrappedBuffer(new byte[1])));

        HttpContent chunk;
        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("3"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("2"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("1"));
        assertThat(chunk, is(instanceOf(HttpContent.class)));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().isReadable(), is(false));
        assertThat(chunk, is(instanceOf(LastHttpContent.class)));
        chunk.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    @Test
    public void testChunkedContentWithTrailingHeader() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        ch.writeOutbound(res);

        assertEncodedResponse(ch);

        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[3])));
        ch.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer(new byte[2])));
        LastHttpContent content = new DefaultLastHttpContent(Unpooled.wrappedBuffer(new byte[1]));
        content.trailingHeaders().set("X-Test", "Netty");
        ch.writeOutbound(content);

        HttpContent chunk;
        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("3"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("2"));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("1"));
        assertThat(chunk, is(instanceOf(HttpContent.class)));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().isReadable(), is(false));
        assertThat(chunk, is(instanceOf(LastHttpContent.class)));
        assertEquals("Netty", ((LastHttpContent) chunk).trailingHeaders().get("X-Test"));
        chunk.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    @Test
    public void testFullContent() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(new byte[42]));
        res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 42);
        ch.writeOutbound(res);

        assertEncodedResponse(ch);
        HttpContent c = ch.readOutbound();
        assertThat(c.content().readableBytes(), is(2));
        assertThat(c.content().toString(CharsetUtil.US_ASCII), is("42"));
        c.release();

        LastHttpContent last = ch.readOutbound();
        assertThat(last.content().readableBytes(), is(0));
        last.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    /**
     * If the length of the content is unknown, {@link HttpContentEncoder} should not skip encoding the content
     * even if the actual length is turned out to be 0.
     */
    @Test
    public void testEmptySplitContent() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        ch.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        assertEncodedResponse(ch);

        ch.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
        HttpContent chunk = ch.readOutbound();
        assertThat(chunk.content().toString(CharsetUtil.US_ASCII), is("0"));
        assertThat(chunk, is(instanceOf(HttpContent.class)));
        chunk.release();

        chunk = ch.readOutbound();
        assertThat(chunk.content().isReadable(), is(false));
        assertThat(chunk, is(instanceOf(LastHttpContent.class)));
        chunk.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    /**
     * If the length of the content is 0 for sure, {@link HttpContentEncoder} should skip encoding.
     */
    @Test
    public void testEmptyFullContent() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
        ch.writeOutbound(res);

        Object o = ch.readOutbound();
        assertThat(o, is(instanceOf(FullHttpResponse.class)));

        res = (FullHttpResponse) o;
        assertThat(res.headers().get(HttpHeaderNames.TRANSFER_ENCODING), is(nullValue()));

        // Content encoding shouldn't be modified.
        assertThat(res.headers().get(HttpHeaderNames.CONTENT_ENCODING), is(nullValue()));
        assertThat(res.content().readableBytes(), is(0));
        assertThat(res.content().toString(CharsetUtil.US_ASCII), is(""));
        res.release();

        assertThat(ch.readOutbound(), is(nullValue()));
    }

    @Test
    public void testEmptyFullContentWithTrailer() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(new TestEncoder());
        ch.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
        res.trailingHeaders().set("X-Test", "Netty");
        ch.writeOutbound(res);

        Object o = ch.readOutbound();
        assertThat(o, is(instanceOf(FullHttpResponse.class)));

        res = (FullHttpResponse) o;
        assertThat(res.headers().get(HttpHeaderNames.TRANSFER_ENCODING), is(nullValue()));

        // Content encoding shouldn't be modified.
        assertThat(res.headers().get(HttpHeaderNames.CONTENT_ENCODING), is(nullValue()));
        assertThat(res.content().readableBytes(), is(0));
        assertThat(res.content().toString(CharsetUtil.US_ASCII), is(""));
        assertEquals("Netty", res.trailingHeaders().get("X-Test"));
        assertThat(ch.readOutbound(), is(nullValue()));
    }

    private static void assertEncodedResponse(EmbeddedChannel ch) {
        Object o = ch.readOutbound();
        assertThat(o, is(instanceOf(HttpResponse.class)));

        HttpResponse res = (HttpResponse) o;
        assertThat(res, is(not(instanceOf(HttpContent.class))));
        assertThat(res.headers().getAndConvert(HttpHeaderNames.TRANSFER_ENCODING), is("chunked"));
        assertThat(res.headers().get(HttpHeaderNames.CONTENT_LENGTH), is(nullValue()));
        assertThat(res.headers().getAndConvert(HttpHeaderNames.CONTENT_ENCODING), is("test"));
    }
}
