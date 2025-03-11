package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.OrderItem;

@Data
@NoArgsConstructor
public class OrderItemToUIDto {
    private Long Id;

    private OrderToUIDto order;

    private ProductToUIDto product;

    private Integer quantity;

    private Double price;

    public OrderItemToUIDto(OrderItem orderItem, OrderToUIDto order) {
        this.Id = orderItem.getId();
        this.order = order;
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
        this.product = new ProductToUIDto(orderItem.getProduct());
    }
}
