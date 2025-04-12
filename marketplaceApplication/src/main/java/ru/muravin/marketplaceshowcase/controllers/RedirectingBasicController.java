package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@RequestMapping
@Controller
public class RedirectingBasicController {
    @GetMapping
    public Mono<Rendering> redirect() {
        return Mono.just(Rendering.redirectTo("/products").build());
    }
}
