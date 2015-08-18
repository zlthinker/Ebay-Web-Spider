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
package io.netty.handler.ssl;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator.ProtocolSelectionListener;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator.ProtocolSelector;
import io.netty.util.internal.PlatformDependent;

import java.util.LinkedHashSet;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.ALPN.ClientProvider;
import org.eclipse.jetty.alpn.ALPN.ServerProvider;

final class JdkAlpnSslEngine extends JdkSslEngine {
    private static boolean available;

    static boolean isAvailable() {
        updateAvailability();
        return available;
    }

    private static void updateAvailability() {
        if (available) {
            return;
        }

        try {
            // Try to get the bootstrap class loader.
            ClassLoader bootloader = ClassLoader.getSystemClassLoader().getParent();
            if (bootloader == null) {
                // If failed, use the system class loader,
                // although it's not perfect to tell if APLN extension has been loaded.
                bootloader = ClassLoader.getSystemClassLoader();
            }
            Class.forName("sun.security.ssl.ALPNExtension", true, bootloader);
            available = true;
        } catch (Exception ignore) {
            // alpn-boot was not loaded.
        }
    }

    JdkAlpnSslEngine(SSLEngine engine, final JdkApplicationProtocolNegotiator applicationNegotiator, boolean server) {
        super(engine);
        checkNotNull(applicationNegotiator, "applicationNegotiator");

        if (server) {
            final ProtocolSelector protocolSelector = checkNotNull(applicationNegotiator.protocolSelectorFactory()
                    .newSelector(this, new LinkedHashSet<String>(applicationNegotiator.protocols())),
                    "protocolSelector");
            ALPN.put(engine, new ServerProvider() {
                @Override
                public String select(List<String> protocols) {
                    try {
                        return protocolSelector.select(protocols);
                    } catch (Throwable t) {
                        PlatformDependent.throwException(t);
                        return null;
                    }
                }

                @Override
                public void unsupported() {
                    protocolSelector.unsupported();
                }
            });
        } else {
            final ProtocolSelectionListener protocolListener = checkNotNull(applicationNegotiator
                    .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()),
                    "protocolListener");
            ALPN.put(engine, new ClientProvider() {
                @Override
                public List<String> protocols() {
                    return applicationNegotiator.protocols();
                }

                @Override
                public void selected(String protocol) {
                    try {
                        protocolListener.selected(protocol);
                    } catch (Throwable t) {
                        PlatformDependent.throwException(t);
                    }
                }

                @Override
                public void unsupported() {
                    protocolListener.unsupported();
                }
            });
        }
    }

    @Override
    public void closeInbound() throws SSLException {
        ALPN.remove(getWrappedEngine());
        super.closeInbound();
    }

    @Override
    public void closeOutbound() {
        ALPN.remove(getWrappedEngine());
        super.closeOutbound();
    }
}
