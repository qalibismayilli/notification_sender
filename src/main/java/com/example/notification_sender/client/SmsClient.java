package com.example.notification_sender.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "smsClient", url = "http://gw.soft-line.az")
public interface SmsClient {

    @GetMapping("/sendsms")
    String sendSms(
            @RequestParam("user") String user,
            @RequestParam("password") String password,
            @RequestParam("gsm") String gsm,
            @RequestParam("from") String from,
            @RequestParam("text") String text
    );
}

