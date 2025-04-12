package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.muravin.marketplaceshowcase.dto.ProductCacheDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.ProductsReactiveRepository;

import java.util.HashMap;
import java.util.List;

@Service
public class ProductsService {
    private final CartService cartService;

    private final ProductsReactiveRepository productsReactiveRepository;
    private final RedisCacheService redisCacheService;

    @Autowired
    public ProductsService(ProductsReactiveRepository productsReactiveRepository, CartService cartService, RedisCacheService redisCacheService) {
        this.productsReactiveRepository = productsReactiveRepository;
        this.cartService = cartService;
        this.redisCacheService = redisCacheService;
    }

    public Flux<ProductToUIDto> findAll(int pageNumber, int pageSize, String sort) {
        int offset = pageNumber * pageSize;
        return redisCacheService.getProductsListCache(null, sort, pageSize, offset)
                .switchIfEmpty(findAllWithCache(offset,pageSize,sort));
    }

    private Flux<ProductToUIDto> findAllWithCache(int offset, int pageSize, String sort) {
        return findAllFromRepo(offset, pageSize, sort).collectList().publishOn(Schedulers.boundedElastic()).doOnSuccess(
            list -> {
               redisCacheService.setProductsListCache(null,sort,pageSize,offset,list).subscribe();
            }
        ).flatMapMany(Flux::fromIterable);
    }
    private Flux<ProductToUIDto> findAllFromRepo( int offset, int pageSize, String sort) {
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

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> save(ProductToUIDto productToUIDto) {
        return productsReactiveRepository.save(productToUIDto.transformToProduct()).doOnError(System.out::println).then();
    }
    public Mono<Long> countAll() {
        return redisCacheService.getProductsCount(null).switchIfEmpty(
            productsReactiveRepository.count().flatMap(count -> {
                redisCacheService.setProductsCount(null, count).subscribe();
                return Mono.just(count);
            }));
    }
    public Flux<ProductToUIDto> findByNameLike(String search, PageRequest pageRequest, String sort) {
        int offset = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int limit = pageRequest.getPageSize();
        return redisCacheService.getProductsListCache(search, sort, limit, offset)
                .switchIfEmpty(findByNameLikeWithCache(search,limit,offset,sort));
    }

    private Flux<ProductToUIDto> findByNameLikeWithCache(String search, int limit, int offset, String sort) {
        return findByNameLikeFromRepo(search, limit, offset, sort).collectList().publishOn(Schedulers.boundedElastic()).doOnSuccess(
                list -> {
                    redisCacheService.setProductsListCache(search,sort,limit,offset,list).subscribe();
                }
        ).flatMapMany(Flux::fromIterable);
    }
    public Flux<ProductToUIDto> findByNameLikeFromRepo(String search, int limit, int offset, String sort) {
        Flux<Product> productFlux;
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
        return redisCacheService.getProductsCount(search).switchIfEmpty(
            productsReactiveRepository.countByNameContainingIgnoreCase(search).flatMap(count -> {
                redisCacheService.setProductsCount(search, count).subscribe();
                return Mono.just(count);
            })
        );
    }

    public Mono<ProductToUIDto> findById(Long id) {
        return redisCacheService.getProductCache(id).switchIfEmpty(
            findByIdFromRepo(id).flatMap(product -> {
                redisCacheService.setProductCache(product).subscribe();
                return Mono.just(product);
            })
        );
    }

    private Mono<ProductToUIDto> findByIdFromRepo(Long id) {
        // Поток для продукта
        var productMono = productsReactiveRepository.findById(id)
                .map(ProductToUIDto::new) // Преобразуем Product в ProductToUIDto
                .switchIfEmpty(Mono.error(() -> new UnknownProductException("Product " + id + " not found")));

        var cartItemMono = getCurrentUserId()
                .flatMap(cartId -> {
                    return cartService.getCartItemMono(cartId, id);
                }).defaultIfEmpty(new CartItem());

        // Объединяем потоки
        return Mono.zip(productMono, cartItemMono)
                .flatMap(tuple -> {
                    var productToUIDto = tuple.getT1();
                    var cartItem = tuple.getT2();
                    // Обогащаем DTO данными из корзины
                    enrichDtoListWithCartQuantities(List.of(productToUIDto), List.of(cartItem));
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

    private Flux<ProductToUIDto> getProductToUIDtoFlux(Flux<Product> productFlux) {
        var products = productFlux.map(ProductToUIDto::new).collectList();
        var cartItems = cartService.getCartItemsFlux(getCurrentUserId()).collectList();
        return Mono.zip(products, cartItems).flatMapMany(tuple -> {
            enrichDtoListWithCartQuantities(tuple.getT1(), tuple.getT2());
            return Flux.fromIterable(tuple.getT1());
        });
    }

    public Mono<Void> saveAll(List<ProductToUIDto> products) {
        var productsEntities = products.stream().map(ProductToUIDto::transformToProduct).toList();
        return productsReactiveRepository.saveAll(productsEntities).then();
    }

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
            return ((User)securityContext.getAuthentication().getPrincipal()).getId();
        }).defaultIfEmpty(0L);
    }
}
