package com.cardService;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Helpers {
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
