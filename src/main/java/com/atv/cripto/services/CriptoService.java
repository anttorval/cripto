package com.atv.cripto.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class CriptoService {

    private final WebClient webClient;

@Autowired
    public CriptoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.exchangeratesapi.io").build();
    }

    private Mono<Map> callExchangeRestApi(String date){
        return this.webClient.get().uri("/"+date).retrieve().bodyToMono(Map.class);
    }

    public Flux<Map> processFiles() throws IOException {
    this.readExcelFile();

    return Flux.empty();
    }

    private void readExcelFile() throws IOException {
        String pathName = "C:\\Users\\antonio\\Desktop\\workspace\\cripto\\src\\main\\java\\resources\\files";

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> processFile(path));
        }
    }

    private void processFile(Path path) {
        try {
            FileInputStream file = new FileInputStream(path.toFile());
            Workbook workbook  = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Map<String,Object>> rowsMap = new HashMap<>();
            for (Row row : sheet) {
                if(row.getRowNum() != 0){
                        Map<String, Object> mapValue = new HashMap<>();
                        mapValue.put("fecha", row.getCell(0).getStringCellValue());
                        mapValue.put("costeDolares", row.getCell(1).getNumericCellValue());
                        mapValue.put("costeEuros", row.getCell(2).getNumericCellValue());
                        mapValue.put("costeEth", row.getCell(3).getNumericCellValue());
                        mapValue.put("costeBtc", row.getCell(4).getNumericCellValue());

                        rowsMap.put("row_"+row.getRowNum(), mapValue);
                }
            }
            
            this.calculateEurosColumn(rowsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void calculateEurosColumn(Map<String, Map<String, Object>> rowsMap) {
        rowsMap.entrySet().stream().forEach(row -> {
            if(row.getValue().get("costeEuros").equals(Double.valueOf("0.0"))){
                String date = this.formatDate((String) row.getValue().get("fecha"));
                Mono<Map> apiResult = this.callExchangeRestApi(date);
                Double USDPrice = (Double) ((Map)apiResult.block().get("rates")).get("USD");
                row.getValue().put("costeEuros",this.calculateEurosFromUSD((Double) row.getValue().get("costeDolares"), USDPrice));
            }
        });
    }

    private Double calculateEurosFromUSD(Double costeDolares, Double usdPrice) {
        return costeDolares/usdPrice;
    }

    private String formatDate(String dateString) {
        try {
            Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).parse(dateString);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;

    }
}
