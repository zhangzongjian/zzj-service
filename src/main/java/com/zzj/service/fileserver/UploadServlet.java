package com.zzj.service.fileserver;

import com.zzj.util.BeanUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/upload_")
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        FileServerController fileServer = BeanUtil.getBean(FileServerController.class);
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);
        Map<String, String> formFields = new HashMap<>();
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();
            if (!item.isFormField()) {
                CustomMultipartFile multipartFile = new CustomMultipartFile();
                multipartFile.setSize(NumberUtils.toLong(formFields.get("length"), 0));
                multipartFile.setOriginalFilename(item.getName());
                multipartFile.setInputStream(item.openStream());
                fileServer.handleFileUpload(multipartFile, formFields.get("name"));
            } else {
                formFields.put(item.getFieldName(), IOUtils.toString(item.openStream(), StandardCharsets.UTF_8));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPut(request, response);
    }
}

