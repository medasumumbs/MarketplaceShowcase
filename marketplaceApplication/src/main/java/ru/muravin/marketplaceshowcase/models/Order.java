package ru.muravin.marketplaceshowcase.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private Long id;

    private Long userId;

    private LocalDateTime orderDate;
}

