package ru.muravin.marketplaceshowcase.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.*;
import ru.muravin.marketplaceshowcase.services.RedisCacheService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MarketplaceShowcaseApplication.class)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
public class CartControllerTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    private CartsReactiveRepository cartsReactiveRepository;

    @Autowired
    private CartItemsReactiveRepository cartItemsReactiveRepository;
    @Autowired
    OrderItemsReactiveRepository orderItemsReactiveRepository;
    @Autowired
    OrderReactiveRepository orderReactiveRepository;
    @Autowired
    ProductsReactiveRepository productsReactiveRepository;
    @Autowired
    private UserReactiveRepository userReactiveRepository;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private ReactiveRedisTemplate<String, ProductToUIDto> reactiveRedisTemplate;
    @Autowired
    private ReactiveRedisTemplate<String, Long> reactiveRedisTemplateForLongValues;
    @Autowired
    private ReactiveRedisTemplate<String, CartItemToUIDto> reactiveRedisTemplateForCartItems;

    @BeforeEach
    void setUp() {
        cartItemsReactiveRepository.deleteAll().block();
        orderItemsReactiveRepository.deleteAll().block();
        productsReactiveRepository.deleteAll().block();
        orderReactiveRepository.deleteAll().block();
        cartsReactiveRepository.deleteAll().block();
        userReactiveRepository.deleteAll().block();
        redisCacheService.evictCache().onErrorComplete().block();
        var user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("abc@gmail.com");
        user.setAuthorities("ROLE_USER");
        var cart = new Cart();
        user = userReactiveRepository.save(user).block();
        cart.setUserId(user.getId());
        cartsReactiveRepository.save(cart).block();
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        product.setId(null);
        productsReactiveRepository.save(product).block();
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(null));
    }
    @Test
    void addItemToCartTest() throws Exception {
        var product = productsReactiveRepository.findAll().blockFirst();
        var user = userReactiveRepository.findAll().blockFirst();
        var auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .post().uri("/products/cart/changeCartItemQuantity/"+product.getId())
                .exchange().expectStatus().is3xxRedirection().expectHeader().location("/cart");
    }

    @Test
    void addItemToCartNoAuthTest() throws Exception {
        var product = productsReactiveRepository.findAll().blockFirst();
        webTestClient
                .post().uri("/products/cart/changeCartItemQuantity/"+product.getId())
                .exchange().expectStatus().is3xxRedirection().expectHeader().location("/login");
    }
    @Test
    void showCartUnauthorizedTest() throws Exception {
        var product = productsReactiveRepository.findAll().blockFirst();
        var cart = cartsReactiveRepository.findAll().blockFirst();
        CartItem cartItem = new CartItem(product,cart);
        cartItem.setQuantity(5);
        cartItemsReactiveRepository.save(cartItem).block();
        webTestClient.get().uri("/cart").exchange().expectStatus().is3xxRedirection().expectHeader().location("/login");
    }
    @Test
    void showCartTest() throws Exception {
        var user = userReactiveRepository.findAll().blockFirst();
        var auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        var product = productsReactiveRepository.findAll().blockFirst();
        var cart = cartsReactiveRepository.findAll().blockFirst();
        CartItem cartItem = new CartItem(product,cart);
        cartItem.setQuantity(5);
        cartItemsReactiveRepository.save(cartItem).block();
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .get().uri("/cart").exchange().expectStatus().isOk().expectHeader().contentType("text/html")
                .expectBody(String.class).value(body -> {
                    assertTrue(body.contains("125"));
                    assertTrue(body.contains("iphone"));
                });
    }
}
