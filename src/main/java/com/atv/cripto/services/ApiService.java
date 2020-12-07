package com.atv.cripto.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ApiService {

    private final WebClient webClient;

@Autowired
    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.exchangeratesapi.io").build();
    }

    public Flux<Map> callExchangeRestApi(){
        return this.webClient.get().uri("/history?start_at=2018-01-01&end_at=2018-09-01").retrieve().bodyToFlux(Map.class);
    }
}
