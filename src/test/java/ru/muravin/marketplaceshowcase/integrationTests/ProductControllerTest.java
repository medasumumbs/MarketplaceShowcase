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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@Disabled
public class ProductControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    /*@Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductsRepository productsRepository;*/

    private MockMvc mockMvc; // Используется для отправки HTTP-запросов

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        //cartItemRepository.deleteAll();
        //orderItemRepository.deleteAll();
        //productsRepository.deleteAll();
        //orderRepository.deleteAll();
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
    void getProductsTest() throws Exception {
        var product = new Product(1L,"samsung",26d,"desc",new byte[0]);
        product.setId(null);
        //productsRepository.save(product);
        var product1 = new Product(1L,"nokia",27d,"desc",new byte[0]);
        product1.setId(null);
        //productsRepository.save(product1);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("samsung")))
                .andExpect(content().string(containsString("iphone")))
                .andExpect(content().string(containsString("25")))
                .andExpect(content().string(containsString("26")))
                .andExpect(content().string(containsString("27")));
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
       // var productId = productsRepository.findAll().get(0).getId();
        var productId = 0;
        mockMvc.perform(get("/products/"+productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("iphone")));
    }
}
