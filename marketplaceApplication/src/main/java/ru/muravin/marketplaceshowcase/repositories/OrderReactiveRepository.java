package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.Product;

@Repository
public interface OrderReactiveRepository extends R2dbcRepository<Order, Long> {
    Flux<Order> findAllByUserId(Long userId);
}
