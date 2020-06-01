package com.cardService.controller;

import com.cardService.grpc.HelloRequest;
import com.cardService.grpc.HelloResponse;
import com.cardService.grpc.HelloServiceGrpc;
import com.cardService.service.HelloService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GRpcService
public class HelloController extends HelloServiceGrpc.HelloServiceImplBase {

    @Autowired
    private HelloService helloService;

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        responseObserver.onNext(helloService.hello(request));
        responseObserver.onCompleted();
    }
}