package ru.muravin.marketplaceshowcase.serviceTests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.exceptions.UnknownCartException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;
import ru.muravin.marketplaceshowcase.services.CartService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
@Disabled
public class CartServiceTest {
    @Autowired
    CartService cartService;
/*
    @MockitoBean(reset= MockReset.BEFORE)
    ProductsRepository productsRepository;*/


    @Test
    void addCartItemTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        //when(productsRepository.findById(1L)).thenReturn(Optional.of(product));
        var cart = new Cart();
        cart.setId(1L);
        //when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setId(1L);
        //when(cartsRepository.findAll()).thenReturn(List.of(cart));
        //when(cartItemRepository.findByProductAndCart(product,cart)).thenReturn(Optional.of(cartItem));
        cartService.addCartItem(product.getId());
        //verify(cartItemRepository, times(1)).findByProductAndCart(product,cart);
        //verify(productsRepository, times(1)).findById(1L);
        //verify(cartsRepository, times(1)).findAll();
        //verify(cartItemRepository, times(1)).save(cartItem);
        //when(cartItemRepository.findByProductAndCart(product,cart)).thenReturn(Optional.empty());
        cartService.addCartItem(product.getId());
        //verify(cartItemRepository, times(2)).findByProductAndCart(product,cart);
        //verify(productsRepository, times(2)).findById(1L);
        //verify(cartsRepository, times(2)).findAll();
        //verify(cartItemRepository, times(2)).save(any(cartItem.getClass()));
    }
    @Test
    void removeCartItemTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
       // when(productsRepository.findById(1L)).thenReturn(Optional.of(product));
        var cart = new Cart();
        cart.setId(1L);
        //when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setId(1L);

        //when(cartsRepository.findAll()).thenReturn(List.of(cart));
        //when(cartItemRepository.findByProductAndCart(product,cart)).thenReturn(Optional.of(cartItem));
        cartService.removeCartItem(product.getId());
        //verify(cartItemRepository, times(1)).findByProductAndCart(product,cart);
       // verify(productsRepository, times(1)).findById(1L);
        //verify(cartsRepository, times(1)).findAll();
        //verify(cartItemRepository, times(1)).save(any(CartItem.class));
        cartService.removeCartItem(product.getId());
        //verify(cartItemRepository, times(2)).findByProductAndCart(product,cart);
       // verify(productsRepository, times(2)).findById(1L);
        //verify(cartsRepository, times(2)).findAll();
        //verify(cartItemRepository, times(1)).delete(any(CartItem.class));
    }
    @Test
    void getCartByIdTest() {
        var cart = new Cart();
       // when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));
        var result = cartService.getCartById(1L);
        assertNotNull(result);
        assertEquals(cart,result);
    }
    @Test
    void getCartByIdTestThrowsError() {
        //when(cartsRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownCartException.class,()->cartService.getCartById(1L));
    }
    @Test
    void getCartItemsTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        var cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        //when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));
        //var result = cartService.getCartItems(cart);
        //assertNotNull(result);
        //assertEquals(cartItem,result.getFirst());
    }
    @Test
    void getCartItemsByIdTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        var cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        //when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));
        //when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));
        //var result = cartService.getCartItems(1L);
        //assertNotNull(result);
        //assertEquals(cartItem,result.getFirst());
    }

    @Test
    void getCartSumTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        var cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        //when(cartsRepository.findById(1L)).thenReturn(Optional.of(cart));
        //when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem,cartItem));
        //assertEquals(100d, cartService.getCartSum(1L));
    }
    @Test
    void deleteAllItemsByCartTest() {
        var cart = new Cart();
        //doNothing().when(cartItemRepository).deleteByCart(any(cart.getClass()));
        cartService.deleteAllItemsByCart(cart);
        //verify(cartItemRepository, times(1)).deleteByCart(cart);
    }
}
