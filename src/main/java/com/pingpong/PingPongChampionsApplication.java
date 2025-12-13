package com.pingpong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PingPongChampionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingPongChampionsApplication.class, args);
    }
}
