package com.atv.cripto.controllers;

import com.atv.cripto.services.CriptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/cripto/api")
public class CriptoController {

    private final CriptoService criptoService;

    @Autowired
    public CriptoController(CriptoService criptoService) {
        this.criptoService = criptoService;
    }

    @GetMapping("/process")
    public Flux<Map> processExcelFiles() throws IOException {
        return this.criptoService.processFiles();
    }
}
