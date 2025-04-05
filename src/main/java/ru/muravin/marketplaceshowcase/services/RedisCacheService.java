package ru.muravin.marketplaceshowcase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import java.time.Duration;
import java.util.List;

@Service
public class RedisCacheService {
    private final ReactiveRedisTemplate<String, ProductToUIDto> reactiveRedisTemplate;

    private final ReactiveRedisTemplate<String, Long> reactiveRedisTemplateForLongValues;

    private static final String REDIS_KEY_PRODUCTS = "products";

    private static final Duration REDIS_KEY_PRODUCTS_DURATION = Duration.ofSeconds(30);

    @Autowired
    public RedisCacheService(ReactiveRedisTemplate<String, ProductToUIDto> redisTemplate, ReactiveRedisTemplate<String, Long> reactiveRedisTemplateForLongValues) {
        this.reactiveRedisTemplate = redisTemplate;
        this.reactiveRedisTemplateForLongValues = reactiveRedisTemplateForLongValues;
    }

    public Mono<Long> setProductsListCache(String nameFilter,
                                           String sort,
                                           Integer limit,
                                           Integer offset,
                                           List<ProductToUIDto> values) {
        if (values == null || values.isEmpty()) return Mono.empty();
        ReactiveListOperations<String, ProductToUIDto> listOperations = reactiveRedisTemplate.opsForList();
        var key = getKeyForProductsList(nameFilter, sort, limit, offset);
        return listOperations.leftPushAll(key, values).flatMap(a -> {
            reactiveRedisTemplate.expire(key, REDIS_KEY_PRODUCTS_DURATION).block();
            return Mono.just(a);
        });
    }

    public Mono<Boolean> setProductCache(ProductToUIDto product) {
        return reactiveRedisTemplate.opsForValue().set(
                getKeyForProduct(String.valueOf(product.getId())), product, REDIS_KEY_PRODUCTS_DURATION
        );
    }

    public Mono<ProductToUIDto> getProductCache(Long productId) {
        return reactiveRedisTemplate.opsForValue().get(getKeyForProduct(String.valueOf(productId)));
    }

    public Flux<ProductToUIDto> getProductsListCache(String nameFilter, String sort, Integer limit, Integer offset) {
        ReactiveListOperations<String, ProductToUIDto> listOperations = reactiveRedisTemplate.opsForList();
        return listOperations.range(getKeyForProductsList(nameFilter, sort, limit, offset), 0, -1);
    }
    public Mono<Boolean> setProductsCount(String nameFilter, Long value) {
        var opsForValue = reactiveRedisTemplateForLongValues.opsForValue();
        return opsForValue.set(getKeyForProductsCount(nameFilter), value, REDIS_KEY_PRODUCTS_DURATION);
    }
    public Mono<Long> getProductsCount(String nameFilter) {
        var opsForValue = reactiveRedisTemplateForLongValues.opsForValue();
        return opsForValue.get(getKeyForProductsCount(nameFilter));
    }

    private String getKeyForProductsCount(String nameFilter) {
        return REDIS_KEY_PRODUCTS + ":products_count:" + nameFilter;
    }

    private static String getKeyForProductsList(String nameFilter, String sort, Integer limit, Integer offset) {
        return REDIS_KEY_PRODUCTS + ":products_list:" + nameFilter + ":" + limit + ":" + offset + ":" + sort;
    }
    private String getKeyForProduct(String productId) {
        return REDIS_KEY_PRODUCTS + ":product:" + productId;
    }

    public Mono<Long> evictCache() {
        return reactiveRedisTemplate.keys("*").flatMap(reactiveRedisTemplate::delete).count();
    }
}
