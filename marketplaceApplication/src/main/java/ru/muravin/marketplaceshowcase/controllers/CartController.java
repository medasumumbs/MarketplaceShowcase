package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.User;
import ru.muravin.marketplaceshowcase.paymentServiceClient.api.PaymentServiceClientAPI;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.CurrentUserService;

import java.net.URI;
import java.util.HashMap;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final PaymentServiceClientAPI paymentServiceClientAPI;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, PaymentServiceClientAPI paymentServiceClientAPI, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.paymentServiceClientAPI = paymentServiceClientAPI;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/add")
    public Mono<ServerResponse> addItemToCart(@RequestParam Integer productId) {
        return ServerResponse.temporaryRedirect(URI.create("redirect:/products")).build();
    }
    @GetMapping
    public Mono<Rendering> showCart(Model model) {
        var currentCartId = currentUserService.getCurrentUserId().flatMap(cartService::getCartByUserId).map(Cart::getId);
        var sumOfOrder = cartService.getCartSumMono(currentCartId);
        var cartItems = cartService.getCartItemsDtoFlux(currentCartId);
        var balance = currentUserService.getCurrentUserId().flatMap((Long userId) -> paymentServiceClientAPI.usersUserIdGet(Math.toIntExact(userId)));
        return Mono.zip(sumOfOrder, cartItems.collectList(), balance).map(tuple -> {
            var hashMap = new HashMap<String, Object>();
            hashMap.put("cartItems", tuple.getT2());
            hashMap.put("sumOfOrder", tuple.getT1());
            hashMap.put("balance", tuple.getT3().getBalance());
            if (tuple.getT3().getBalance() < 0) {
                hashMap.put("serviceIsUnavailable", true);
            } else {
                hashMap.put("serviceIsUnavailable", false);
            }
            return Rendering.view("cart").model(hashMap).build();
        });
    }

}
