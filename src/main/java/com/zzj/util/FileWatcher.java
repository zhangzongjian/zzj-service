package com.zzj.util;


import java.nio.file.*;

public class FileWatcher {
    public static void main(String[] args) throws Exception {
        // 获取要监听的文件路径
        Path path = Paths.get("C:\\Users\\jian\\cursor-tutor\\");

        // 获取文件系统的 WatchService 对象
        WatchService watchService = FileSystems.getDefault().newWatchService();

        // 注册监听器
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        // 循环监听
        while (true) {
            WatchKey watchKey = watchService.take();
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                // 处理事件
                System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            }
            watchKey.reset();
        }
    }
}