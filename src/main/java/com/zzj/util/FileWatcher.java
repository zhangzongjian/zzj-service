package com.zzj.util;


import com.zzj.util.filewatcher.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileWatcher {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private static final Map<File, List<Watcher>> WATCHER_MAP = new HashMap<>();

    private static final WatchService watchService = newWatchService();

    static {
        new Thread("File_watch_thread") {
            @Override
            public void run() {
                while (true) {
                    File file = null;
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path eventPath = (Path) event.context();
                            Path eventDir = (Path) key.watchable();
                            file = Paths.get(eventDir.toString(), eventPath.toString()).toFile();
                            handleFile(file);
                            LOGGER.info("Event kind:{}. File affected: {}.", event.kind(), file);
                        }
                        key.reset();
                    } catch (Exception e) {
                        throw new RuntimeException("File watcher error. " + Optional.ofNullable(file).map(File::getPath).orElse(null), e);
                    }
                }

            }
        }.start();
    }

    private static void handleFile(File file) {
        List<Watcher> list = WATCHER_MAP.getOrDefault(file, Collections.emptyList());
        for (Watcher watcher : list) {
            try {
                watcher.handle();
            } catch (Exception e) {
                LOGGER.error("File watcher handle error. " + file, e);
            }
        }
    }

    private static WatchService newWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Init watch service error.", e);
        }
    }

    public static void register(Watcher watcher) throws IOException {
        LOGGER.info("Register file watcher. {}", watcher.getFile());
        Path path = Paths.get(watcher.getFile()).getParent();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        WATCHER_MAP.computeIfAbsent(new File(watcher.getFile()), key -> new ArrayList<>()).add(watcher);
    }
}