package com.cardService.service;

import com.cardService.grpc.HelloRequest;
import com.cardService.grpc.HelloResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelloService {
    public HelloResponse hello(HelloRequest request) {
        //log.info("Received Ping message {}", new Gson().toJson(request));
        String greeting = String.format("Hello, %s %s", request.getFirstName(), request.getLastName());
        return HelloResponse.newBuilder()
                .setGreeting(greeting)
                .build();
    }
}
