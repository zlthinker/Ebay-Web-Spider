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

package io.netty.channel;

import io.netty.buffer.ByteBufUtil;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThreadLocalRandom;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * The default {@link ChannelId} implementation.
 */
final class DefaultChannelId implements ChannelId {

    private static final long serialVersionUID = 3884076183504074063L;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);

    private static final Pattern MACHINE_ID_PATTERN = Pattern.compile("^(?:[0-9a-fA-F][:-]?){6,8}$");
    private static final int MACHINE_ID_LEN = 8;
    private static final byte[] MACHINE_ID;
    private static final int PROCESS_ID_LEN = 4;
    // Maximal value for 64bit systems is 2^22.  See man 5 proc.
    // See https://github.com/netty/netty/issues/2706
    private static final int MAX_PROCESS_ID = 4194304;
    private static final int PROCESS_ID;
    private static final int SEQUENCE_LEN = 4;
    private static final int TIMESTAMP_LEN = 8;
    private static final int RANDOM_LEN = 4;

    private static final AtomicInteger nextSequence = new AtomicInteger();

    static ChannelId newInstance() {
        DefaultChannelId id = new DefaultChannelId();
        id.init();
        return id;
    }

    static {
        int processId = -1;
        String customProcessId = SystemPropertyUtil.get("io.netty.processId");
        if (customProcessId != null) {
            try {
                processId = Integer.parseInt(customProcessId);
            } catch (NumberFormatException e) {
                // Malformed input.
            }

            if (processId < 0 || processId > MAX_PROCESS_ID) {
                processId = -1;
                logger.warn("-Dio.netty.processId: {} (malformed)", customProcessId);
            } else if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.processId: {} (user-set)", processId);
            }
        }

        if (processId < 0) {
            processId = defaultProcessId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.processId: {} (auto-detected)", processId);
            }
        }

        PROCESS_ID = processId;

        byte[] machineId = null;
        String customMachineId = SystemPropertyUtil.get("io.netty.machineId");
        if (customMachineId != null) {
            if (MACHINE_ID_PATTERN.matcher(customMachineId).matches()) {
                machineId = parseMachineId(customMachineId);
                logger.debug("-Dio.netty.machineId: {} (user-set)", customMachineId);
            } else {
                logger.warn("-Dio.netty.machineId: {} (malformed)", customMachineId);
            }
        }

        if (machineId == null) {
            machineId = defaultMachineId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.machineId: {} (auto-detected)", formatAddress(machineId));
            }
        }

        MACHINE_ID = machineId;
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    private static byte[] parseMachineId(String value) {
        // Strip separators.
        value = value.replaceAll("[:-]", "");

        byte[] machineId = new byte[MACHINE_ID_LEN];
        for (int i = 0; i < value.length(); i += 2) {
            machineId[i] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
        }

        return machineId;
    }

    private static byte[] defaultMachineId() {
        // Find the best MAC address available.
        final byte[] NOT_FOUND = { -1 };
        byte[] bestMacAddr = NOT_FOUND;
        InetAddress bestInetAddr = null;
        try {
            bestInetAddr = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        } catch (UnknownHostException e) {
            // Never happens.
            PlatformDependent.throwException(e);
        }

        // Retrieve the list of available network interfaces.
        Map<NetworkInterface, InetAddress> ifaces = new LinkedHashMap<NetworkInterface, InetAddress>();
        try {
            for (Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {
                NetworkInterface iface = i.nextElement();
                // Use the interface with proper INET addresses only.
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                if (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (!a.isLoopbackAddress()) {
                        ifaces.put(iface, a);
                    }
                }
            }
        } catch (SocketException e) {
            logger.warn("Failed to retrieve the list of available network interfaces", e);
        }

        for (Entry<NetworkInterface, InetAddress> entry: ifaces.entrySet()) {
            NetworkInterface iface = entry.getKey();
            InetAddress inetAddr = entry.getValue();
            if (iface.isVirtual()) {
                continue;
            }

            byte[] macAddr;
            try {
                macAddr = iface.getHardwareAddress();
            } catch (SocketException e) {
                logger.debug("Failed to get the hardware address of a network interface: {}", iface, e);
                continue;
            }

            boolean replace = false;
            int res = compareAddresses(bestMacAddr, macAddr);
            if (res < 0) {
                // Found a better MAC address.
                replace = true;
            } else if (res == 0) {
                // Two MAC addresses are of pretty much same quality.
                res = compareAddresses(bestInetAddr, inetAddr);
                if (res < 0) {
                    // Found a MAC address with better INET address.
                    replace = true;
                } else if (res == 0) {
                    // Cannot tell the difference.  Choose the longer one.
                    if (bestMacAddr.length < macAddr.length) {
                        replace = true;
                    }
                }
            }

            if (replace) {
                bestMacAddr = macAddr;
                bestInetAddr = inetAddr;
            }
        }

        if (bestMacAddr == NOT_FOUND) {
            bestMacAddr = new byte[MACHINE_ID_LEN];
            ThreadLocalRandom.current().nextBytes(bestMacAddr);
            logger.warn(
                    "Failed to find a usable hardware address from the network interfaces; using random bytes: {}",
                    formatAddress(bestMacAddr));
        }

        switch (bestMacAddr.length) {
            case 6: // EUI-48 - convert to EUI-64
                byte[] newAddr = new byte[MACHINE_ID_LEN];
                System.arraycopy(bestMacAddr, 0, newAddr, 0, 3);
                newAddr[3] = (byte) 0xFF;
                newAddr[4] = (byte) 0xFE;
                System.arraycopy(bestMacAddr, 3, newAddr, 5, 3);
                bestMacAddr = newAddr;
                break;
            default: // Unknown
                bestMacAddr = Arrays.copyOf(bestMacAddr, MACHINE_ID_LEN);
        }

        return bestMacAddr;
    }

    /**
     * @return positive - current is better, 0 - cannot tell from MAC addr, negative - candidate is better.
     */
    private static int compareAddresses(byte[] current, byte[] candidate) {
        if (candidate == null) {
            return 1;
        }

        // Must be EUI-48 or longer.
        if (candidate.length < 6) {
            return 1;
        }

        // Must not be filled with only 0 and 1.
        boolean onlyZeroAndOne = true;
        for (byte b: candidate) {
            if (b != 0 && b != 1) {
                onlyZeroAndOne = false;
                break;
            }
        }

        if (onlyZeroAndOne) {
            return 1;
        }

        // Must not be a multicast address
        if ((candidate[0] & 1) != 0) {
            return 1;
        }

        // Prefer globally unique address.
        if ((current[0] & 2) == 0) {
            if ((candidate[0] & 2) == 0) {
                // Both current and candidate are globally unique addresses.
                return 0;
            } else {
                // Only current is globally unique.
                return 1;
            }
        } else {
            if ((candidate[0] & 2) == 0) {
                // Only candidate is globally unique.
                return -1;
            } else {
                // Both current and candidate are non-unique.
                return 0;
            }
        }
    }

    /**
     * @return positive - current is better, 0 - cannot tell, negative - candidate is better
     */
    private static int compareAddresses(InetAddress current, InetAddress candidate) {
        return scoreAddress(current) - scoreAddress(candidate);
    }

    private static int scoreAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress()) {
            return 0;
        }
        if (addr.isMulticastAddress()) {
            return 1;
        }
        if (addr.isLinkLocalAddress()) {
            return 2;
        }
        if (addr.isSiteLocalAddress()) {
            return 3;
        }

        return 4;
    }

    private static String formatAddress(byte[] addr) {
        StringBuilder buf = new StringBuilder(24);
        for (byte b: addr) {
            buf.append(String.format("%02x:", b & 0xff));
        }
        return buf.substring(0, buf.length() - 1);
    }

    private static int defaultProcessId() {
        final ClassLoader loader = PlatformDependent.getSystemClassLoader();
        String value;
        try {
            // Invoke java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
            Class<?> mgmtFactoryType = Class.forName("java.lang.management.ManagementFactory", true, loader);
            Class<?> runtimeMxBeanType = Class.forName("java.lang.management.RuntimeMXBean", true, loader);

            Method getRuntimeMXBean = mgmtFactoryType.getMethod("getRuntimeMXBean", EmptyArrays.EMPTY_CLASSES);
            Object bean = getRuntimeMXBean.invoke(null, EmptyArrays.EMPTY_OBJECTS);
            Method getName = runtimeMxBeanType.getDeclaredMethod("getName", EmptyArrays.EMPTY_CLASSES);
            value = (String) getName.invoke(bean, EmptyArrays.EMPTY_OBJECTS);
        } catch (Exception e) {
            logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(); Android?", e);
            try {
                // Invoke android.os.Process.myPid()
                Class<?> processType = Class.forName("android.os.Process", true, loader);
                Method myPid = processType.getMethod("myPid", EmptyArrays.EMPTY_CLASSES);
                value = myPid.invoke(null, EmptyArrays.EMPTY_OBJECTS).toString();
            } catch (Exception e2) {
                logger.debug("Could not invoke Process.myPid(); not Android?", e2);
                value = "";
            }
        }

        int atIndex = value.indexOf('@');
        if (atIndex >= 0) {
            value = value.substring(0, atIndex);
        }

        int pid;
        try {
            pid = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // value did not contain an integer.
            pid = -1;
        }

        if (pid < 0 || pid > MAX_PROCESS_ID) {
            pid = ThreadLocalRandom.current().nextInt(MAX_PROCESS_ID + 1);
            logger.warn("Failed to find the current process ID from '{}'; using a random value: {}",  value, pid);
        }

        return pid;
    }

    private final byte[] data = new byte[MACHINE_ID_LEN + PROCESS_ID_LEN + SEQUENCE_LEN + TIMESTAMP_LEN + RANDOM_LEN];
    private int hashCode;

    private transient String shortValue;
    private transient String longValue;

    private void init() {
        int i = 0;

        // machineId
        System.arraycopy(MACHINE_ID, 0, data, i, MACHINE_ID_LEN);
        i += MACHINE_ID_LEN;

        // processId
        i = writeInt(i, PROCESS_ID);

        // sequence
        i = writeInt(i, nextSequence.getAndIncrement());

        // timestamp (kind of)
        i = writeLong(i, Long.reverse(System.nanoTime()) ^ System.currentTimeMillis());

        // random
        int random = ThreadLocalRandom.current().nextInt();
        hashCode = random;
        i = writeInt(i, random);

        assert i == data.length;
    }

    private int writeInt(int i, int value) {
        data[i ++] = (byte) (value >>> 24);
        data[i ++] = (byte) (value >>> 16);
        data[i ++] = (byte) (value >>> 8);
        data[i ++] = (byte) value;
        return i;
    }

    private int writeLong(int i, long value) {
        data[i ++] = (byte) (value >>> 56);
        data[i ++] = (byte) (value >>> 48);
        data[i ++] = (byte) (value >>> 40);
        data[i ++] = (byte) (value >>> 32);
        data[i ++] = (byte) (value >>> 24);
        data[i ++] = (byte) (value >>> 16);
        data[i ++] = (byte) (value >>> 8);
        data[i ++] = (byte) value;
        return i;
    }

    @Override
    public String asShortText() {
        String shortValue = this.shortValue;
        if (shortValue == null) {
            this.shortValue = shortValue = ByteBufUtil.hexDump(
                    data, MACHINE_ID_LEN + PROCESS_ID_LEN + SEQUENCE_LEN + TIMESTAMP_LEN, RANDOM_LEN);
        }
        return shortValue;
    }

    @Override
    public String asLongText() {
        String longValue = this.longValue;
        if (longValue == null) {
            this.longValue = longValue = newLongValue();
        }
        return longValue;
    }

    private String newLongValue() {
        StringBuilder buf = new StringBuilder(2 * data.length + 5);
        int i = 0;
        i = appendHexDumpField(buf, i, MACHINE_ID_LEN);
        i = appendHexDumpField(buf, i, PROCESS_ID_LEN);
        i = appendHexDumpField(buf, i, SEQUENCE_LEN);
        i = appendHexDumpField(buf, i, TIMESTAMP_LEN);
        i = appendHexDumpField(buf, i, RANDOM_LEN);
        assert i == data.length;
        return buf.substring(0, buf.length() - 1);
    }

    private int appendHexDumpField(StringBuilder buf, int i, int length) {
        buf.append(ByteBufUtil.hexDump(data, i, length));
        buf.append('-');
        i += length;
        return i;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(ChannelId o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DefaultChannelId)) {
            return false;
        }

        return Arrays.equals(data, ((DefaultChannelId) obj).data);
    }

    @Override
    public String toString() {
        return asShortText();
    }
}
