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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.TypeParameterMatcher;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;

/**
 * A skeletal {@link NameResolver} implementation.
 */
public abstract class SimpleNameResolver<T extends SocketAddress> implements NameResolver<T> {

    private final EventExecutor executor;
    private final TypeParameterMatcher matcher;

    /**
     * @param executor the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned
     *                 by {@link #resolve(SocketAddress)}
     */
    protected SimpleNameResolver(EventExecutor executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }

        this.executor = executor;
        matcher = TypeParameterMatcher.find(this, SimpleNameResolver.class, "T");
    }

    /**
     * @param executor the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned
     *                 by {@link #resolve(SocketAddress)}
     * @param addressType the type of the {@link SocketAddress} supported by this resolver
     */
    protected SimpleNameResolver(EventExecutor executor, Class<? extends T> addressType) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }

        this.executor = executor;
        matcher = TypeParameterMatcher.get(addressType);
    }

    /**
     * Returns the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned
     * by {@link #resolve(SocketAddress)}.
     */
    protected EventExecutor executor() {
        return executor;
    }

    @Override
    public boolean isSupported(SocketAddress address) {
        return matcher.match(address);
    }

    @Override
    public final boolean isResolved(SocketAddress address) {
        if (!isSupported(address)) {
            throw new UnsupportedAddressTypeException();
        }

        @SuppressWarnings("unchecked")
        final T castAddress = (T) address;
        return doIsResolved(castAddress);
    }

    /**
     * Invoked by {@link #isResolved(SocketAddress)} to check if the specified {@code address} has been resolved
     * already.
     */
    protected abstract boolean doIsResolved(T address);

    @Override
    public final Future<T> resolve(String inetHost, int inetPort) {
        if (inetHost == null) {
            throw new NullPointerException("inetHost");
        }

        return resolve(InetSocketAddress.createUnresolved(inetHost, inetPort));
    }

    @Override
    public Future<T> resolve(String inetHost, int inetPort, Promise<T> promise) {
        if (inetHost == null) {
            throw new NullPointerException("inetHost");
        }

        return resolve(InetSocketAddress.createUnresolved(inetHost, inetPort), promise);
    }

    @Override
    public final Future<T> resolve(SocketAddress address) {
        if (address == null) {
            throw new NullPointerException("unresolvedAddress");
        }

        if (!isSupported(address)) {
            // Address type not supported by the resolver
            return executor().newFailedFuture(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // Resolved already; no need to perform a lookup
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return executor.newSucceededFuture(cast);
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            final Promise<T> promise = executor().newPromise();
            doResolve(cast, promise);
            return promise;
        } catch (Exception e) {
            return executor().newFailedFuture(e);
        }
    }

    @Override
    public final Future<T> resolve(SocketAddress address, Promise<T> promise) {
        if (address == null) {
            throw new NullPointerException("unresolvedAddress");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }

        if (!isSupported(address)) {
            // Address type not supported by the resolver
            return promise.setFailure(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // Resolved already; no need to perform a lookup
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return promise.setSuccess(cast);
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            doResolve(cast, promise);
            return promise;
        } catch (Exception e) {
            return promise.setFailure(e);
        }
    }

    /**
     * Invoked by {@link #resolve(SocketAddress)} and {@link #resolve(String, int)} to perform the actual name
     * resolution.
     */
    protected abstract void doResolve(T unresolvedAddress, Promise<T> promise) throws Exception;

    @Override
    public void close() { }
}
