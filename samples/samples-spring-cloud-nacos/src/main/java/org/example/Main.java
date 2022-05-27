package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date ${DATE} ${TIME}
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}