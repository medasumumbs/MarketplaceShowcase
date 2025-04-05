package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductCacheDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;
import ru.muravin.marketplaceshowcase.repositories.ProductsCacheRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class RedisCacheService {
    private final ReactiveRedisTemplate<String, ProductToUIDto> reactiveRedisTemplate;

    private static final String REDIS_KEY_PRODUCTS = "products";

    @Autowired
    public RedisCacheService(ReactiveRedisTemplate<String, ProductToUIDto> redisTemplate) {
        this.reactiveRedisTemplate = redisTemplate;
    }

    public Mono<Long> setProductsListCache(String nameFilter,
                                           String sort,
                                           Integer limit,
                                           Integer offset,
                                           List<ProductToUIDto> values) {
        ReactiveListOperations<String, ProductToUIDto> listOperations = reactiveRedisTemplate.opsForList();
        var key = getKeyForProductsList(nameFilter, sort, limit, offset);
        return listOperations.leftPushAll(key, values);
    }

    public Flux<ProductToUIDto> getProductsListCache(String nameFilter, String sort, Integer limit, Integer offset) {
        ReactiveListOperations<String, ProductToUIDto> listOperations = reactiveRedisTemplate.opsForList();
        return listOperations.range(getKeyForProductsList(nameFilter, sort, limit, offset), 0, -1);
    }
    private static String getKeyForProductsList(String nameFilter, String sort, Integer limit, Integer offset) {
        return REDIS_KEY_PRODUCTS + ":products_list:" + nameFilter + ":" + limit + ":" + offset + ":" + sort;
    }
}
