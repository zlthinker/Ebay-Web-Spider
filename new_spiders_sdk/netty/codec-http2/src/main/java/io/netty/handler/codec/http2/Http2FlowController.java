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

import io.netty.channel.ChannelHandlerContext;

/**
 * Base interface for all HTTP/2 flow controllers.
 */
public interface Http2FlowController {

    /**
     * Sets the initial flow control window and updates all stream windows (but not the connection
     * window) by the delta.
     * <p>
     * This method is used to apply the {@code SETTINGS_INITIAL_WINDOW_SIZE} value for an
     * {@code SETTINGS} frame.
     *
     * @param newWindowSize the new initial window size.
     * @throws Http2Exception thrown if any protocol-related error occurred.
     */
    void initialWindowSize(int newWindowSize) throws Http2Exception;

    /**
     * Gets the initial flow control window size that is used as the basis for new stream flow
     * control windows.
     */
    int initialWindowSize();

    /**
     * Gets the number of bytes remaining in the flow control window size for the given stream.
     *
     * @param stream The subject stream. Use {@link Http2Connection#connectionStream()} for
     *            requesting the size of the connection window.
     * @return the current size of the flow control window.
     * @throws IllegalArgumentException if the given stream does not exist.
     */
    int windowSize(Http2Stream stream);

    /**
     * Increments the size of the stream's flow control window by the given delta.
     * <p>
     * In the case of a {@link Http2RemoteFlowController} this is called upon receipt of a
     * {@code WINDOW_UPDATE} frame from the remote endpoint to mirror the changes to the window
     * size.
     * <p>
     * For a {@link Http2LocalFlowController} this can be called to request the expansion of the
     * window size published by this endpoint. It is up to the implementation, however, as to when a
     * {@code WINDOW_UPDATE} is actually sent.
     *
     * @param ctx The context for the calling handler
     * @param stream The subject stream. Use {@link Http2Connection#connectionStream()} for
     *            requesting the size of the connection window.
     * @param delta the change in size of the flow control window.
     * @throws Http2Exception thrown if a protocol-related error occurred.
     */
    void incrementWindowSize(ChannelHandlerContext ctx, Http2Stream stream, int delta) throws Http2Exception;
}
