package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.muravin.marketplaceshowcase.models.Product;

import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
public class ProductToUIDto {
        private Long id;

        private String name;

        private double price;

        private String description;

        private String imageBase64;

        private Integer quantityInCart;

        public ProductToUIDto(Product product) {
                BeanUtils.copyProperties(product, this);
                if (product.getImageBase64()!=null) {
                        this.setImageBase64(new String(product.getImageBase64()));
                }
        }
        public Product transformToProduct() {
                Product product = new Product();
                BeanUtils.copyProperties(this, product);
                product.setImageBase64(imageBase64.getBytes(StandardCharsets.UTF_8));
                return product;
        }
}
