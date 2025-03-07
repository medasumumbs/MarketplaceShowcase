package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.ProductsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
        enrichDtoListWithCartQuantities(dtoList, cartService.getCartItems(1l));
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
        enrichDtoListWithCartQuantities(dtoList, cartService.getCartItems(1l));
        return dtoList;
    }
    public Long countByNameLike(String search) {
        return repository.countByNameLike('%' + search + '%');
    }

    private void enrichDtoListWithCartQuantities(List<ProductToUIDto> dtoList, List<CartItem> cartItems) {
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
    public ProductToUIDto findById(Long id) {
        var product = repository.findById(id);
        var dto = new ProductToUIDto(product.orElseThrow(()->new UnknownProductException("Product "+id+" not found")));
        enrichDtoListWithCartQuantities(List.of(dto), cartService.getCartItems(1l));
        return dto;
    }
}
