package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.Product;

@Repository
public interface ProductsReactiveRepository extends R2dbcRepository<Product, Long> {
    @Query("SELECT * from products limit :pageSize offset :offset")
    Flux<Product> findAll(int pageSize, int offset);

    @Query("SELECT * from products order by Name limit :pageSize offset :offset")
    Flux<Product> findAllSortByName(int pageSize, int offset);

    @Query("SELECT * from products order by Price limit :pageSize offset :offset")
    Flux<Product> findAllSortByPrice(int pageSize, int offset);

    @Query("SELECT * from products where upper(name) like concat('%',upper(:name),'%') limit :pageSize offset :offset")
    Flux<Product> findByNameLike(String name, int pageSize, int offset);

    @Query("SELECT * from products where upper(name) like concat('%',upper(:name),'%') order by name limit :pageSize offset :offset")
    Flux<Product> findByNameLikeSortByName(String name, int pageSize, int offset);

    @Query("SELECT * from products where upper(name) like concat('%',upper(:name),'%') order by price limit :pageSize offset :offset")
    Flux<Product> findByNameLikeSortByPrice(String name, int pageSize, int offset);

    Mono<Long> countByNameContainingIgnoreCase(String pattern);
}
