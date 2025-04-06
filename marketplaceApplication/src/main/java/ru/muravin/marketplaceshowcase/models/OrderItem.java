package ru.muravin.marketplaceshowcase.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.muravin.marketplaceshowcase.dto.CartItemToUIDto;

@Data
@Table("order_products")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    private Long Id;

    @Column("order_id")
    private Long orderId;

    @Column("product_id")
    private Long productId;

    @Column("product_count")
    private Integer quantity;

    @Column("product_price")
    private Double price;

    public OrderItem(CartItem cartItem, Order order, Product product) {
        this.orderId = order.getId();
        this.productId = cartItem.getProductId();
        this.quantity = cartItem.getQuantity();
        this.price = product.getPrice();

    }

    public OrderItem(CartItemToUIDto item, Order order) {
        this.orderId = order.getId();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.productId = item.getProductId();
    }
}
