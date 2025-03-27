package ru.muravin.marketplaceshowcase.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Entity
@Table("carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;
}

