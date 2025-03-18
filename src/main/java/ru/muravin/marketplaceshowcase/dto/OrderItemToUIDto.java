package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.OrderItem;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemToUIDto that = (OrderItemToUIDto) o;
        return Objects.equals(Id, that.Id) && Objects.equals(product, that.product) && Objects.equals(quantity, that.quantity) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, product, quantity, price);
    }
}

