package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.CartItem;

@Repository
public interface CartItemsReactiveRepository extends R2dbcRepository<CartItem, Integer> {
    Flux<CartItem> findAllByCart_Id(Long cart_id);

    Mono<CartItem> findByProduct_IdAndCart_Id(Long productId, Long cartId);

    Mono<Void> deleteByCart_Id(Long cartId);
}
