package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.marketplaceshowcase.exceptions.UnknownCartException;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.repositories.CartItemRepository;
import ru.muravin.marketplaceshowcase.repositories.CartsRepository;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;

import java.util.List;

@Service
public class CartService {

    private final ProductsRepository productsRepository;
    private final CartsRepository cartsRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductsRepository productsRepository, CartsRepository cartsRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productsRepository = productsRepository;
        this.cartsRepository = cartsRepository;
    }

    public void addCartItem(Long productId) {
        var product = productsRepository.findById(productId).orElseThrow(
                () -> new UnknownProductException("Product "+productId+" not found")
        );
        // Пока в приложении один пользователь - корзину ищем как первую в базе
        var cart = cartsRepository.findById(1l).orElseThrow(() -> new UnknownCartException("Cart "+1l+" not found"));
        var cartItemFromRepo = cartItemRepository.findByProductAndCart(product, cart);
        CartItem cartItem;
        if (cartItemFromRepo.isPresent()) {
            cartItem = cartItemFromRepo.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            cartItem = new CartItem(product, cart);
        }
        cartItemRepository.save(cartItem);
    }

    public void removeCartItem(Long productId) {
        var product = productsRepository.findById(productId).orElseThrow(
                () -> new UnknownProductException("Product "+productId+" not found")
        );
        // Пока в приложении один пользователь - корзину ищем как первую в базе
        var cart = cartsRepository.findById(1l).orElseThrow(() -> new UnknownCartException("Cart "+1l+" not found"));
        var cartItemFromRepo = cartItemRepository.findByProductAndCart(product, cart);
        CartItem cartItem;
        if (cartItemFromRepo.isPresent()) {
            cartItem = cartItemFromRepo.get();
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            if (cartItem.getQuantity() == 0) {
                cartItemRepository.delete(cartItem);
            } else {
                cartItemRepository.save(cartItem);
            }
        }
    }

    public Cart getCartById(long id) {
        return cartsRepository.findById(id).orElseThrow(
                () -> new UnknownCartException("Cart "+id+" not found")
        );
    }

    public List<CartItem> getCartItems(Cart cart) {
        return cartItemRepository.findAllByCart(cart);
    }
    public List<CartItem> getCartItems(Long cartId) {
        return getCartItems(getCartById(cartId));
    }
}
