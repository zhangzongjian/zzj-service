package com.zzj.util;

import lombok.SneakyThrows;
import org.apache.commons.math3.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.function.Consumer;

public class SystemUtil {
    @SneakyThrows
    public static String getLocalIpAddress() {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }

    public static Pair<Integer, String> exec(String[] command, long timeout) throws IOException {
        return exec(command, timeout, null);
    }

    public static Pair<Integer, String> exec(String[] command, long timeout, Consumer<String> callback) throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            long startTime = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
                if (callback != null) {
                    callback.accept(line);
                }
                if (System.currentTimeMillis() - startTime > timeout) {
                    process.destroy();
                    throw new IOException("Command execution timed out");
                }
            }
            int exitCode = process.waitFor();
            return new Pair<>(exitCode, output.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command execution interrupted", e);
        }
    }


}
