package ru.muravin.marketplaceshowcase.services;

import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.OrderItemToUIDto;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.NoOrderException;
import ru.muravin.marketplaceshowcase.exceptions.NoUserException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;
import ru.muravin.marketplaceshowcase.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderReactiveRepository orderReactiveRepository;
    private final UserReactiveRepository userReactiveRepository;
    private final CartService cartService;
    private final OrderItemsReactiveRepository orderItemsReactiveRepository;
    private final CartItemsReactiveRepository cartItemsReactiveRepository;
    private final RedisCacheService redisCacheService;

    public OrderService(OrderReactiveRepository orderReactiveRepository, UserReactiveRepository userReactiveRepository, CartService cartService, OrderItemsReactiveRepository orderItemsReactiveRepository, CartItemsReactiveRepository cartItemsReactiveRepository, RedisCacheService redisCacheService) {
        this.orderReactiveRepository = orderReactiveRepository;
        this.userReactiveRepository = userReactiveRepository;
        this.cartService = cartService;
        this.orderItemsReactiveRepository = orderItemsReactiveRepository;
        this.cartItemsReactiveRepository = cartItemsReactiveRepository;
        this.redisCacheService = redisCacheService;
    }

    @Transactional
    public Mono<Order> addOrder(Cart cart) {
        return userReactiveRepository.findAll().next().flatMap((user)->{
            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderDate(LocalDateTime.now());
            return orderReactiveRepository.save(order);
        }).flatMap(order -> {
            return cartItemsReactiveRepository.findAllByCart_Id(cart.getId()).map((item)->{
                return new OrderItem(item, order);
            }).collectList()
                    .flatMap(entities -> {
                        System.out.println(entities);
                        return orderItemsReactiveRepository.saveAll(entities).then();
                    })
                    .then(cartService.deleteAllItemsByCart(cart))
                    .then(redisCacheService.evictCartCache())
                    .then(Mono.just(order));
        });
    }

    public Mono<Order> findOrderById(Long id) {
        return orderReactiveRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoOrderException("Order not found")));
    }
    public Mono<OrderToUIDto> findOrderToUIDtoById(Long id) {
        Mono<List<OrderItemToUIDto>> orderItemsMono = orderItemsReactiveRepository.findAllByOrder_Id(id).collectList();
        Mono<Order> orderMono = findOrderById(id);
        return Mono.zip(orderMono, orderItemsMono).flatMap(tuple -> {
            var order = tuple.getT1();
            var orderItems = tuple.getT2();

            var orderToUIDto = new OrderToUIDto(order, orderItems);
            orderToUIDto.getOrderItems().forEach(orderItem -> {
                orderItem.setBase64Image(new String(orderItem.getImageBase64()));
            });
            return Mono.just(orderToUIDto);
        });
    }
    public Flux<OrderToUIDto> findAll() {
        return orderReactiveRepository.findAll().flatMap(order -> {
            return orderItemsReactiveRepository
                    .findAllByOrder_Id(order.getId()).collectList()
                    .flatMap((orderItems -> {
                        var dto = new OrderToUIDto(order, orderItems);
                        dto.getOrderItems().forEach(orderItem -> {
                            if (orderItem.getBase64Image() != null) {
                                orderItem.setBase64Image(new String(orderItem.getImageBase64()));
                                orderItem.setImageBase64(null);
                            }
                        });
                        return Mono.just(dto);
                    }));
        });
    }
}
