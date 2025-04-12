package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.*;

@Service
public class CartService {

    private final ProductsReactiveRepository productsReactiveRepository;
    private final CartsReactiveRepository cartsReactiveRepository;
    private final CartItemsReactiveRepository cartItemsReactiveRepository;
    private final RedisCacheService redisCacheService;

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
            return ((User)securityContext.getAuthentication().getPrincipal()).getId();
        }).defaultIfEmpty(0L);
    }
    @Autowired
    public CartService(CartsReactiveRepository cartsReactiveRepository,
                       CartItemsReactiveRepository cartItemsReactiveRepository,
                       ProductsReactiveRepository productsReactiveRepository,
                       RedisCacheService redisCacheService) {
        this.cartsReactiveRepository = cartsReactiveRepository;
        this.cartItemsReactiveRepository = cartItemsReactiveRepository;
        this.productsReactiveRepository = productsReactiveRepository;
        this.redisCacheService = redisCacheService;
    }

    public Mono<Void> addCartItem(Long productId) {
        return getCurrentUserId().map(id->{
            redisCacheService.evictCartCache(Math.toIntExact(id)).subscribe();
            return id;
        }).flatMap(cartId -> {
            var productMono = productsReactiveRepository.findById(productId);
            productMono = productMono.switchIfEmpty(Mono.error(() -> new UnknownProductException("Product "+productId+" not found")));
            return productMono.flatMap(
                    product -> {
                        return cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(), cartId)
                                .flatMap(cartItem -> {
                                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                                    return cartItemsReactiveRepository.save(cartItem);
                                })
                                .switchIfEmpty(cartItemsReactiveRepository.save(new CartItem(productId, cartId)));
                    }
            ).then();
        });
    }

    public Mono<Void> removeCartItem(Long productId) {
        return getCurrentUserId().map(
                cartId-> {
                    redisCacheService.evictCartCache(Math.toIntExact(cartId));
                    return cartId;
                }).flatMap(cartId -> {
            var productMono = productsReactiveRepository.findById(productId);
            productMono = productMono.switchIfEmpty(Mono.error(() -> new UnknownProductException("Product "+productId+" not found")));
            return productMono.flatMap(
                    product -> {
                        return cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(), Long.valueOf(cartId));
                    }
            ).flatMap(cartItem -> {
                if (cartItem == null) return Mono.fromCallable(()->null);
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                if (cartItem.getQuantity() == 0) {
                    return cartItemsReactiveRepository.delete(cartItem);
                } else {
                    return cartItemsReactiveRepository.save(cartItem);
                }
            }).then();
        });
    }

  //  @PreAuthorize("#id = @cartService.getCurrentUserId()")
    public Mono<Cart> getCartById(long id) {
        return cartsReactiveRepository.findById(id);
    }

  // @PreAuthorize("#cart.id = @cartService.getCurrentUserId()")
    public Mono<Void> deleteAllItemsByCart(Cart cart) {
        return cartItemsReactiveRepository.deleteByCartId(cart.getId());
    }

    public Mono<Long> getFirstCartIdMono() {
        return cartsReactiveRepository.findAll().elementAt(0).map(Cart::getId);
    }

   // @PreAuthorize("#cart.id = @cartService.getCurrentUserId()")
    public Flux<CartItem> getCartItemsFlux(Cart cart) {
        return getCartItemsFlux(Mono.just(cart.getId()));
    }

  //  @PreAuthorize("#cartId.block() = @cartService.getCurrentUserId()")
    public Flux<CartItem> getCartItemsFlux(Mono<Long> cartId) {
        return cartId.flatMapMany(cartItemsReactiveRepository::findAllByCart_Id).map(CartItemToUIDto::toCartItem);
    }
   // @PreAuthorize("#cartId = @cartService.getCurrentUserId()")
    public Mono<CartItem> getCartItemMono(Long cartId, Long productId) {
        return cartItemsReactiveRepository.findByProductIdAndCartId(productId, cartId);
    }

   // @PreAuthorize("#cartId.block() = @cartService.getCurrentUserId()")
    public Flux<CartItemToUIDto> getCartItemsDtoFlux(Mono<Long> cartId) {
        return redisCacheService.getCartItemsCache(String.valueOf(cartId.subscribe())).switchIfEmpty(
            getCartItemsDtoFluxFromRepo(cartId).collectList().publishOn(Schedulers.boundedElastic()).flatMap(a -> {
                redisCacheService.setCartItemsCache(String.valueOf(cartId), a).subscribe();
                return Mono.just(a);
            }).flatMapMany(Flux::fromIterable)
        );
    }

   // @PreAuthorize("#cartId.block() = @cartService.getCurrentUserId()")
    public Flux<CartItemToUIDto> getCartItemsDtoFluxFromRepo(Mono<Long> cartId) {
        return getCartItemsFlux(cartId).flatMap(cartItem -> {
            return productsReactiveRepository.findById(cartItem.getProductId()).zipWith(Mono.just(cartItem));
        }).map(tuple-> {
            var product = tuple.getT1();
            var cartItem = tuple.getT2();
            return new CartItemToUIDto(cartItem, new ProductToUIDto(product));
        });
    }
   // @PreAuthorize("#cartId.block() = @cartService.getCurrentUserId()")
    public Mono<Double> getCartSumMono(Mono<Long> cartId) {
        Double sum = (double) 0;
        return getCartItemsDtoFlux(cartId).reduce(
                sum,
                (accumulator, item) -> accumulator + item.getQuantity()*item.getProduct().getPrice()
        );
    }
}
