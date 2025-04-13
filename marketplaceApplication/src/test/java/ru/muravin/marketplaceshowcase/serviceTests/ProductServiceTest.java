package ru.muravin.marketplaceshowcase.serviceTests;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.repositories.ProductsReactiveRepository;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.CurrentUserService;
import ru.muravin.marketplaceshowcase.services.ProductsService;
import ru.muravin.marketplaceshowcase.services.RedisCacheService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class ProductServiceTest {

    @MockitoBean(reset = MockReset.BEFORE)
    CartService cartService;

    @MockitoBean(reset = MockReset.BEFORE)
    ProductsReactiveRepository productsReactiveRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    RedisCacheService redisCacheService;

    @MockitoBean(reset = MockReset.BEFORE)
    CurrentUserService currentUserService;

    @Autowired
    private ProductsService productsService;
    @Test
    void testCountAll() {
        long count = 25L;
        when(productsReactiveRepository.count()).thenReturn(Mono.just(count));
        when(redisCacheService.getProductsCount(null)).thenReturn(Mono.just(count));
        assertEquals(count, productsService.countAll().block());
        verify(productsReactiveRepository, times(1)).count();

        when(productsReactiveRepository.count()).thenReturn(Mono.just(count));
        when(redisCacheService.getProductsCount(null)).thenReturn(Mono.empty());
        when(redisCacheService.setProductsCount(any(),any())).thenReturn(Mono.just(true));
        assertEquals(count, productsService.countAll().block());
    }

    @Test
    void testFindAll() {
        PageRequest pageRequest = PageRequest.of(1, 2);
        List<Product> products = List.of(
                new Product(0l,"iphone",25d,"desc",new byte[0]),
                new Product(1l,"iphone",25d,"desc",new byte[0]),
                new Product(2l,"iphone 2",26d,"desc",new byte[0]),
                new Product(3l,"iphone 2",27d,"desc",new byte[0])
        );;
        List<Product> subList = products.subList(1,3);
        when(currentUserService.getCurrentUserId()).thenReturn(Mono.just(1l));
        when(productsReactiveRepository
                .findAll(pageRequest.getPageSize(), pageRequest.getPageNumber()*pageRequest.getPageSize()))
                .thenReturn(Flux.fromIterable(subList));
        when(cartService.getFirstCartIdMono()).thenReturn(Mono.just(1l));
        var cartItemFake = new CartItem(1l,1l);
        cartItemFake.setQuantity(0);
        when(cartService.getCartItemsFlux(any(Mono.class))).thenReturn(Flux.just(cartItemFake));
        when(redisCacheService.setProductsListCache(any(),anyString(),anyInt(),anyInt(),any())).thenReturn(Mono.just(2L));
        when(redisCacheService.getProductsListCache(any(),anyString(),anyInt(),anyInt())).thenReturn(Flux.empty());
        var realResult = productsService.findAll(pageRequest.getPageNumber(), pageRequest.getPageSize(), "id").toIterable();
        List<ProductToUIDto> realResultParsed = new ArrayList<>();
        realResult.forEach(realResultParsed::add);
        assertNotNull(realResult);
        var firstDto = new ProductToUIDto(products.get(1));
        firstDto.setQuantityInCart(0);
        var secondDto = new ProductToUIDto(products.get(2));
        secondDto.setQuantityInCart(0);
        assertEquals(realResultParsed.get(0), firstDto);
        assertEquals(realResultParsed.get(1), secondDto);
        verify(productsReactiveRepository, times(1)).findAll(pageRequest.getPageSize(), pageRequest.getPageNumber()*pageRequest.getPageSize());
        verifyNoMoreInteractions(productsReactiveRepository);
        verify(cartService, times(1)).getCartItemsFlux(any(Mono.class));
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testSave() {
        var product = new Product(0l,"iphone",25d,"desc",new byte[0]);
        var dto = new ProductToUIDto(product);
        when(productsReactiveRepository.save(any(Product.class))).thenReturn(Mono.just(product));
        productsService.save(dto).block();
        verify(productsReactiveRepository, times(1)).save(any(Product.class));
    }
    @Test
    @WithAnonymousUser
    void testFindById() {
        when(currentUserService.getCurrentUserId()).thenReturn(Mono.just(1l));
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.just(product));
        when(cartService.getFirstCartIdMono()).thenReturn(Mono.just(1l));
        when(cartService.getCartItemMono(1L, 1L)).thenReturn(Mono.empty());
        when(redisCacheService.getProductCache(product.getId())).thenReturn(Mono.empty());
        when(redisCacheService.setProductCache(any())).thenReturn(Mono.just(true));
        var result = productsService.findById(1L).block();
        assertNotNull(result);
        var dto = new ProductToUIDto(product);
        dto.setQuantityInCart(0);
        assertEquals(dto, result);
    }
    @Test
    void testFindByIdNotFound() {
        when(currentUserService.getCurrentUserId()).thenReturn(Mono.just(1l));
        when(redisCacheService.getProductCache(1L)).thenReturn(Mono.empty());
        when(productsReactiveRepository.findById(1L)).thenReturn(Mono.empty());
        when(cartService.getFirstCartIdMono()).thenReturn(Mono.just(1l));
        when(cartService.getCartItemMono(1L, 1L)).thenReturn(Mono.empty());
        assertThrows(UnknownProductException.class, () -> productsService.findById(1L).block());
    }
    @Test
    void testFindByNameLike() {
        when(currentUserService.getCurrentUserId()).thenReturn(Mono.just(1l));
        var pageable = PageRequest.of(1,2);
        List<Product> products = List.of(
                new Product(0l,"iphone",25d,"desc",new byte[0]),
                new Product(1l,"iphone",25d,"desc",new byte[0]),
                new Product(2l,"iphone 2",26d,"desc",new byte[0]),
                new Product(3l,"iphone 2",27d,"desc",new byte[0])
        );;
        var searchString = "iphone";
        when(productsReactiveRepository.findByNameLike(searchString, pageable.getPageSize(), pageable.getPageSize()*pageable.getPageNumber()))
                .thenReturn(Flux.fromIterable(products.subList(1,3)));
        when(cartService.getFirstCartIdMono()).thenReturn(Mono.just(1l));
        when(cartService.getCartItemsFlux(any(Mono.class))).thenReturn(Flux.empty());
        when(redisCacheService.setProductsListCache(any(String.class),any(String.class),anyInt(),anyInt(),any())).thenReturn(Mono.just(Long.valueOf(2)));
        when(redisCacheService.getProductsListCache(any(String.class),any(String.class),anyInt(),anyInt())).thenReturn(Flux.empty());
        var result = productsService.findByNameLike(searchString, pageable, "id").toIterable();
        var resultParsed = new ArrayList<>();
        result.forEach(resultParsed::add);
        assertNotNull(result);
        verify(productsReactiveRepository, times(1))
                .findByNameLike(searchString, pageable.getPageSize(), pageable.getPageNumber()*pageable.getPageSize());
        verifyNoMoreInteractions(productsReactiveRepository);
        var expected = products.subList(1,3).stream().map(ProductToUIDto::new).toList();
        expected.forEach(
                a -> a.setQuantityInCart(0)
        );
        assertEquals(expected, resultParsed);
    }
}
