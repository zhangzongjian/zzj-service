package com.zzj.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.ZipEntry;
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
                    IOUtils.copy(fis, zipOut);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
