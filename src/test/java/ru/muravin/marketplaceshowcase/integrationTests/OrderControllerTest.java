package ru.muravin.marketplaceshowcase.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import reactor.core.publisher.Flux;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
public class OrderControllerTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    CartItemsReactiveRepository cartItemsReactiveRepository;
    @Autowired
    OrderItemsReactiveRepository orderItemsReactiveRepository;
    @Autowired
    OrderReactiveRepository orderReactiveRepository;
    @Autowired
    ProductsReactiveRepository productsReactiveRepository;
    @Autowired
    private CartsReactiveRepository cartsReactiveRepository;
    @Autowired
    private UserReactiveRepository userReactiveRepository;

    @BeforeEach
    void setUp() {
        cartItemsReactiveRepository.deleteAll().block();
        orderItemsReactiveRepository.deleteAll().block();
        productsReactiveRepository.deleteAll().block();
        orderReactiveRepository.deleteAll().block();
        cartsReactiveRepository.deleteAll().block();
        userReactiveRepository.deleteAll().block();
        var user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("abc@gmail.com");
        var cart = new Cart();
        user = userReactiveRepository.save(user).block();
        cart.setUserId(user.getId());
        cartsReactiveRepository.save(cart).block();
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        product.setId(null);
        productsReactiveRepository.save(product).block();
    }

    @Test
    void addOrderTest() throws Exception {
        var product = productsReactiveRepository.findAll().blockFirst();
        var cart = cartsReactiveRepository.findAll().blockFirst();
        CartItem cartItem = new CartItem(product,cart);
        cartItem.setQuantity(5);
        cartItemsReactiveRepository.save(cartItem).block();

        Flux.just(webTestClient.post().uri("/orders").exchange().expectStatus()).doOnNext(a->{
            var orderId = orderReactiveRepository.findAll().blockLast().getId();
            a.is3xxRedirection().expectHeader().location("/orders/"+orderId+"?justBought=true");
        }).blockLast();

    }

    @Test
    void getOrderTest() throws Exception {
        addOrderTest();
        var orderId = orderReactiveRepository.findAll().blockLast().getId();
        webTestClient.get().uri("/orders/"+orderId).exchange().expectStatus().isOk().expectBody(String.class).value(body->{
            Assertions.assertTrue(body.contains("125"));
            Assertions.assertTrue(body.contains("iphone"));
        });
    }
    @Test
    void getOrdersTest() throws Exception {
        addOrderTest();
        var orderId = orderReactiveRepository.findAll().blockLast().getId();
        webTestClient.get().uri("/orders").exchange().expectStatus().isOk().expectBody(String.class).value(body->{
            Assertions.assertTrue(body.contains("125"));
            Assertions.assertTrue(body.contains("iphone"));
        });
    }
}
