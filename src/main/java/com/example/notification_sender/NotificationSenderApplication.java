package com.example.notification_sender;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@RequiredArgsConstructor
public class NotificationSenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationSenderApplication.class, args);
    }

}
