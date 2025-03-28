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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.io.*;
import java.net.URI;
import java.util.*;

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

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    @Transactional
    public Mono<Rendering> getProducts(@RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "id") String sort,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(defaultValue = "1") Integer pageNumber,
                                       Model model
    ) {
        //ModelAndView modelAndView = new ModelAndView("main.html");
        String sortingColumn = "id";
        if (sort.equals("ALPHA")) {
            sortingColumn = "name";
        } else if (sort.equals("PRICE")) {
            sortingColumn = "price";
        }
        Sort sortingObject = Sort.by(sortingColumn);

        var pageRequest = PageRequest.of(pageNumber-1, pageSize, sortingObject);
        Flux<ProductToUIDto> productsFlux;
        if ((search != null) && (!search.isEmpty())) {
            productsFlux = productsService.findByNameLike(search, pageRequest);
        } else {
            productsFlux = productsService.findAll(pageRequest, pageRequest.getPageNumber(), pageRequest.getPageSize());
        }
        Mono<Long> countAllMono;
        if ((search != null) && (!search.isEmpty())) {
            countAllMono = productsService.countByNameLike(search);
        } else {
            countAllMono = productsService.countAll();
        }
        return productsFlux.collectList().zipWith(countAllMono).flatMap(tuple -> {
            var productsPage = tuple.getT1();
            var countAll = tuple.getT2();
            HashMap<String, Object> params = new HashMap<>();
            System.out.println(productsPage);
            params.put("products", productsPage);
            params.put("search", search);
            params.put("sort", sort);
            params.put("pageNumber", pageNumber);
            params.put("pageSize", pageSize);
            params.put("lastPageNumber", Math.ceil((double)countAll/pageSize));
            model.addAllAttributes(params);
            return Mono.just(Rendering.view("main").modelAttributes(params).build());
        });
    }

    @PostMapping(value = "/changeCartItemQuantity/{id}", params = "action=plus")
    public Mono<ServerResponse> increaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/products/")).build()));
    }
    @PostMapping(value = "/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantity(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/products")).build()));
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=plus")
    public Mono<ServerResponse> increaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/products/" + itemId.longValue())).build()));
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantityAndShowItem(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/products/"+itemId)).build()));
    }
    @PostMapping(value = "/cart/changeCartItemQuantity/{id}", params = "action=plus")
    public Mono<ServerResponse> increaseCartItemQuantityAndShowCart(@PathVariable(name = "id") Integer itemId) {
        return cartService.addCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/cart")).build()));
    }
    @PostMapping(value = "/cart/changeCartItemQuantity/{id}", params = "action=minus")
    public Mono<ServerResponse> decreaseCartItemQuantityAndShowCart(@PathVariable(name = "id") Integer itemId) {
        return cartService.removeCartItem(itemId.longValue())
                .then(Mono.defer(() -> ServerResponse.temporaryRedirect(URI.create("/cart")).build()));
    }


    @GetMapping("/uploadCSV")
    public Mono<String> uploadCSV() {
        return Mono.just("uploadCSV");
    }

    @PostMapping(
            value = "/uploadCSV",
            consumes = "multipart/form-data" // Обязательно включаем медиа-тип
    )
    public Mono<String> uploadIcon(
            @RequestPart("csv")  Mono<byte[]> file, // Файл в виде массива байт
            Model model) throws IOException, CsvValidationException {
        return file.flatMap((csvFile) -> {
            if (csvFile.length==0) {
                model.addAttribute("message","Файл пуст, импорт не выполнен");
                return Mono.just("uploadCSVStatus");
            }
            try (CSVReader csvReader = new CSVReaderBuilder(
                    new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(csvFile))
                    )
                ).withCSVParser(
                    new CSVParserBuilder().withSeparator(',').build()
                ).build()) {

                ColumnPositionMappingStrategy<ProductToUIDto> beanStrategy = new ColumnPositionMappingStrategy<ProductToUIDto>();
                beanStrategy.setType(ProductToUIDto.class);
                beanStrategy.setColumnMapping(new String[] {"name","description","price","imageBase64"});

                String[] header = csvReader.readNext();
                if (!Arrays.equals(header, beanStrategy.getColumnMapping())) {
                    model.addAttribute("message","Ошибка импорта - некорректный заголовок");
                    return Mono.just("uploadCSVStatus");
                }

                CsvToBean<ProductToUIDto> csvToBean = new CsvToBean<ProductToUIDto>();
                csvToBean.setMappingStrategy(beanStrategy);
                csvToBean.setCsvReader(csvReader);
                List<ProductToUIDto> products = csvToBean.parse();
                return Flux.fromIterable(products).map(productsService::save).collectList().flatMap(list -> {
                    model.addAttribute("message", "Импорт завершен успешно, продуктов импортировано: " + list.size());
                    return Mono.just("uploadCSVStatus");
                });
            } catch (IOException | CsvValidationException e) {
                return Mono.error(new RuntimeException(e));
            }
        });
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
