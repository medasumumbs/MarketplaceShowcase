package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.services.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Mono<String> addItemToCart(@RequestParam Integer productId) {
        return Mono.just("redirect:/products");
    }
    @GetMapping
    public Mono<String> showCart(Model model) {
        var firstCartIdMono = cartService.getFirstCartIdMono();
        var sumOfOrder = cartService.getCartSumFlux(firstCartIdMono);
        var cartItems = cartService.getCartItemsFlux(firstCartIdMono);
        return Mono.zip(sumOfOrder, cartItems.collectList()).doOnNext(tuple -> {
            model.addAttribute("cartItems", tuple.getT2());
            model.addAttribute("sumOfOrder", tuple.getT1());
        }).map(cart->"cart").defaultIfEmpty("errorPage");
    }
}
