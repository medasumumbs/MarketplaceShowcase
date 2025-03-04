package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.repositories.CartItemRepository;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;

@Service
public class CartService {

    private final ProductsRepository productsRepository;
    private CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductsRepository productsRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productsRepository = productsRepository;
    }

    public void addCartItem(Long productId) {
        var product = productsRepository.findById(productId).orElseThrow(
                () -> new UnknownProductException("Product "+productId+" not found")
        );
        CartItem cartItem = new CartItem(product, cart);
    }
}
