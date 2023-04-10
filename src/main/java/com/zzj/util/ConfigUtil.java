package com.zzj.util;

import com.zzj.util.filewatcher.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private static final Properties PROPERTIES = new Properties();

    private static final String CONFIT_FILE = Paths.get("config", "application.properties").toString();

    static {
        try {
            FileWatcher.register(new Watcher(getConfigFile().getPath()) {
                @Override
                public void handle() {
                    init();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Init config util error.", e);
        }
    }

    private static File getConfigFile() {
        File parent = new File(System.getProperty("java.class.path")).getParentFile();
        File config = Paths.get(parent.getPath(), CONFIT_FILE).toFile();
        if (!config.exists()) {
            config = new File(ConfigUtil.class.getClassLoader().getResource(config.getName()).getFile());
        }
        return config;
    }

    private static void init() {
        File config = getConfigFile();
        LOGGER.info("Init config from {}", config);
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(config.toPath()), StandardCharsets.UTF_8)) {
            PROPERTIES.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("Init config error", e);
        }
    }

    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new NullPointerException(StringUtil.format("Property[{}] is not exits in {}", key, CONFIT_FILE));
        }
        return value;
    }
}
