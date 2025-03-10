package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;

@Data
@NoArgsConstructor
public class CartItemToUIDto {
    private Long Id;

    private Cart cart;

    private ProductToUIDto product;

    private Integer quantity;

    public CartItemToUIDto(CartItem cartItem) {
        BeanUtils.copyProperties(cartItem, this);
        this.product = new ProductToUIDto(cartItem.getProduct());
    }
}
