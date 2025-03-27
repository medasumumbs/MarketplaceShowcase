package ru.muravin.marketplaceshowcase.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@Disabled
public class CartControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

  /*  @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductsRepository productsRepository;*/

    private MockMvc mockMvc; // Используется для отправки HTTP-запросов

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        /*cartItemRepository.deleteAll();
        orderItemRepository.deleteAll();
        productsRepository.deleteAll();
        orderRepository.deleteAll();*/
        //userRepository.deleteAll();
        //cartsRepository.deleteAll();
        var user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("abc@gmail.com");
        var cart = new Cart();
        //userRepository.save(user);
        cart.setUser(user);
        //cartsRepository.save(cart);

        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        product.setId(null);
        //productsRepository.save(product);
    }
    @Test
    void addItemToCartTest() throws Exception {
        mockMvc.perform(post("/cart/add").param("productId","1")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
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
