package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.CartItem;
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
    public Flux<ProductToUIDto> findAll(PageRequest pageRequest, int pageSize, int offset) {
        var products = productsReactiveRepository.findAll(pageRequest, pageSize, offset).map(ProductToUIDto::new).collectList();
        var cartItems = cartService.getCartItemsFlux(cartService.getFirstCartIdMono()).collectList();
        return Mono.zip(products, cartItems).flatMapMany(tuple -> {
            enrichDtoListWithCartQuantities(tuple.getT1(), tuple.getT2());
            return Flux.fromIterable(tuple.getT1());
        });
    }
    public Mono<Void> save(ProductToUIDto productToUIDto) {
        return productsReactiveRepository.save(productToUIDto.transformToProduct()).then();
    }
    public Mono<Long> countAll() {
        return productsReactiveRepository.count();
    }

    public Flux<ProductToUIDto> findByNameLike(String search, PageRequest pageRequest) {
        var products = productsReactiveRepository
                .findByNameLike('%' + search + '%', pageRequest.getPageNumber(), pageRequest.getPageSize())
                .map(ProductToUIDto::new).collectList();
        var cartItems = cartService.getCartItemsFlux(cartService.getFirstCartIdMono()).collectList();
        return Mono.zip(products, cartItems).flatMapMany(tuple -> {
            enrichDtoListWithCartQuantities(tuple.getT1(), tuple.getT2());
            return Flux.fromIterable(tuple.getT1());
        });
    }
    public Mono<Long> countByNameLike(String search) {
        return productsReactiveRepository.countByNameLike('%' + search + '%');
    }

    public Mono<ProductToUIDto> findById(Long id) {
        var cartItem = cartService.getCartItemFlux(cartService.getFirstCartIdMono().block(), id);
        var product = productsReactiveRepository.findById(id).map(ProductToUIDto::new)
                .switchIfEmpty(Mono.error(()->new UnknownProductException("Product "+id+" not found")));
        return Mono.zip(cartItem, product).map(tuple -> {
            var cartItemValue = tuple.getT1();
            var productValue = tuple.getT2();
            enrichDtoListWithCartQuantities(List.of(productValue), List.of(cartItemValue));
            return productValue;
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
}
