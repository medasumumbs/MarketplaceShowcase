package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.muravin.marketplaceshowcase.models.Product;

@Data
@NoArgsConstructor
public class ProductToUIDto {
        private Long id;

        private String name;

        private double price;

        private String description;

        private String imageBase64;

        public ProductToUIDto(Product product) {
                BeanUtils.copyProperties(product, this);
                if (product.getImageBase64()!=null) {
                        this.setImageBase64(new String(product.getImageBase64()));
                }
        }
}
