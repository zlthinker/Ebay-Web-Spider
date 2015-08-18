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
package io.netty.channel.epoll;

import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketReadMode;

public final class EpollChannelOption {
    private static final Class<EpollChannelOption> T = EpollChannelOption.class;

    public static final ChannelOption<Boolean> TCP_CORK = ChannelOption.valueOf(T, "TCP_CORK");
    public static final ChannelOption<Boolean> SO_REUSEPORT = ChannelOption.valueOf(T, "SO_REUSEPORT");
    public static final ChannelOption<Integer> TCP_KEEPIDLE = ChannelOption.valueOf(T, "TCP_KEEPIDLE");
    public static final ChannelOption<Integer> TCP_KEEPINTVL = ChannelOption.valueOf(T, "TCP_KEEPINTVL");
    public static final ChannelOption<Integer> TCP_KEEPCNT = ChannelOption.valueOf(T, "TCP_KEEPCNT");
    public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE =
            ChannelOption.valueOf(T, "DOMAIN_SOCKET_READ_MODE");
    public static final ChannelOption<EpollMode> EPOLL_MODE =
            ChannelOption.valueOf(T, "EPOLL_MODE");
    private EpollChannelOption() { }
}
