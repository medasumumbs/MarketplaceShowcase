package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.ProductsReactiveRepository;

import java.util.HashMap;
import java.util.List;

@Service
public class ProductsService {
    private final CartService cartService;

    private final ProductsReactiveRepository productsReactiveRepository;

    @Autowired
    public ProductsService(ProductsReactiveRepository productsReactiveRepository, CartService cartService) {
        this.productsReactiveRepository = productsReactiveRepository;
        this.cartService = cartService;
    }
    public Flux<ProductToUIDto> findAll(PageRequest pageRequest, int pageNumber, int pageSize, String sort) {
        int offset = pageNumber * pageSize;
        Flux<Product> productFlux;
        if (sort == null || sort.isEmpty()) {
            productFlux = productsReactiveRepository.findAll(pageSize, offset);
        } else {
            if (sort.equalsIgnoreCase("name")) {
                productFlux = productsReactiveRepository.findAllSortByName(pageSize, offset);
            } else if (sort.equalsIgnoreCase("price")) {
                productFlux = productsReactiveRepository.findAllSortByPrice(pageSize, offset);
            } else {
                productFlux = productsReactiveRepository.findAll(pageSize, offset);
            }
        }

        return getProductToUIDtoFlux(productFlux);
    }

    private Flux<ProductToUIDto> getProductToUIDtoFlux(Flux<Product> productFlux) {
        var products = productFlux.map(ProductToUIDto::new).collectList();
        var cartItems = cartService.getCartItemsFlux(cartService.getFirstCartIdMono()).collectList();
        return Mono.zip(products, cartItems).flatMapMany(tuple -> {
            enrichDtoListWithCartQuantities(tuple.getT1(), tuple.getT2());
            return Flux.fromIterable(tuple.getT1());
        });
    }

    public Mono<Void> save(ProductToUIDto productToUIDto) {
        return productsReactiveRepository.save(productToUIDto.transformToProduct()).doOnError(e->System.out.println(e)).then();
    }
    public Mono<Long> countAll() {
        return productsReactiveRepository.count();
    }

    public Flux<ProductToUIDto> findByNameLike(String search, PageRequest pageRequest, String sort) {
        Flux<Product> productFlux;
        var limit = pageRequest.getPageSize();
        var offset = pageRequest.getPageSize() * pageRequest.getPageNumber();
        if (sort != null && !sort.isEmpty()) {
            if (sort.equalsIgnoreCase("name")) {
                productFlux = productsReactiveRepository.findByNameLikeSortByName(search, limit, offset);
            } else if (sort.equalsIgnoreCase("price")) {
                productFlux = productsReactiveRepository.findByNameLikeSortByPrice(search, limit, offset);
            } else {
                productFlux = productsReactiveRepository.findByNameLike(search, limit, offset);
            }
        } else {
            productFlux = productsReactiveRepository.findByNameLike(search, limit, offset);
        }
        return getProductToUIDtoFlux(productFlux);
    }
    public Mono<Long> countByNameLike(String search) {
        return productsReactiveRepository.countByNameContainingIgnoreCase(search);
    }

    public Mono<ProductToUIDto> findById(Long id) {
        // Поток для продукта
        var productMono = productsReactiveRepository.findById(id)
                .map(ProductToUIDto::new) // Преобразуем Product в ProductToUIDto
                .switchIfEmpty(Mono.error(() -> new UnknownProductException("Product " + id + " not found")));

        // Поток для элемента корзины
        var cartItemMono = cartService.getFirstCartIdMono()
                .flatMap(cartId -> cartService.getCartItemMono(cartId, id)).defaultIfEmpty(new CartItem());

        // Объединяем потоки
        return Mono.zip(productMono, cartItemMono)
                .flatMap(tuple -> {
                    var productToUIDto = tuple.getT1();
                    var cartItem = tuple.getT2();

                    // Обогащаем DTO данными из корзины
                    enrichDtoListWithCartQuantities(List.of(productToUIDto), List.of(cartItem));

                    System.out.println("productToUIDto: " + productToUIDto);

                    return Mono.just(productToUIDto);
                });
    }

    private void enrichDtoListWithCartQuantities(List<ProductToUIDto> dtoList, List<CartItem> cartItems) {
        var productsMap = new HashMap<>();
        dtoList.forEach(productToUIDto -> {
            productsMap.put(productToUIDto.getId(), productToUIDto);
        });
        dtoList.forEach(productToUIDto -> {
            productToUIDto.setQuantityInCart(0);
        });
        cartItems.forEach(cartItem -> {
            if (productsMap.containsKey(cartItem.getProductId())) {
                ((ProductToUIDto)productsMap.get(cartItem.getProductId())).setQuantityInCart(cartItem.getQuantity());
            }
        });
    }

    public Mono<Void> saveAll(List<ProductToUIDto> products) {
        var productsEntities = products.stream().map((dto)->dto.transformToProduct()).toList();
        return productsReactiveRepository.saveAll(productsEntities).then();
    }
}
