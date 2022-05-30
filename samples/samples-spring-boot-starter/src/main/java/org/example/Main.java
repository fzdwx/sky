package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import sky.starter.UseSkyWebServer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date ${DATE} ${TIME}
 */
@SpringBootApplication
@UseSkyWebServer
public class Main {

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
    }
}