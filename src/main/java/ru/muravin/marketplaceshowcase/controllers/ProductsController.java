package ru.muravin.marketplaceshowcase.controllers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {
    private final ProductsService productsService;

    @Autowired
    public ProductsController(ProductsService productsService){
        this.productsService = productsService;
    }

    @GetMapping
    @Transactional
    public ModelAndView getProducts(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "1") Integer pageNumber
    ) {
        ModelAndView modelAndView = new ModelAndView("main.html");
        modelAndView.addObject("products", productsService.findAll(PageRequest.of(pageNumber-1, pageSize)));
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("pageSize", pageSize);
        modelAndView.addObject("pageNumber", pageNumber);
        modelAndView.addObject("lastPageNumber", Math.ceil((double)productsService.countAll()/pageSize));

        return modelAndView;
    }



    @GetMapping("/uploadCSV")
    public String uploadCSV() {
        return "uploadCSV";
    }

    @PostMapping(
            value = "/uploadCSV",
            consumes = "multipart/form-data" // Обязательно включаем медиа-тип
    )
    public String uploadIcon(
            @RequestPart("csv") MultipartFile csvFile, // Файл в виде массива байт
            Model model) throws IOException, CsvValidationException {
        if (csvFile.isEmpty()) {
            model.addAttribute("message", "Файл пуст, импорт не выполнен");
            return "uploadCSVStatus";
        }
        try (CSVReader csvReader = new CSVReaderBuilder(
                new BufferedReader(
                    new InputStreamReader(csvFile.getInputStream())
                )
            ).withCSVParser(
                new CSVParserBuilder().withSeparator(',').build()
            ).build()) {

            ColumnPositionMappingStrategy<Product> beanStrategy = new ColumnPositionMappingStrategy<Product>();
            beanStrategy.setType(Product.class);
            beanStrategy.setColumnMapping(new String[] {"id","name","description","price","imageBase64"});

            String[] header = csvReader.readNext();
            System.out.println(header[0]);

            CsvToBean<Product> csvToBean = new CsvToBean<Product>();
            csvToBean.setMappingStrategy(beanStrategy);
            csvToBean.setCsvReader(csvReader);
            List<Product> products = csvToBean.parse();
            products.forEach(product -> {
                System.out.println(product.toString());
            });

            // save users list on model
            model.addAttribute("message", "");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return "uploadCSVStatus";
    }
    @ExceptionHandler(Exception.class)
    public String errorPage(Model model, Exception exception) {
        model.addAttribute("message", "Ошибка импорта - проверьте файл на корректность: " + exception.getCause());
        return "errorPage";
    }

}
