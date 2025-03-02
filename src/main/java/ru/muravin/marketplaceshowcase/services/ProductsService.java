package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductsService {
    ProductsRepository repository;
    @Autowired
    public ProductsService(ProductsRepository repository) {
        this.repository = repository;
    }
    public List<ProductToUIDto> findAll(PageRequest pageRequest) {
        return repository.findAll(pageRequest)
                .stream().map(ProductToUIDto::new).collect(Collectors.toList());
    }
    public Long countAll() {
        return repository.count();
    }
}
