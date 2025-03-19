package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.muravin.marketplaceshowcase.dto.OrderToUIDto;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.OrderService;

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
    public String addOrder() {
        var cart = cartService.getCartById(cartService.getFirstCartId());
        Order order = orderService.addOrder(cart);
        return "redirect:/orders/" + order.getId() + "?justBought=true";
    }
    @GetMapping("/{id}")
    public String getOrder(@PathVariable Long id, Model model,
                           @RequestParam(name = "justBought",defaultValue = "false") boolean justBought) {
        model.addAttribute("justBought", justBought);
        model.addAttribute("order",new OrderToUIDto(orderService.findOrderById(id)));
        return "order";
    }
    @GetMapping
    public String getOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "orders";
    }
}
