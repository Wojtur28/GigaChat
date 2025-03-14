package org.example.gigachat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;


@SpringBootApplication
public class GigaChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(GigaChatApplication.class, args);
    }

}
