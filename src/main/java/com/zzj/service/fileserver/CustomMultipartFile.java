package com.zzj.service.fileserver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
public class CustomMultipartFile implements MultipartFile {
    private InputStream inputStream;

    private String originalFilename;

    private long size;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {

    }
}
