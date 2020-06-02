package com.cardService;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;


@SpringBootApplication
public class CoreApplication implements DisposableBean, CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(CoreApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(CoreApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String pidFile = System.getProperty("pidfile");
        if (pidFile != null) {
            new File(pidFile).deleteOnExit();
        }
    }

    @Override
    public void destroy() {
    }
}
