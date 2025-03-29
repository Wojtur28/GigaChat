package org.example.gigachat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;


@SpringBootApplication
@EnableReactiveMongoRepositories
public class GigaChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(GigaChatApplication.class, args);
    }

}
