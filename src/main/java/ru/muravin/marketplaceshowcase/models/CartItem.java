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
@Table(name = "cart_products")
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_product_seq")
    @SequenceGenerator(name = "cart_product_seq", sequenceName = "cart_product_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "product_count")
    private Integer quantity;


    public CartItem(Product product, Cart cart) {
        this.product = product;
        this.cart = cart;
        this.quantity = 1;
    }
}
