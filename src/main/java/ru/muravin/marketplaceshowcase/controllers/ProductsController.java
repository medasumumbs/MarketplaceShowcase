package ru.muravin.marketplaceshowcase.controllers;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.ProductsCSVService;
import ru.muravin.marketplaceshowcase.services.ProductsService;
import ru.muravin.marketplaceshowcase.services.RedisCacheService;

import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductsController {
    private final ProductsService productsService;
    private final CartService cartService;
    private final ProductsCSVService productsCSVService;
    private final RedisCacheService redisCacheService;

    @Autowired
    public ProductsController(ProductsService productsService, CartService cartService, ProductsCSVService productsCSVService, RedisCacheService redisCacheService){
        this.productsService = productsService;
        this.cartService = cartService;
        this.productsCSVService = productsCSVService;
        this.redisCacheService = redisCacheService;
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
            productsFlux = productsService.findByNameLike(search, pageRequest, sortingColumn);
        } else {
            productsFlux = productsService.findAll(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortingColumn);
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
            System.out.println("lastPageNumber=" + params.get("lastPageNumber") + " pageNumber=" + params.get("pageNumber") + " pageSize=" + params.get("pageSize") + "countAll=" + countAll);
            model.addAllAttributes(params);
            return Mono.just(Rendering.view("main").modelAttributes(params).build());
        });
    }

    @PostMapping(value = "/changeCartItemQuantity/{id}")
    public Mono<Rendering> changeCartItemQuantity(@PathVariable(name = "id") Integer itemId, ServerWebExchange exchange) {
        redisCacheService.evictCache().subscribe();
        return exchange.getFormData().flatMap(data->{
            if (Objects.equals(data.getFirst("action"), "plus")) {
                return cartService.addCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/products").build()));
            } else {
                return cartService.removeCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/products").build()));
            }
        });
    }
    @PostMapping(value = "/{id1}/changeCartItemQuantity/{id}")
    public Mono<Rendering> changeCartItemQuantityAndShowItem(
            @PathVariable(name = "id") Integer itemId,
            ServerWebExchange exchange) {
        redisCacheService.evictCache().subscribe();
        return exchange.getFormData().flatMap(data->{
            if (Objects.equals(data.getFirst("action"), "plus")) {
                return cartService.addCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/products/"+itemId).build()));
            } else {
                return cartService.removeCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/products/"+itemId).build()));
            }
        });
    }
    @PostMapping(value = "/cart/changeCartItemQuantity/{id}")
    public Mono<Rendering> changeCartItemQuantityAndShowCart(@PathVariable(name = "id") Integer itemId, ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(data->{
            if (Objects.equals(data.getFirst("action"), "plus")) {
                redisCacheService.evictCache().subscribe();
                return cartService.addCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/cart").build()));
            } else {
                redisCacheService.evictCache().subscribe();
                return cartService.removeCartItem(itemId.longValue())
                        .then(Mono.just(Rendering.redirectTo("/cart").build()));
            }
        });
    }


    @GetMapping("/uploadCSV")
    public Mono<String> uploadCSV() {
        return Mono.just("uploadCSV");
    }

    @PostMapping(
            value = "/uploadCSV",
            consumes = "multipart/form-data"
    )
    public Mono<Rendering> uploadCSV(@RequestPart("csv") Mono<byte[]> file) {
        return file.flatMap(csvFile -> {
            if (csvFile.length == 0) {
                // Если файл пустой, возвращаем сообщение об ошибке
                return Mono.just("Файл пуст, импорт не выполнен");
            }
            return productsCSVService.uploadCSV(csvFile);
        }).flatMap(message -> Mono.just(Rendering.view("uploadCSVStatus").modelAttribute("message",message).build()));
    }

    @GetMapping("/{id}")
    public Mono<Rendering> itemPage(@PathVariable(name = "id") Long id) {
        return productsService.findById(id)
                .flatMap(productToUIDto ->
                        Mono.just(Rendering.view("item").modelAttribute("product",productToUIDto).build()));
    }

    @ExceptionHandler({IOException.class, CsvValidationException.class})
    public Mono<Rendering> CSVErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        return Mono.just(Rendering.view("errorPage").modelAttribute("message", "Ошибка импорта - проверьте файл на корректность: " + exception.getCause()).build());
    }
    @ExceptionHandler(Exception.class)
    public Mono<Rendering> unknownErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        return Mono.just(Rendering.view("errorPage").modelAttribute("message", "Неизвестная ошибка: " + exception.getMessage()).build());
    }

}
