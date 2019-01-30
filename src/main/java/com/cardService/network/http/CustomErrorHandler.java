package com.cardService.network.http;

import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;

public class CustomErrorHandler extends ErrorHandler {

    @Override
    protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {}
}