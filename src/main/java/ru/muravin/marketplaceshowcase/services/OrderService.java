package ru.muravin.marketplaceshowcase.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.NoOrderException;
import ru.muravin.marketplaceshowcase.exceptions.NoUserException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;
import ru.muravin.marketplaceshowcase.repositories.OrderItemRepository;
import ru.muravin.marketplaceshowcase.repositories.OrderRepository;
import ru.muravin.marketplaceshowcase.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;

    public OrderService(UserRepository userRepository, OrderRepository orderRepository, CartService cartService, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order addOrder(Cart cart) {
        Order order = new Order();
        order.setUser(userRepository.findById(1L).orElseThrow(() -> new NoUserException("User not found")));
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());
        orderRepository.save(order);
        var cartItems = cartService.getCartItems(cart);
        cartItems.forEach(cartItem -> {
            OrderItem orderItem = new OrderItem(cartItem, order);
            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);
        });
        cartService.deleteAllItemsByCart(cart);
        return order;
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(()->new NoOrderException("Order not found"));
    }

    public List<OrderToUIDto> findAll() {
        return orderRepository.findAll().stream().map(OrderToUIDto::new).collect(Collectors.toList());
    }
}
