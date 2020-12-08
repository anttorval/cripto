package com.atv.cripto.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        this.webClient = webClientBuilder.baseUrl("").build();
    }

    private Mono<Map> callExchangeRestApi(String date) {
        return this.webClient.get().uri("https://api.exchangeratesapi.io/" + date).retrieve().bodyToMono(Map.class);
    }

    public Flux<Map> processFiles() throws IOException {
        this.readExcelFile();

        return Flux.empty();
    }

    private void readExcelFile() throws IOException {
        String pathName = "C:\\Users\\antonio\\Desktop\\workspace\\cripto\\src\\main\\java\\resources\\files\\kuailian";

        try (Stream<Path> paths = Files.walk(Paths.get(pathName))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> processFile(path));
        }
    }

    private void processFile(Path path) {
        Workbook workbook = null;
        try {
            try {
                workbook = new HSSFWorkbook(new FileInputStream(path.toFile()));
            } catch (Exception e) {
                workbook = new XSSFWorkbook(new FileInputStream(path.toFile()));
            }
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() != 0) {
                    String dateRow = "";
                    try{
                        if(CellType.STRING.equals(row.getCell(0).getCellType())){
                            dateRow = row.getCell(0).getStringCellValue();
                        }else{
                            dateRow = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(row.getCell(0).getDateCellValue());
                        }
                    }catch (Exception e){
                        logger.error(path.getFileName().toString() + " : " + e.getMessage());
                        continue;
                    }

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
            logger.error(path.getFileName().toString() + " : " + e.getMessage());
        } finally {
            try {
                if (Objects.nonNull(workbook)) {
                    workbook.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
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
        Date date = null;
        try{
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dateString);
        }catch (ParseException e){
            try {
                date = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).parse(dateString);
            } catch (ParseException e0) {
                try {
                    date = new SimpleDateFormat("MMM. dd, yyyy", Locale.ENGLISH).parse(dateString);
                } catch (ParseException e1) {
                    try {
                        date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(dateString);
                    } catch (ParseException e2) {
                        try {
                            date = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH).parse(dateString);
                        } catch (ParseException e3) {
                            try {
                                date = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH).parse(dateString);
                            } catch (ParseException e4) {
                                try {
                                    date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(dateString);
                                } catch (ParseException e5) {
                                    logger.error(e5.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }

        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
    }
}
