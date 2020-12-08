package com.atv.cripto.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class CriptoService {

    Logger logger = LoggerFactory.getLogger(CriptoService.class);

    private final WebClient webClient;

    @Autowired
    public CriptoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.exchangeratesapi.io").build();
    }

    private Mono<Map> callExchangeRestApi(String date) {
        return this.webClient.get().uri("/" + date).retrieve().bodyToMono(Map.class);
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
            Workbook workbook = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() != 0) {
                    String dateRow = row.getCell(0).getStringCellValue();
                    Double costDollarsRow = row.getCell(1).getNumericCellValue();
                    Double costEurosRow = Objects.nonNull(row.getCell(2)) ? row.getCell(2).getNumericCellValue() : null;

                    if (Objects.isNull(costEurosRow)) {
                        row.createCell(2).setCellValue(this.calculateEurosColumn(costDollarsRow, dateRow));
                    } else if (costEurosRow.equals(Double.valueOf("0.0"))) {
                        row.getCell(2).setCellValue(this.calculateEurosColumn(costDollarsRow, dateRow));
                    }
                }
            }
            File editedFile = new File(path.toString());
            FileOutputStream outputStream = new FileOutputStream(editedFile);
            workbook.write(outputStream);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private Double calculateEurosColumn(Double costDollarsRow, String dateRow) {
        String date = this.formatDate(dateRow);
        Mono<Map> apiResult = this.callExchangeRestApi(date);
        Double USDPrice = (Double) ((Map) apiResult.block().get("rates")).get("USD");
        return this.calculateEurosFromUSD(costDollarsRow, USDPrice);
    }

    private Double calculateEurosFromUSD(Double costDollars, Double usdPrice) {
        return costDollars / usdPrice;
    }

    private String formatDate(String dateString) {
        try {
            Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).parse(dateString);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;

    }
}
