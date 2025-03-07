package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;

import java.util.HashMap;
import java.util.List;

@Service
public class ProductsService {
    private final CartService cartService;
    ProductsRepository repository;
    @Autowired
    public ProductsService(ProductsRepository repository, CartService cartService) {
        this.repository = repository;
        this.cartService = cartService;
    }
    public List<ProductToUIDto> findAll(PageRequest pageRequest) {
        var products = repository.findAll(pageRequest);
        var dtoList = products.stream().map(ProductToUIDto::new).toList();
        enrichDtoListWithCartQuantities(dtoList);
        return dtoList;
    }
    public void save(ProductToUIDto productToUIDto) {
        repository.save(productToUIDto.transformToProduct());
    }
    public Long countAll() {
        return repository.count();
    }

    public List<ProductToUIDto> findByNameLike(String search, PageRequest pageRequest) {
        var products = repository.findByNameLike('%' + search + '%', pageRequest);
        var dtoList = products.stream().map(ProductToUIDto::new).toList();
        enrichDtoListWithCartQuantities(dtoList);
        return dtoList;
    }
    public Long countByNameLike(String search) {
        return repository.countByNameLike('%' + search + '%');
    }

    private void enrichDtoListWithCartQuantities(List<ProductToUIDto> dtoList) {
        var cartItems = cartService.getCartItems(cartService.getCartById(1L));
        var productsMap = new HashMap<>();
        dtoList.forEach(productToUIDto -> {
            productsMap.put(productToUIDto.getId(), productToUIDto);
        });
        dtoList.forEach(productToUIDto -> {
            productToUIDto.setQuantityInCart(0);
        });
        cartItems.forEach(cartItem -> {
            if (productsMap.containsKey(cartItem.getProduct().getId())) {
                ((ProductToUIDto)productsMap.get(cartItem.getProduct().getId())).setQuantityInCart(cartItem.getQuantity());
            }
        });
    }
}
