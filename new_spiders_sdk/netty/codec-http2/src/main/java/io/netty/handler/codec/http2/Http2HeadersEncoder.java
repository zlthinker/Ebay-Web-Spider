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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.AsciiString;

/**
 * Encodes {@link Http2Headers} into HPACK-encoded headers blocks.
 */
public interface Http2HeadersEncoder {
    /**
     * Configuration related elements for the {@link Http2HeadersEncoder} interface
     */
    interface Configuration {
        /**
         * Access the Http2HeaderTable for this {@link Http2HeadersEncoder}
         */
        Http2HeaderTable headerTable();
    }

    /**
     * Determine if a header name/value pair is treated as
     * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
     * If the object can be dynamically modified and shared across multiple connections it may need to be thread safe.
     */
    interface SensitivityDetector {
        /**
         * Determine if a header {@code name}/{@code value} pair should be treated as
         * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
         * @param name The name for the header.
         * @param value The value of the header.
         * @return {@code true} if a header {@code name}/{@code value} pair should be treated as
         * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
         * {@code false} otherwise.
         */
        boolean isSensitive(AsciiString name, AsciiString value);
    }

    /**
     * Encodes the given headers and writes the output headers block to the given output buffer.
     *
     * @param headers the headers to be encoded.
     * @param buffer the buffer to receive the encoded headers.
     */
    void encodeHeaders(Http2Headers headers, ByteBuf buffer) throws Http2Exception;

    /**
     * Get the {@link Configuration} for this {@link Http2HeadersEncoder}
     */
    Configuration configuration();

    /**
     * Always return {@code false} for {@link SensitivityDetector#isSensitive(AsciiString, AsciiString)}.
     */
    SensitivityDetector NEVER_SENSITIVE = new SensitivityDetector() {
        @Override
        public boolean isSensitive(AsciiString name, AsciiString value) {
            return false;
        }
    };
}
