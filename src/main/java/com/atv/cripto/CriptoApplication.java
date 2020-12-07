package com.atv.cripto;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
public class CriptoApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(CriptoApplication.class, args);
		readExcelFile();
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
