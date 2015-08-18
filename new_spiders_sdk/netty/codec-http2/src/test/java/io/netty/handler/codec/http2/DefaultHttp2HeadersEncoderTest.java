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

import static io.netty.handler.codec.http2.Http2TestUtil.as;
import static org.junit.Assert.assertTrue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultHttp2HeadersEncoder}.
 */
public class DefaultHttp2HeadersEncoderTest {

    private DefaultHttp2HeadersEncoder encoder;

    @Before
    public void setup() {
        encoder = new DefaultHttp2HeadersEncoder();
    }

    @Test
    public void encodeShouldSucceed() throws Http2Exception {
        Http2Headers headers = headers();
        ByteBuf buf = Unpooled.buffer();
        try {
            encoder.encodeHeaders(headers, buf);
            assertTrue(buf.writerIndex() > 0);
        } finally {
            buf.release();
        }
    }

    @Test(expected = Http2Exception.class)
    public void headersExceedMaxSetSizeShouldFail() throws Http2Exception {
        Http2Headers headers = headers();
        encoder.headerTable().maxHeaderListSize(2);
        encoder.encodeHeaders(headers, Unpooled.buffer());
    }

    private static Http2Headers headers() {
        return new DefaultHttp2Headers().method(as("GET")).add(as("a"), as("1"))
                .add(as("a"), as("2"));
    }
}
