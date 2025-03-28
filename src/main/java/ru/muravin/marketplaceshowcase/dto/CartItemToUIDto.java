package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeanUtils;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;

@Data
@NoArgsConstructor
public class CartItemToUIDto {
    private Long Id;

    private Cart cart;

    private Long cartId;

    private ProductToUIDto product;

    private Integer quantity;

    public CartItemToUIDto(CartItem cartItem, ProductToUIDto product) {
        BeanUtils.copyProperties(cartItem, this);
        this.product = product;
    }

    public CartItem toCartItem() {
        CartItem cartItem = new CartItem();
        BeanUtils.copyProperties(this, cartItem);
        cartItem.setCartId(cart.getId());
        cartItem.setProductId(product.getId());
        return cartItem;
    }
}
