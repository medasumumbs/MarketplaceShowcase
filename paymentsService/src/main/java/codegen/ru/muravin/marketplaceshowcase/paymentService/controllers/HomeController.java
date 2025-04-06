package codegen.ru.muravin.marketplaceshowcase.paymentService.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Home redirection to OpenAPI api documentation
 */
@Controller
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    Mono<Rendering> index() {
        return Mono.just(Rendering.view("swagger-ui").build());
    }

}