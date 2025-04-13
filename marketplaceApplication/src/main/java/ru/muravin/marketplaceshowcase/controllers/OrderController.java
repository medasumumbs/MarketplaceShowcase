package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.CurrentUserService;
import ru.muravin.marketplaceshowcase.services.OrderService;

import java.net.URI;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final CartService cartService;
    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderController(CartService cartService, OrderService orderService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }
    @PostMapping
    public Mono<Rendering> addOrder() {
        return currentUserService.getCurrentUserId()
                .flatMap(cartService::getCartByUserId)
                .flatMap(orderService::addOrder)
                .map((order) -> {
                    return Rendering.redirectTo("/orders/" + order.getId() + "?justBought=true").build();
                }).onErrorMap(e -> {
                    if (e.getMessage().contains("400")) {
                        return new RuntimeException("Баланс недостаточен для совершения заказа");
                    } else if (e.getMessage().contains("404")) {
                        return new RuntimeException("Пользователь не найден");
                    } else {
                        System.out.println(e.getMessage());
                        return new RuntimeException("Платежный сервис временно недоступен");
                    }
                }).onErrorResume(error -> {
                    return Mono.just(Rendering.view("errorPage")
                            .modelAttribute("message", "Заказ не может быть совершен: " + error.getMessage()).build());
                });
    }
    @GetMapping("/{id}")
    public Mono<Rendering> getOrder(@PathVariable Long id,
                           @RequestParam(name = "justBought",defaultValue = "false") boolean justBought) {
        return orderService.findOrderToUIDtoById(id).map(dto -> {
            return Rendering.view("order").modelAttribute("order",dto)
                    .modelAttribute("justBought",justBought).build();
        });
    }
    @GetMapping
    public Mono<Rendering> getOrders() {
        return orderService.findAll().collectList().flatMap(orders -> {
            return Mono.just(Rendering.view("orders").modelAttribute("orders", orders).build());
        });
    }
    @ExceptionHandler(Exception.class)
    public Mono<Rendering> unknownErrorPage(Model model, Exception exception) {
        exception.printStackTrace();
        return Mono.just(Rendering.view("errorPage").modelAttribute("message", "Неизвестная ошибка: " + exception.getMessage()).build());
    }
}
