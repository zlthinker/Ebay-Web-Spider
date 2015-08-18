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

package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * A {@link NameResolver} that resolves an {@link InetSocketAddress} using JDK's built-in domain name lookup mechanism.
 * Note that this resolver performs a blocking name lookup from the caller thread.
 */
public class DefaultNameResolver extends SimpleNameResolver<InetSocketAddress> {

    public DefaultNameResolver(EventExecutor executor) {
        super(executor);
    }

    @Override
    protected boolean doIsResolved(InetSocketAddress address) {
        return !address.isUnresolved();
    }

    @Override
    protected void doResolve(InetSocketAddress unresolvedAddress, Promise<InetSocketAddress> promise) throws Exception {
        try {
            // Note that InetSocketAddress.getHostName() will never incur a reverse lookup here,
            // because an unresolved address always has a host name.
            promise.setSuccess(new InetSocketAddress(
                    InetAddress.getByName(unresolvedAddress.getHostName()), unresolvedAddress.getPort()));
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }
}
