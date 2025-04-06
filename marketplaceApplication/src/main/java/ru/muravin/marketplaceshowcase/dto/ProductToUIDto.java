package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.muravin.marketplaceshowcase.models.Product;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Objects;

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

        public ProductToUIDto(LinkedHashMap<Object, Object> eachItem) {
                var productToUIDto = new ProductToUIDto();
                productToUIDto.setId(((Integer) eachItem.get("id")).longValue());
                productToUIDto.setName((String)eachItem.get("name"));
                productToUIDto.setDescription((String)eachItem.get("description"));
                productToUIDto.setPrice((Double) eachItem.get("price"));
                productToUIDto.setQuantityInCart((Integer) eachItem.get("quantityInCart"));
                productToUIDto.setImageBase64((String) eachItem.get("imageBase64"));
        }

        public Product transformToProduct() {
                Product product = new Product();
                BeanUtils.copyProperties(this, product);
                product.setImageBase64(imageBase64.getBytes(StandardCharsets.UTF_8));
                return product;
        }

        @Override
        public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                ProductToUIDto product = (ProductToUIDto) o;
                return Double.compare(price, product.price) == 0 && Objects.equals(id, product.id) && Objects.equals(name, product.name) && Objects.equals(description, product.description) && Objects.equals(imageBase64, product.imageBase64) && Objects.equals(quantityInCart, product.quantityInCart);
        }

        @Override
        public int hashCode() {
                return Objects.hash(id, name, price, description, imageBase64, quantityInCart);
        }
}
