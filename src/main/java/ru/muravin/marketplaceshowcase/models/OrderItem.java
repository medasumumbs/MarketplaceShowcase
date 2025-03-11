package ru.muravin.marketplaceshowcase.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "order_products")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_seq")
    @SequenceGenerator(name = "cart_seq", sequenceName = "cart_sequence", allocationSize = 1)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "product_count")
    private Integer quantity;

    @Column(name = "product_price")
    private Double price;

    public OrderItem(CartItem cartItem, Order order) {
        this.order = order;
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getProduct().getPrice();

    }
}
