package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;
import ru.muravin.marketplaceshowcase.models.CartItem;

@Repository
public interface CartItemsReactiveRepository extends R2dbcRepository<CartItem, Integer> {
    @Query("select * from cart_products join products on cart_products.product_id=products.id  where cart_id = :cart_id")
    Flux<CartItemToUIDto> findAllByCart_Id(Long cart_id);

    Mono<CartItem> findByProductIdAndCartId(Long productId, Long cartId);

    Mono<Void> deleteByCartId(Long cartId);
}
