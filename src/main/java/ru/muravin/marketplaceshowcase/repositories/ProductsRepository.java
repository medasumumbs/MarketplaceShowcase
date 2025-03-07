package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.models.Product;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameLike(String search, PageRequest pageRequest);

    Long countByNameLike(String search);
}
