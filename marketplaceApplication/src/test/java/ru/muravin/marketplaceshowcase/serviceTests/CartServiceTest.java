package ru.muravin.marketplaceshowcase.serviceTests;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownCartException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.CartItemsReactiveRepository;
import ru.muravin.marketplaceshowcase.repositories.CartsReactiveRepository;
import ru.muravin.marketplaceshowcase.repositories.ProductsReactiveRepository;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.RedisCacheService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class CartServiceTest {
    @Autowired
    CartService cartService;

    @MockitoBean(reset = MockReset.BEFORE)
    private CartItemsReactiveRepository cartItemsReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private CartsReactiveRepository cartsReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private ProductsReactiveRepository productsReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private RedisCacheService redisCacheService;

    @Test
    void addCartItemTest() {
        when(redisCacheService.evictCartCache()).thenReturn(Mono.just(2l));

        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        var cart = new Cart();
        cart.setId(1L);
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.just(cart));

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(1);
        cartItem.setId(1L);
        when(cartsReactiveRepository.findAll()).thenReturn(Flux.just(cart));
        when(cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(),cart.getId())).thenReturn(Mono.just(cartItem));
        when(cartItemsReactiveRepository.delete(cartItem)).thenReturn(Mono.empty());
        when(cartItemsReactiveRepository.save(ArgumentMatchers.any())).thenReturn(Mono.just(cartItem));
        when(cartItemsReactiveRepository.save(cartItem)).thenReturn(Mono.just(cartItem));
        cartService.addCartItem(product.getId()).block();
        verify(cartItemsReactiveRepository, times(1)).findByProductIdAndCartId(product.getId(),cart.getId());
        verify(productsReactiveRepository, times(1)).findById(1L);
        verify(cartsReactiveRepository, times(1)).findAll();
        verify(cartItemsReactiveRepository, times(1)).save(cartItem);
        when(cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(),cart.getId())).thenReturn(Mono.empty());
        cartService.addCartItem(product.getId()).block();
        verify(cartItemsReactiveRepository, times(2)).findByProductIdAndCartId(product.getId(),cart.getId());
        verify(productsReactiveRepository, times(2)).findById(1L);
        verify(cartsReactiveRepository, times(2)).findAll();
        verify(cartItemsReactiveRepository, times(3)).save(argThat(cartItem1 ->
                cartItem1.getProductId().equals(product.getId()) &&
                        cartItem1.getCartId().equals(cart.getId())
        ));
    }

    @Test
    void removeCartItemTest() {
        when(redisCacheService.evictCartCache()).thenReturn(Mono.just(2l));

        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        var cart = new Cart();
        cart.setId(1L);
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.just(cart));

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(2);
        cartItem.setId(1L);

        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        when(cartsReactiveRepository.findAll()).thenReturn(Flux.just(cart));
        when(cartItemsReactiveRepository.findByProductIdAndCartId(product.getId(), cart.getId())).thenReturn(Mono.just(cartItem));
        when(cartItemsReactiveRepository.delete(cartItem)).thenReturn(Mono.empty());
        when(cartItemsReactiveRepository.save(cartItem)).thenReturn(Mono.just(cartItem));
        cartService.removeCartItem(product.getId()).block();
        verify(cartItemsReactiveRepository, times(1)).findByProductIdAndCartId(product.getId(),cart.getId());
        verify(productsReactiveRepository, times(1)).findById(1L);
        verify(cartsReactiveRepository, times(1)).findAll();
        verify(cartItemsReactiveRepository, times(1)).save(argThat(cartItem1 ->
                cartItem1.getProductId().equals(product.getId()) &&
                        cartItem1.getCartId().equals(cart.getId())
        ));

        cartService.removeCartItem(product.getId()).block();
        verify(cartItemsReactiveRepository, times(2)).findByProductIdAndCartId(product.getId(),cart.getId());
        verify(productsReactiveRepository, times(2)).findById(1L);
        verify(cartsReactiveRepository, times(2)).findAll();
        verify(cartItemsReactiveRepository, times(1)).save(argThat(cartItem1 ->
                cartItem1.getProductId().equals(product.getId()) &&
                        cartItem1.getCartId().equals(cart.getId())
        ));
    }


    @Test
    void getCartByIdTest() {
        var cart = new Cart();
        cart.setId(1L);
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.just(cart));
        assertEquals(cart, cartService.getCartById(1L).block());
    }

    @Test
    void getCartByIdTestThrowsError() {
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.empty());
        cartService.getCartById(1L).doOnError(e -> assertTrue(e instanceof UnknownCartException)).block();
    }

    @Test
    void getCartItemsTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        cart.setId(1L);
        var cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        var cartItemDto = new CartItemToUIDto(cartItem, new ProductToUIDto(product));
        when(cartItemsReactiveRepository.findAllByCart_Id(cart.getId())).thenReturn(Flux.just(cartItemDto));
        cartService.getCartItemsFlux(cart).doOnNext(next->{
            assertNotNull(next);
            assertEquals(cartItem, next);
        }).blockFirst();
    }

    @Test
    void getCartItemsByIdTest() {
        when(redisCacheService.setCartItemsCache(any(),any())).thenReturn(Mono.just(2l));
        when(redisCacheService.getCartItemsCache(any())).thenReturn(Flux.empty());
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        cart.setId(1L);
        var cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartItemsReactiveRepository.findAllByCart_Id(cart.getId())).thenReturn(Flux.just(new CartItemToUIDto(cartItem, new ProductToUIDto(product))));
        cartService.getCartItemsDtoFlux(Mono.just(1L)).doOnNext(item -> assertEquals(cartItem, item.toCartItem())).blockFirst();
    }

    @Test
    void getCartSumTest() {
        when(redisCacheService.setCartItemsCache(any(),any())).thenReturn(Mono.just(2l));
        when(redisCacheService.getCartItemsCache(any())).thenReturn(Flux.empty());
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var cart = new Cart();
        cart.setId(1L);
        var cartItem = new CartItem();
        cartItem.setCartId(1L);
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setId(1L);
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        when(cartsReactiveRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartItemsReactiveRepository.findAllByCart_Id(cart.getId())).thenReturn(Flux.just(
                new CartItemToUIDto(cartItem,new ProductToUIDto(product)),
                new CartItemToUIDto(cartItem,new ProductToUIDto(product)))
        );
        cartService.getCartSumMono(Mono.just(1l)).doOnSuccess(sum -> {
            assertEquals(100d, sum);
        }).block();
    }

    @Test
    void deleteAllItemsByCartTest() {
        var cart = new Cart();
        cart.setId(1L);
        when(cartItemsReactiveRepository.deleteByCartId(1L)).thenReturn(Mono.empty());
        cartService.deleteAllItemsByCart(cart).doOnSuccess(v -> verify(cartItemsReactiveRepository, times(1)).deleteByCartId(1L)).block();
    }
}
