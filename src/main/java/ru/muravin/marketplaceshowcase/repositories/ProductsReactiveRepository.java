package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.Product;

@Repository
public interface ProductsReactiveRepository extends R2dbcRepository<Product, Long> {
}
