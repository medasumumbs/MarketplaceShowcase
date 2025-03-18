package ru.muravin.marketplaceshowcase.serviceTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.CartItemRepository;
import ru.muravin.marketplaceshowcase.repositories.CartsRepository;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;
import ru.muravin.marketplaceshowcase.services.CartService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class CartServiceTest {
    @Autowired
    CartService cartService;

    @MockitoBean(reset= MockReset.BEFORE)
    ProductsRepository productsRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    CartsRepository cartsRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    CartItemRepository cartItemRepository;

    @Test
    void addCartItemTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        when(productsRepository.findById(1L)).thenReturn(Optional.of(product));
        var cart = new Cart();
        cart.setId(1L);
        when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setId(1L);
        when(cartItemRepository.findByProductAndCart(product,cart)).thenReturn(Optional.of(cartItem));
        cartService.addCartItem(product.getId());
        verify(cartItemRepository, times(1)).findByProductAndCart(product,cart);
        verify(productsRepository, times(1)).findById(1L);
        verify(cartsRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(cartItem);
        when(cartItemRepository.findByProductAndCart(product,cart)).thenReturn(Optional.empty());
        cartService.addCartItem(product.getId());
        verify(cartItemRepository, times(2)).findByProductAndCart(product,cart);
        verify(productsRepository, times(2)).findById(1L);
        verify(cartsRepository, times(2)).findById(1L);
        verify(cartItemRepository, times(2)).save(any(cartItem.getClass()));

    }
    /*
    * public void addCartItem(Long productId) {
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
    public List<CartItemToUIDto> getCartItemDtoList(Long cartId) {
        return getCartItems(cartId).stream().map(CartItemToUIDto::new).toList();
    }

    public Double getCartSum(long cartId) {
        AtomicReference<Double> sum = new AtomicReference<>(Double.valueOf(0));
        getCartItems(cartId)
                .forEach((item) -> sum.updateAndGet(v -> v + (item.getQuantity() * item.getProduct().getPrice())));
        return sum.get();
    }

    public void deleteAllItemsByCart(Cart cart) {
        cartItemRepository.deleteByCart(cart);
    }*/
}
