package com.zzj.util;

import lombok.SneakyThrows;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.math3.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class SystemUtil {
    @SneakyThrows
    public static String getLocalIpAddress() {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }

    public static Pair<Integer, String> exec(String command) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream);
        executor.setStreamHandler(streamHandler);
        int exitValue = executor.execute(CommandLine.parse(command));
        String output = outputStream.toString();
        return new Pair<>(exitValue, output);

    }
}
