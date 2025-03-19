package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.muravin.marketplaceshowcase.services.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public String addItemToCart(@RequestParam Integer productId) {

        return "redirect:/products";
    }
    @GetMapping
    public String showCart(Model model) {
        model.addAttribute("sumOfOrder",cartService.getCartSum(cartService.getFirstCartId()));
        model.addAttribute("cartItems",cartService.getCartItemDtoList(cartService.getFirstCartId()));
        return "cart";
    }
}
