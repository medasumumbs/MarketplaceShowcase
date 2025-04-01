package ru.muravin.marketplaceshowcase.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.*;

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
    void addItemToCartTest() throws Exception {
        var product = productsReactiveRepository.findAll().blockFirst();
        webTestClient.post().uri("/products/changeCartItemQuantity/"+product.getId()).exchange().expectStatus().is3xxRedirection().expectHeader().location("/products");
    }
    @Test
    void showCartTest() throws Exception {
        /*
        //CartItem cartItem = new CartItem(productsRepository.findAll().get(0),cartsRepository.findAll().get(0));
        cartItem.setQuantity(5);
        cartItemRepository.save(cartItem);
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("125")))
                .andExpect(content().string(containsString("iphone")));

         */

    }
}
