package org.example.libraryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.example.libraryservice")
@EnableMongoRepositories(basePackages = "org.example.libraryservice.book")
public class DbmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbmsApplication.class, args);
    }

}
