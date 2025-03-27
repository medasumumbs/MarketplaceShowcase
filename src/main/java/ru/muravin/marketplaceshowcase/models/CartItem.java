package ru.muravin.marketplaceshowcase.models;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("cart_products")
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    private Long Id;

    @Column("cart_id")
    private Long cartId;

    @Column("product_id")
    private Long productId;

    @Column("product_count")
    private Integer quantity;

    public CartItem(Product product, Cart cart) {
        this.productId = product.getId();
        this.cartId = cart.getId();
        this.quantity = 1;
    }

    public CartItem(Long productId, Long cartId) {
        this.productId = productId;
        this.cartId = cartId;
        this.quantity = 1;
    }
}
