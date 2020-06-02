package com.cardService.controller;

import com.cardService.grpc.HelloRequest;
import com.cardService.grpc.HelloResponse;
import com.cardService.grpc.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class HelloControllerTest {

    @Test
    public void hello() {
        HelloRequest request = HelloRequest.newBuilder()
                .setFirstName("Toshek")
                .setLastName("Boshek")
                .build();
        HelloResponse response = smsServiceStub().hello(request);
        Assert.assertTrue(response.getGreeting().contains(request.getFirstName()));
    }

    private HelloServiceGrpc.HelloServiceBlockingStub smsServiceStub() {
        ManagedChannel channel =
                ManagedChannelBuilder.forTarget("localhost:6790").usePlaintext().build();
        return HelloServiceGrpc.newBlockingStub(channel);
    }
}