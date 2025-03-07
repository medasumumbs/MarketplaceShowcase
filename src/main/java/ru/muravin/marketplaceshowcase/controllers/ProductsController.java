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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {
    private final ProductsService productsService;
    private final CartService cartService;

    @Autowired
    public ProductsController(ProductsService productsService, CartService cartService){
        this.productsService = productsService;
        this.cartService = cartService;
    }

    @GetMapping
    @Transactional
    public ModelAndView getProducts(@RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "id") String sort,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "1") Integer pageNumber
    ) {
        ModelAndView modelAndView = new ModelAndView("main.html");
        String sortingColumn = "id";
        if (sort.equals("ALPHA")) {
            sortingColumn = "name";
        } else if (sort.equals("PRICE")) {
            sortingColumn = "price";
        }
        Sort sortingObject = Sort.by(sortingColumn);
        List<ProductToUIDto> products;

        var pageRequest = PageRequest.of(pageNumber-1, pageSize, sortingObject);
        if ((search != null) && (!search.isEmpty())) {
            products = productsService.findByNameLike(search, pageRequest);
        } else {
            products = productsService.findAll(pageRequest);
        }

        modelAndView.addObject("products", products);
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("pageSize", pageSize);
        modelAndView.addObject("pageNumber", pageNumber);
        Long countAll;
        if ((search != null) && (!search.isEmpty())) {
            countAll = productsService.countByNameLike(search);
        } else {
            countAll = productsService.countAll();
        }
        modelAndView.addObject("lastPageNumber", Math.ceil((double)countAll/pageSize));

        return modelAndView;
    }

    @PostMapping(value = "/changeCartItemQuantity/{id}", params = "action=plus")
    public String increaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        cartService.addCartItem(itemId.longValue());
        return "redirect:/products";
    }
    @PostMapping(value = "/changeCartItemQuantity/{id}", params = "action=minus")
    public String decreaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        cartService.removeCartItem(itemId.longValue());
        return "redirect:/products";
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=plus")
    public String increaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        cartService.addCartItem(itemId.longValue());
        return "redirect:/products/"+itemId;
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=minus")
    public String decreaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        cartService.removeCartItem(itemId.longValue());
        return "redirect:/products/"+itemId;
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

            ColumnPositionMappingStrategy<ProductToUIDto> beanStrategy = new ColumnPositionMappingStrategy<ProductToUIDto>();
            beanStrategy.setType(ProductToUIDto.class);
            beanStrategy.setColumnMapping(new String[] {"name","description","price","imageBase64"});

            String[] header = csvReader.readNext();
            if (!Arrays.equals(header, beanStrategy.getColumnMapping())) {
                model.addAttribute("message", "Ошибка импорта - некорректный заголовок");
                return "uploadCSVStatus";
            }

            CsvToBean<ProductToUIDto> csvToBean = new CsvToBean<ProductToUIDto>();
            csvToBean.setMappingStrategy(beanStrategy);
            csvToBean.setCsvReader(csvReader);
            List<ProductToUIDto> products = csvToBean.parse();
            products.forEach(productsService::save);

            // save users list on model
            model.addAttribute(
                    "message", "Импорт завершен успешно, продуктов импортировано: " + products.size()
            );
        }
        return "uploadCSVStatus";
    }

    @GetMapping("/{id}")
    public String itemPage(Model model, @PathVariable(name = "id") Long id) {
        model.addAttribute("product", productsService.findById(id));
        return "item";
    }

    @ExceptionHandler({IOException.class, CsvValidationException.class})
    public String CSVErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        model.addAttribute("message", "Ошибка импорта - проверьте файл на корректность: " + exception.getCause());
        return "errorPage";
    }
    @ExceptionHandler(Exception.class)
    public String unknownErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        model.addAttribute("message", "Неизвестная ошибка: " + exception.getMessage());
        return "errorPage";
    }

}
