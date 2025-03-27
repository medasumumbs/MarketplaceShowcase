package ru.muravin.marketplaceshowcase.serviceTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.marketplaceshowcase.MarketplaceShowcaseApplication;
import ru.muravin.marketplaceshowcase.TestcontainersConfiguration;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.exceptions.UnknownProductException;
import ru.muravin.marketplaceshowcase.models.Product;
import ru.muravin.marketplaceshowcase.services.CartService;
import ru.muravin.marketplaceshowcase.services.ProductsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = MarketplaceShowcaseApplication.class)
public class ProductServiceTest {

    @MockitoBean(reset = MockReset.BEFORE)
    CartService cartService;

    /*@MockitoBean(reset = MockReset.BEFORE)
    ProductsRepository repository;*/

    @Autowired
    private ProductsService productsService;
    @Test
    void testCountAll() {
        int count = 25;
       // when(repository.count()).thenReturn((long) count);
        assertEquals(count, productsService.countAll());
        ///verify(repository, times(1)).count();
    }

    @Test
    void testFindAll() {
        //when(cartService.getCartItems(1l)).thenReturn(List.of());
        PageRequest pageRequest = PageRequest.of(1, 2);
        List<Product> products = List.of(
                new Product(0l,"iphone",25d,"desc",new byte[0]),
                new Product(1l,"iphone",25d,"desc",new byte[0]),
                new Product(2l,"iphone 2",26d,"desc",new byte[0]),
                new Product(3l,"iphone 2",27d,"desc",new byte[0])
        );;
        Page<Product> productPage = new PageImpl<>(products.subList(1,3), pageRequest, products.size());
        //when(repository.findAll(pageRequest)).thenReturn(productPage);
        var realResult = productsService.findAll(pageRequest);
        assertNotNull(realResult);
        var firstDto = new ProductToUIDto(products.get(1));
        firstDto.setQuantityInCart(0);
        var secondDto = new ProductToUIDto(products.get(2));
        secondDto.setQuantityInCart(0);
        assertEquals(realResult.get(0), firstDto);
        assertEquals(realResult.get(1), secondDto);
        //verify(repository, times(1)).findAll(pageRequest);
        //verifyNoMoreInteractions(repository);
        //verify(cartService, times(1)).getCartItems(any(Long.class));
    }
    @Test
    void testSave() {
        var product = new Product(0l,"iphone",25d,"desc",new byte[0]);
        var dto = new ProductToUIDto(product);
        productsService.save(dto);
        //verify(repository, times(1)).save(product);
    }
    @Test
    void testFindById() {
        var product = new Product(1L,"iphone",25d,"desc",new byte[0]);
        //when(repository.findById(1L)).thenReturn(Optional.of(product));
        var result = productsService.findById(1L);
        assertNotNull(result);
        var dto = new ProductToUIDto(product);
        dto.setQuantityInCart(0);
        result.setQuantityInCart(0);
        assertEquals(dto, result);
    }
    @Test
    void testFindByIdNotFound() {
        //when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownProductException.class, () -> productsService.findById(1L));
    }
    @Test
    void testFindByNameLike() {
        var pageable = PageRequest.of(1,2);
        List<Product> products = List.of(
                new Product(0l,"iphone",25d,"desc",new byte[0]),
                new Product(1l,"iphone",25d,"desc",new byte[0]),
                new Product(2l,"iphone 2",26d,"desc",new byte[0]),
                new Product(3l,"iphone 2",27d,"desc",new byte[0])
        );;
        var searchString = "iphone";
       // when(repository.findByNameLike("%"+searchString+"%",pageable)).thenReturn(products.subList(1,3));
       // when(cartService.getCartItems(1L)).thenReturn(new ArrayList<>());
        var result = productsService.findByNameLike(searchString, pageable);
        assertNotNull(result);
      //  verify(repository, times(1)).findByNameLike("%"+searchString+"%", pageable);
       // verifyNoMoreInteractions(repository);
        var expected = products.subList(1,3).stream().map(ProductToUIDto::new).toList();
        expected.forEach(
                a -> a.setQuantityInCart(0)
        );
        assertEquals(expected, result);
    }
}
