package com.zzj.service.controller;

import com.zzj.constant.RestConstant;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@RestController
public class GreetingController {

    private final static Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);

    private final AtomicLong counter = new AtomicLong();

    @GetMapping(RestConstant.REST_GREET)
    public Map<String, Object> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("hello", "name1");
        map.put("count", counter.getAndDecrement());
        LOGGER.info("greeting!");
        return map;
    }

    @PutMapping("/receiveInputStream")
    public String receiveInputStream(@RequestBody byte[] inputStream) {
        String str = IOUtils.toString(inputStream, "UTF-8");
        System.out.println(IOUtils.toString(inputStream, "UTF-8"));
        return str;
    }

    public static void main(String[] args) throws IOException {
        String zipFile = "D:\\ftp\\compressedFile.zip";
        String newZipFile = "D:\\ftp\\compressedFileNew.zip";
        File file1 = new File("D:\\ftp\\ftp1.zip");
        File file2 = new File("D:\\ftp\\ftp2.zip");
        String dir = "D:\\ftp\\abc";
        unzip(zipFile, dir);
        Files.move(file1.toPath(), Paths.get(dir, file1.getName()));
        Files.move(file2.toPath(), Paths.get(dir, file2.getName()));
        zip(newZipFile, dir);
    }

    public static void zip(String zipFileName, String dirPath) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            Arrays.stream(Optional.ofNullable(new File(dirPath).listFiles()).orElse(new File[0])).forEach(file -> zipFile(file, zos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipFile(File file, ZipOutputStream zos) {
        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(new ZipEntry(file.getName()));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String zipFileName, String dirPath) {
        try (ZipFile zipFile = new ZipFile(zipFileName)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int entriesCount = 0;
            while (entries.hasMoreElements()) {
                entriesCount++;
                if (entriesCount > 1000) {
                    throw new IOException("Potential zip bomb detected due to excessive number of entries.");
                }
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(dirPath, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    processFileEntry(zipFile, entry, entryDestination, dirPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFileEntry(ZipFile zipFile, ZipEntry entry, File entryDestination, String checkDir) throws IOException {
        if (entryDestination.getCanonicalPath().startsWith(new File(checkDir).getCanonicalPath())) {
            entryDestination.getParentFile().mkdirs();
            try (InputStream in = zipFile.getInputStream(entry); OutputStream out = new FileOutputStream(entryDestination)) {
                byte[] buffer = new byte[1024];
                int length;
                long totalBytesRead = 0;
                while ((length = in.read(buffer)) > 0) {
                    totalBytesRead += length;
                    if (totalBytesRead > 1000000000) {
                        throw new IOException("Potential zip bomb detected.");
                    }
                    out.write(buffer, 0, length);
                }
            }
        } else {
            throw new IOException("Potential zip slip detected.");
        }
    }

}