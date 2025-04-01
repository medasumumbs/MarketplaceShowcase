package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.services.CartService;

import java.net.URI;
import java.util.HashMap;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Mono<ServerResponse> addItemToCart(@RequestParam Integer productId) {
        return ServerResponse.temporaryRedirect(URI.create("redirect:/products")).build();
    }
    @GetMapping
    public Mono<Rendering> showCart(Model model) {
        var firstCartIdMono = cartService.getFirstCartIdMono();
        var sumOfOrder = cartService.getCartSumFlux(firstCartIdMono);
        var cartItems = cartService.getCartItemsDtoFlux(firstCartIdMono);
        return Mono.zip(sumOfOrder, cartItems.collectList()).map(tuple -> {
            var hashMap = new HashMap<String, Object>();
            hashMap.put("cartItems", tuple.getT2());
            hashMap.put("sumOfOrder", tuple.getT1());
            return Rendering.view("cart").model(hashMap).build();
        });
    }
}
