package wang.liangchen.matrix.bpmjob.common.utils;

import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author Liangchen.Wang
 */
public enum NetUtil {
    // instance;
    INSTANCE;
    private final InetAddress inetAddress;


    NetUtil() {
        try {
            inetAddress = getLocalInetAddress();
        } catch (SocketException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public String getLocalHostAddress() {
        return inetAddress.getHostAddress();
    }

    public String getLocalHostName() {
        return inetAddress.getHostName();
    }

    private InetAddress getLocalInetAddress() throws SocketException {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress = null;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                // 排除loopback
                if (networkInterface.isLoopback()) {
                    continue;
                }
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress()) {
                        break;
                    }
                }
            }
            return inetAddress;
        }
    }
}
