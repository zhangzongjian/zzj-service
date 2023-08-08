package com.zzj.util;

import com.zzj.util.filewatcher.Watcher;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private static final Properties PROPERTIES = new Properties();

    private static final Path CONFIG_FILE = Paths.get("config", "application.properties");

    static {
        try {
            init();
            registerWatcher();
        } catch (Exception e) {
            throw new RuntimeException("Init config util error.", e);
        }
    }

    private static void registerWatcher() throws IOException {
        File configFile = getConfigFile();
        if (configFile == null) {
            return;
        }
        FileWatcher.register(new Watcher(getConfigFile().getPath()) {
            @Override
            public void handle() {
                init();
            }
        });
    }

    private static File getConfigFile() {
        File parent = new File(System.getProperty("java.class.path").split(";")[0]).getParentFile();
        File config = Paths.get(parent.getAbsolutePath(), CONFIG_FILE.toString()).toFile();
        return config.exists() ? config : null;
    }

    /**
     * 优先加载包外面的config/application.properties
     */
    private static void init() {
        try {
            File config = getConfigFile();
            InputStream in;
            if (config != null) {
                LOGGER.info("Init config from {}", config);
                in = Files.newInputStream(config.toPath());
            } else {
                String fileName = CONFIG_FILE.getFileName().toString();
                ClassPathResource resource = new ClassPathResource(fileName);
                LOGGER.info("Init config from classpath: {}", resource.getURL());
                in = resource.getInputStream();
            }
            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            PROPERTIES.load(reader);
        } catch (Exception e) {
            LOGGER.error("Init config util error.", e);
        }
    }

    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new NullPointerException(StringUtil.format("Property[{}] is not exits in {}", key, CONFIG_FILE));
        }
        return value;
    }

    public static int getPropertyInt(String key) {
        return NumberUtils.toInt(getProperty(key));
    }
}
