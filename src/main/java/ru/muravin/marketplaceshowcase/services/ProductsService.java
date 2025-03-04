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
    public void save(ProductToUIDto productToUIDto) {
        repository.save(productToUIDto.transformToProduct());
    }
    public Long countAll() {
        return repository.count();
    }

    public List<ProductToUIDto> findByNameLike(String search, PageRequest pageRequest) {
        return repository.findByNameLike('%' + search + '%', pageRequest);
    }
    public Long countByNameLike(String search) {
        return repository.countByNameLike('%' + search + '%');
    }
}
