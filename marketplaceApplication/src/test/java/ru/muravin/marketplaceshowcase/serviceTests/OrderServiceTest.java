package ru.muravin.marketplaceshowcase.serviceTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import ru.muravin.marketplaceshowcase.dto.OrderItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.paymentServiceClient.api.PaymentServiceClientAPI;
import ru.muravin.marketplaceshowcase.paymentServiceClient.model.PaymentResponse;
import ru.muravin.marketplaceshowcase.repositories.CartItemsReactiveRepository;
import ru.muravin.marketplaceshowcase.repositories.OrderItemsReactiveRepository;
import ru.muravin.marketplaceshowcase.repositories.OrderReactiveRepository;
import ru.muravin.marketplaceshowcase.repositories.UserReactiveRepository;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.OrderService;
import ru.muravin.marketplaceshowcase.services.RedisCacheService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class OrderServiceTest {


    @MockitoBean(reset = MockReset.BEFORE)
    OrderReactiveRepository orderReactiveRepository;
    @Autowired
    private OrderService orderService;
    @MockitoBean(reset = MockReset.BEFORE)
    CartService cartService;
    @MockitoBean(reset = MockReset.BEFORE)
    private OrderItemsReactiveRepository orderItemsReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private UserReactiveRepository userReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private CartItemsReactiveRepository cartItemsReactiveRepository;
    @MockitoBean(reset = MockReset.BEFORE)
    private PaymentServiceClientAPI paymentServiceClient;
    @MockitoBean(reset = MockReset.BEFORE)
    private RedisCacheService redisCacheService;
    @Test
    void findByIdTest() {
        var order = getTestOrder(1L);
        when(orderReactiveRepository.findById(1L)).thenReturn(Mono.just(order));
        var result = orderService.findOrderById(1L).block();
        assertEquals(order, result);
    }

    private static @NotNull Order getTestOrder(Long id) {
        var order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUserId(1L);
        order.setId(1L);
        return order;
    }

    private static OrderItem getTestOrderItem(Order order, Product product) {
        var orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrderId(order.getId());
        orderItem.setPrice(123.23);
        orderItem.setQuantity(1);
        orderItem.setProductId(product.getId());
        return orderItem;
    }

    @Test
    void findAllTest() {
        var listOfOrders = List.of(getTestOrder(1L),getTestOrder(2L),getTestOrder(3L));
        when(orderReactiveRepository.findAll()).thenReturn(Flux.fromIterable(listOfOrders));
        var orderItemDto = new OrderItemToUIDto();
        orderItemDto.setId(1L);
        orderItemDto.setPrice(123.23);
        orderItemDto.setQuantity(1);
        var bytes = new byte[] {1,2,3,4};
        orderItemDto.setImageBase64(bytes);
        when(orderItemsReactiveRepository.findAllByOrder_Id(any())).thenReturn(Flux.just(orderItemDto));
        var result = orderService.findAll().toIterable();
        var resultParsed = new ArrayList<OrderToUIDto>();
        result.forEach(resultParsed::add);
        assertEquals(listOfOrders.stream().map((@NotNull Order order) -> {
            return new OrderToUIDto(order, List.of(orderItemDto));
        }).toList(), resultParsed);
    }
    @Test
    void saveTest() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        var user = new User();
        var testOrder = getTestOrder(1L);
        var testItem = getTestOrderItem(testOrder, product);
        var testCartItem = new CartItemToUIDto(new CartItem(1l,1l), new ProductToUIDto(product));
        user.setId(1L);
        when(userReactiveRepository.findAll()).thenReturn(Flux.just(user));
        when(orderReactiveRepository.save(any())).thenReturn(Mono.just(testOrder));
        when(cartItemsReactiveRepository.findAllByCart_Id(anyLong())).thenReturn(Flux.just(testCartItem));
        when(orderItemsReactiveRepository.saveAll(any(Iterable.class))).thenReturn(Flux.just(new ArrayList<OrderItemToUIDto>() {}));
        when(cartService.deleteAllItemsByCart(any())).thenReturn(Mono.empty());
        when(paymentServiceClient.usersUserIdMakePaymentPost(any(), any())).thenReturn(Mono.just(
                new PaymentResponse().restBalance(100.00f).message("OK")
        ));
        when(cartService.getCartSumMono(any())).thenReturn(Mono.just(Double.valueOf("50")));
        when(redisCacheService.evictCartCache(1)).thenReturn(Mono.just(2l));
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        var cartItem = new CartItem();
        cartItem.setQuantity(2);
        orderService.addOrder(cart).block();

       verify(orderReactiveRepository, times(1)).save(any(Order.class));
       verify(userReactiveRepository, times(1)).findAll();
       verify(orderItemsReactiveRepository, times(1)).saveAll(any(Iterable.class));
    }
}
