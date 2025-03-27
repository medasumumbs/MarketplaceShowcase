package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.OrderService;

import java.net.URI;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final CartService cartService;
    private final OrderService orderService;

    public OrderController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @PostMapping
    public Mono<ServerResponse> addOrder() {
        return cartService.getFirstCartIdMono()
                .flatMap(cartService::getCartById)
                .flatMap(orderService::addOrder)
                .flatMap((order) -> {
                    return ServerResponse.temporaryRedirect(URI.create("redirect:/orders/" + order.getId() + "?justBought=true")).build();
                });
    }
    @GetMapping("/{id}")
    public Mono<ServerResponse> getOrder(@PathVariable Long id,
                           @RequestParam(name = "justBought",defaultValue = "false") boolean justBought) {
        return orderService.findOrderById(id).map(OrderToUIDto::new).flatMap(dto -> {
            return ServerResponse.ok().render("order", Map.of("order", dto, "justBought", justBought));
        });
    }
    @GetMapping
    public Mono<ServerResponse> getOrders() {
        return orderService.findAll().collectList().flatMap(orders -> {
            return ServerResponse.ok().render("orders", Map.of("orders", orders));
        });
    }
}
