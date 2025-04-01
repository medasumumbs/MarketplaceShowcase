package ru.muravin.marketplaceshowcase.integrationTests;

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
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
public class ProductControllerTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    private CartItemsReactiveRepository cartItemsReactiveRepository;
    @Autowired
    OrderItemsReactiveRepository orderItemsReactiveRepository;
    @Autowired
    OrderReactiveRepository orderReactiveRepository;
    @Autowired
    ProductsReactiveRepository productsReactiveRepository;
    @Autowired
    CartsReactiveRepository cartsReactiveRepository;
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
    void getProductsTest() throws Exception {
        var product = new Product(1L,"samsung",26d,"desc",new byte[0]);
        product.setId(null);
        productsReactiveRepository.save(product).block();
        var product1 = new Product(1L,"nokia",27d,"desc",new byte[0]);
        product1.setId(null);
        productsReactiveRepository.save(product1).block();

        webTestClient.get().uri("/products").exchange().expectStatus().isOk().expectHeader().contentType("text/html").expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("samsung"));
                    assertTrue(body.contains("iphone"));
                    assertTrue(body.contains("25"));
                    assertTrue(body.contains("26"));
                    assertTrue(body.contains("27"));
                });
    }
    @Test
    void changeCartItemQuantityTest() throws Exception {
        /*
        CartItem cartItem = new CartItem(productsRepository.findAll().get(0),cartsRepository.findAll().get(0));
        cartItem.setQuantity(5);
        var productId = cartItem.getProduct().getId();
        cartItemRepository.save(cartItem);

        mockMvc.perform(post("/products/changeCartItemQuantity/"+productId)
                        .param("action","plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
        cartItem = cartItemRepository.findAll().get(0);
        assertEquals(6, cartItem.getQuantity());
        mockMvc.perform(post("/products/changeCartItemQuantity/"+productId)
                        .param("action","minus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
        cartItem = cartItemRepository.findAll().get(0);
        assertEquals(5, cartItem.getQuantity());*/
    }
    @Test
    void getItemPage() throws Exception {
        var productId = productsReactiveRepository.findAll().blockLast().getId();

        webTestClient.get().uri("/products/" + productId)
                .exchange().expectStatus().isOk().expectHeader().contentType("text/html")
                .expectBody(String.class).value(body -> {
                    assertTrue(body.contains("iphone"));
                });
    }
}
