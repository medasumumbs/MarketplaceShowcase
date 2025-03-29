package ru.muravin.marketplaceshowcase.services;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductsCSVService {

    private final ProductsService productsService;

    private int productsSize;
    @Autowired
    public ProductsCSVService(ProductsService productsService) {
        this.productsService = productsService;
    }

    public Mono<String> uploadCSV(byte[] csvFile) {
        return Mono.using(
                () -> new CSVReaderBuilder(
                        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvFile))))
                        .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                        .build(),
                csvReader -> {
                    try {
                        ColumnPositionMappingStrategy<ProductToUIDto> beanStrategy = new ColumnPositionMappingStrategy<>();
                        beanStrategy.setType(ProductToUIDto.class);
                        beanStrategy.setColumnMapping(new String[]{"name", "description", "price", "imageBase64"});

                        String[] header = csvReader.readNext();
                        if (!Arrays.equals(header, beanStrategy.getColumnMapping())) {
                            return Mono.just("Ошибка импорта - некорректный заголовок");
                        }

                        CsvToBean<ProductToUIDto> csvToBean = new CsvToBean<>();
                        csvToBean.setMappingStrategy(beanStrategy);
                        csvToBean.setCsvReader(csvReader);

                        List<ProductToUIDto> products = csvToBean.parse();
                        productsSize = products.size();
                        return productsService.saveAll(products).thenReturn("Импорт завершен успешно, продуктов импортировано: " + productsSize);
                    } catch (IOException | CsvValidationException e) {
                        e.printStackTrace();
                        return Mono.error(e); // Передаем ошибку дальше
                    }
                },
                csvReader -> {
                    try {
                        csvReader.close(); // Закрываем ресурс
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Ошибка при закрытии CSVReader", e);
                    }
                }
        ).map(string -> string);
    }
}
