package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.muravin.marketplaceshowcase.models.OrderItem;

import java.util.Objects;

@Data
@NoArgsConstructor
public class OrderItemToUIDto {
    private Long Id;

    private OrderToUIDto order;

    private ProductToUIDto product;

    private String productName;

    private String productDescription;

    private byte[] imageBase64;

    private Long productId;

    private Integer quantity;

    private Double price;

    private String name;

    private String description;

    private String base64Image;


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

