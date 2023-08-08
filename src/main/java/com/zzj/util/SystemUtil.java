package com.zzj.util;

import lombok.SneakyThrows;

import java.net.InetAddress;

public class SystemUtil {
    @SneakyThrows
    public static String getLocalIpAddress() {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
