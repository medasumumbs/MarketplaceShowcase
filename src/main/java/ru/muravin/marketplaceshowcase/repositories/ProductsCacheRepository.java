package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

public interface ProductsCacheRepository extends CrudRepository<ProductToUIDto, Long> {
}
