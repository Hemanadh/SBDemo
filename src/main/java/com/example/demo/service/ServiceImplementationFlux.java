package com.example.demo.service;

import com.example.demo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ServiceImplementationFlux implements IService{

   private final ExternalServiceImplementation externalServiceImplementation;


    Logger log = LoggerFactory.getLogger(ServiceImplementationFlux.class);

    public ServiceImplementationFlux(ExternalServiceImplementation externalServiceImplementation){
        this.externalServiceImplementation = externalServiceImplementation;
    }


    @Override
    public Mono<List<User>> getUserInfo() {

        Mono<List<User>> mono1 = externalServiceImplementation.getUsers();

        Mono<List<User>> mono2 = externalServiceImplementation.addNewUser();

        Mono<User> mono3 = externalServiceImplementation.getUser();

        Mono<User> mono4 = externalServiceImplementation.modifyUser();

        return Mono.zip(mono1, mono2, mono3, mono4).flatMap(tuple -> {
            List<User> fromGet = tuple.getT1();
            List<User> fromPost = tuple.getT2();
            User fromGetWithName = tuple.getT3();
            User fromPut = tuple.getT4();
            log.info("In Mono zip");

            // Combine the results, even if some Monos failed
            return Mono.just(
                    Stream.concat(
                            Stream.of(fromGet, fromPost).flatMap(List::stream),  // Combine lists
                            Stream.of(fromGetWithName, fromPut)                  // Add individual Users
                    ).collect(Collectors.toList())
            );
        });
    }


}
