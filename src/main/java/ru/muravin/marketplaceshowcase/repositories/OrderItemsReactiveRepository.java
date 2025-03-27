package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;

import java.util.List;

@Repository
public interface OrderItemsReactiveRepository extends R2dbcRepository<OrderItem, Integer> {
    Flux<OrderItem> findAllByCart_Id(Long cart_id);

    Mono<OrderItem> findByProduct_IdAndCart_Id(Long productId, Long cartId);

    Mono<Void> deleteByCart_Id(Long cartId);

    Flux<OrderItem> findAllByOrder_Id(Long orderId);
}
