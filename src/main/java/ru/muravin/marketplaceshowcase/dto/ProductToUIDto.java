package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import ru.muravin.marketplaceshowcase.models.Product;

@Data
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
