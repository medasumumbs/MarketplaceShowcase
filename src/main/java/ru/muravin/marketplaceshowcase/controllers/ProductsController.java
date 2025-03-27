package ru.muravin.marketplaceshowcase.controllers;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
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
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public Mono<ServerResponse> increaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue()).flatMap(unused -> {
            return ServerResponse.temporaryRedirect(URI.create("/products/")).build();
        });
    }
    @PostMapping(value = "/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue()).flatMap(unused -> {
            return ServerResponse.temporaryRedirect(URI.create("/products")).build();
        });
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=plus")
    public Mono<ServerResponse> increaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue()).flatMap(unused -> {
            return ServerResponse.temporaryRedirect(URI.create("/products/" + itemId.longValue())).build();
        });
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue()).flatMap(nullPointer -> {
            return ServerResponse.temporaryRedirect(URI.create("/products/"+itemId)).build();
        });
    }
    @PostMapping(value = "/cart/changeCartItemQuantity/{id}", params = "action=plus")
    public Mono<ServerResponse> increaseCartItemQuantityAndShowCart(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue()).flatMap(nullPointer -> {
            return ServerResponse.temporaryRedirect(URI.create("/cart")).build();
        });
    }
    @PostMapping(value = "/cart/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantityAndShowCart(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue()).flatMap(nullPointer -> {
            return ServerResponse.temporaryRedirect(URI.create("/cart")).build();
        });
    }


    @GetMapping("/uploadCSV")
    public Mono<ServerResponse> uploadCSV() {
        return ServerResponse.ok().render("uploadCSV");
    }

    @PostMapping(
            value = "/uploadCSV",
            consumes = "multipart/form-data" // Обязательно включаем медиа-тип
    )
    public Mono<ServerResponse> uploadIcon(
            @RequestPart("csv") MultipartFile csvFile, // Файл в виде массива байт
            Model model) throws IOException, CsvValidationException {
        if (csvFile.isEmpty()) {
            return ServerResponse.badRequest().render("uploadCSVStatus", Map.of("message","Файл пуст, импорт не выполнен"));
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
                return ServerResponse.badRequest()
                        .render("uploadCSVStatus", Map.of("message","Ошибка импорта - некорректный заголовок"));

            }

            CsvToBean<ProductToUIDto> csvToBean = new CsvToBean<ProductToUIDto>();
            csvToBean.setMappingStrategy(beanStrategy);
            csvToBean.setCsvReader(csvReader);
            List<ProductToUIDto> products = csvToBean.parse();
            return Flux.fromIterable(products).map(productsService::save).collectList().flatMap(list -> {
                return ServerResponse.ok()
                    .render(
                            "uploadCSVStatus",
                            Map.of("message", "Импорт завершен успешно, продуктов импортировано: " + list.size())
                    );
            });
        }
    }

    @GetMapping("/{id}")
    public Mono<ServerResponse> itemPage(Model model, @PathVariable(name = "id") Long id) {
        return productsService.findById(id)
                .flatMap(productToUIDto -> ServerResponse.ok().render("item", Map.of("product",productToUIDto)));
    }

    @ExceptionHandler({IOException.class, CsvValidationException.class})
    public Mono<ServerResponse> CSVErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        return ServerResponse.status(500)
                .render("errorPage", "Ошибка импорта - проверьте файл на корректность: " + exception.getCause());
    }
    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> unknownErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        return ServerResponse.status(500).render("errorPage", Map.of("message","Неизвестная ошибка: " + exception.getMessage()));
    }

}
