package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.Product;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Long> {
}
