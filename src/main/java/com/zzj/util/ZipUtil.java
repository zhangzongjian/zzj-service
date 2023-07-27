package com.zzj.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void compress(File directoryToCompress, File outputFile) {
        File parentFile = outputFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new RuntimeException("Create directory error. " + parentFile.getAbsolutePath());
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            Collection<File> fileList = FileUtils.listFiles(directoryToCompress, FileFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
            for (File file : fileList) {
                String filePath = file.getCanonicalPath();
                String entryName = filePath.substring(directoryToCompress.getCanonicalPath().length() + 1);
                ZipEntry zipEntry = new ZipEntry(Paths.get(directoryToCompress.getName(), entryName).toString());
                zipOut.putNextEntry(zipEntry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void unzip(String zipFilePath, String dirPath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
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
        if (!entryDestination.getCanonicalPath().startsWith(new File(checkDir).getCanonicalPath())) {
            throw new IOException("Potential zip slip detected.");
        }
        entryDestination.getParentFile().mkdirs();
        try (InputStream in = zipFile.getInputStream(entry);
             OutputStream out = Files.newOutputStream(entryDestination.toPath())) {
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
    }
}
