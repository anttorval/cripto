package com.atv.cripto.controllers;

import com.atv.cripto.services.ApiService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/cripto/api")
public class CriptoController {

    private final ApiService apiService;

    @Autowired
    public CriptoController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/process")
    public Flux<Map> processExcelFiles(){
        return this.apiService.callExchangeRestApi();
    }

    private static void readExcelFile() throws IOException {
        String pathName = "C:\\Users\\antonio\\Desktop\\workspace\\cripto\\src\\main\\java\\resources\\files";

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> processFile(path));
        }
    }

    private static void processFile(Path path) {
        try {
            FileInputStream file = new FileInputStream(path.toFile());
            Workbook workbook  = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                for (Cell cell : row) {
                    System.out.println(cell.toString() + " </br>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
