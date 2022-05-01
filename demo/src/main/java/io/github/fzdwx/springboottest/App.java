package io.github.fzdwx.springboottest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/29 14:21
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        final var context = SpringApplication.run(App.class, args);
    }
}