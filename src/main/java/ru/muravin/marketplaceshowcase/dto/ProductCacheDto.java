package ru.muravin.marketplaceshowcase.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import ru.muravin.marketplaceshowcase.models.Product;

@RedisHash(
        value = "prodRuct"
)
public class ProductCacheDto {
    @Id
    private Long id;

    @Indexed
    private String name;

    private double price;

    private String description;

    private String imageBase64;

    private Integer quantityInCart;

    public ProductCacheDto(ProductToUIDto product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.imageBase64 = product.getImageBase64();
        this.quantityInCart = product.getQuantityInCart();
    }
    public ProductToUIDto toProductToUIDto() {
        ProductToUIDto productToUIDto = new ProductToUIDto();
        productToUIDto.setId(id);
        productToUIDto.setName(name);
        productToUIDto.setPrice(price);
        productToUIDto.setDescription(description);
        productToUIDto.setImageBase64(imageBase64);
        productToUIDto.setQuantityInCart(quantityInCart);
        return productToUIDto;
    }
}
