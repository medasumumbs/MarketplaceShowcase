package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.repositories.*;

@Service
public class CartService {

    private final ProductsReactiveRepository productsReactiveRepository;
    private final CartsReactiveRepository cartsReactiveRepository;
    private final CartItemsReactiveRepository cartItemsReactiveRepository;


    @Autowired
    public CartService(CartsReactiveRepository cartsReactiveRepository,
                       CartItemsReactiveRepository cartItemsReactiveRepository,
                       ProductsReactiveRepository productsReactiveRepository) {
        this.cartsReactiveRepository = cartsReactiveRepository;
        this.cartItemsReactiveRepository = cartItemsReactiveRepository;
        this.productsReactiveRepository = productsReactiveRepository;
    }

    public Mono<Void> addCartItem(Long productId) {
        var productMono = productsReactiveRepository.findById(productId);
        productMono = productMono.switchIfEmpty(Mono.error(() -> new UnknownProductException("Product "+productId+" not found")));
        return productMono.zipWith(getFirstCartIdMono()).flatMap(
            tuple -> {
                var product = tuple.getT1();
                var cartId = tuple.getT2();
                return cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(), cartId)
                        .flatMap(cartItem -> {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            return cartItemsReactiveRepository.save(cartItem);
                        })
                        .switchIfEmpty(cartItemsReactiveRepository.save(new CartItem(productId,cartId)));
            }
        ).then();
    }

    public Mono<Void> removeCartItem(Long productId) {
        var productMono = productsReactiveRepository.findById(productId);
        productMono = productMono.switchIfEmpty(Mono.error(() -> new UnknownProductException("Product "+productId+" not found")));
        return productMono.zipWith(getFirstCartIdMono()).flatMap(
                tuple -> {
                    var product = tuple.getT1();
                    var cartId = tuple.getT2();
                    return cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(), cartId);
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
    }

    public Mono<Cart> getCartById(long id) {
        return cartsReactiveRepository.findById(id);
    }

    public Mono<Void> deleteAllItemsByCart(Cart cart) {
        return cartItemsReactiveRepository.deleteByCartId(cart.getId());
    }

    public Mono<Long> getFirstCartIdMono() {
        return cartsReactiveRepository.findAll().elementAt(0).map(Cart::getId);
    }
    public Flux<CartItem> getCartItemsFlux(Cart cart) {
        return getCartItemsFlux(Mono.just(cart.getId()));
    }
    public Flux<CartItem> getCartItemsFlux(Mono<Long> cartId) {
        return cartId.flatMapMany(cartItemsReactiveRepository::findAllByCart_Id).map(CartItemToUIDto::toCartItem);
    }
    public Mono<CartItem> getCartItemMono(Long cartId, Long productId) {
        return cartItemsReactiveRepository.findByProductIdAndCartId(cartId, productId);
    }
    public Flux<CartItemToUIDto> getCartItemsDtoFlux(Mono<Long> cartId) {
        return getCartItemsFlux(cartId).flatMap(cartItem -> {
            return productsReactiveRepository.findById(cartItem.getProductId()).zipWith(Mono.just(cartItem));
        }).map(tuple-> {
            var product = tuple.getT1();
            var cartItem = tuple.getT2();
            return new CartItemToUIDto(cartItem, new ProductToUIDto(product));
        });
    }
    public Mono<Double> getCartSumFlux(Mono<Long> cartId) {
        Double sum = (double) 0;
        return getCartItemsDtoFlux(cartId).reduce(
                sum,
                (accumulator, item) -> accumulator + item.getQuantity()*item.getProduct().getPrice()
        );
    }
}
