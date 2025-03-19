package ru.muravin.marketplaceshowcase.serviceTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.repositories.CartsRepository;
import ru.muravin.marketplaceshowcase.repositories.OrderItemRepository;
import ru.muravin.marketplaceshowcase.repositories.OrderRepository;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;
import ru.muravin.marketplaceshowcase.repositories.UserRepository;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.OrderService;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class OrderServiceTest {


    @MockitoBean(reset = MockReset.BEFORE)
    OrderRepository repository;
    @MockitoBean(reset = MockReset.BEFORE)
    private UserRepository userRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderService orderService;
    @MockitoBean(reset = MockReset.BEFORE)
    CartService cartService;
    @MockitoBean
    private CartsRepository cartsRepository;

    @Test
    void findByIdTest() {
        var order = getTestOrder(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        var result = orderService.findOrderById(1L);
        assertEquals(order, result);
    }

    private static @NotNull Order getTestOrder(Long id) {
        var product = new Product(id,"iphone",25d,"desc",new byte[0]);
        var order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(new User());
        order.setId(1L);
        var orderItem = new OrderItem();
        orderItem.setId(1L);
        //orderItem.setOrder(order);
        orderItem.setPrice(123.23);
        orderItem.setQuantity(1);
        orderItem.setProduct(product);
        order.setOrderItems(List.of(orderItem));
        return order;
    }

    @Test
    void findAllTest() {
        var listOfOrders = List.of(getTestOrder(1L),getTestOrder(2L),getTestOrder(3L));
        when(repository.findAll()).thenReturn(listOfOrders);
        var result = orderService.findAll();
        assertEquals(listOfOrders.stream().map(OrderToUIDto::new).toList(), result);
    }
    @Test
    void saveTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var user = new User();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        Cart cart = new Cart();
        var cartItem = new CartItem();
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        when(cartsRepository.findAll()).thenReturn(List.of(cart));
        when(cartService.getCartItems(cart)).thenReturn(List.of(cartItem,cartItem));
        orderService.addOrder(cart);

        verify(repository, times(1)).save(any(Order.class));
        verify(userRepository, times(1)).findAll();
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
    }
}
