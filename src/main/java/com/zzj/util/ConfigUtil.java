package com.zzj.util;

import com.zzj.util.filewatcher.Watcher;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class ConfigUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private static final Properties PROPERTIES = new Properties();

    private static final String CONFIG_NAME = "application.properties";

    private static boolean hasRegisterWatcher = false;

    static {
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException("Init config util error.", e);
        }
    }

    private static void registerWatcher(File config) throws IOException {
        if (hasRegisterWatcher) {
            return;
        }
        FileWatcher.register(new Watcher(config.getPath()) {
            @Override
            public void handle() {
                init();
            }
        });
        hasRegisterWatcher = true;
    }

    private static void load(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        PROPERTIES.putAll(properties);
    }

    /**
     * 配置优先级：resources目录>war包外>war包内
     */
    private static void init() {
        try {
            // idea resources目录
            // java -jar xxx.war 时加载war包内配置
            load(new ClassPathResource(CONFIG_NAME).getInputStream());
            // java -jar xxx.war 时加载war包外配置
            File parent = new File(System.getProperty("java.class.path")).getParentFile();
            File config = new File(parent.getAbsolutePath(), CONFIG_NAME);
            if (config.exists()) {
                load(Files.newInputStream(config.toPath()));
                registerWatcher(config);
            }
        } catch (Exception e) {
            LOGGER.error("Init config util error.", e);
        }
    }

    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new NullPointerException(StringUtil.format("Property[{}] is not exits in {}", key, CONFIG_NAME));
        }
        return value;
    }

    public static int getPropertyInt(String key) {
        return NumberUtils.toInt(getProperty(key));
    }
}
