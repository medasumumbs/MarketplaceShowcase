package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart/add")
public class CartController {
    @PostMapping
    public String addItemToCart(@RequestParam Integer productId) {

        return "redirect:/products";
    }
}
