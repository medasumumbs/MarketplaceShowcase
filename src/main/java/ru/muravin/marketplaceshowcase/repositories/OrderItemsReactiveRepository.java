package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.OrderItemToUIDto;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;

import java.util.List;

@Repository
public interface OrderItemsReactiveRepository extends R2dbcRepository<OrderItem, Long> {
    @Query("select products.image_base64 as image_base_64, * from order_products join products on order_products.product_id=products.id  where order_id = :orderId")
    Flux<OrderItemToUIDto> findAllByOrder_Id(Long orderId);
}
