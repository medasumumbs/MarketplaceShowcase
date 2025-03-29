package ru.muravin.marketplaceshowcase.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    private Long id;
    
    private Long userId;
}

