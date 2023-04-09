package com.zzj.service.controller;

import com.zzj.exception.PageNotFountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractController {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @ExceptionHandler(Throwable.class)
    public String handleThrowable(Throwable e) {
        LOGGER.error("Throwable", e);
        return "common/500";
    }

    @ExceptionHandler(PageNotFountException.class)
    public String pageNotFoundException(Throwable e) {
        LOGGER.error("PageNotFountException", e);
        return "common/404";
    }
}
