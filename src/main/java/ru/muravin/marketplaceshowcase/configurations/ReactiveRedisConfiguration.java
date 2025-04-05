package ru.muravin.marketplaceshowcase.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;
import ru.muravin.marketplaceshowcase.dto.ProductCacheDto;
import ru.muravin.marketplaceshowcase.dto.ProductToUIDto;

import java.util.List;

@Configuration
public class ReactiveRedisConfiguration {

    // Настройка ReactiveRedisTemplate
    @Bean
    public ReactiveRedisTemplate<String, ProductToUIDto> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        // Настройка ObjectMapper для работы с JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // (Опционально) Добавление модулей или настройка ObjectMapper
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Создаем сериализатор для списка ProductToUIDto
        var serializer = new Jackson2JsonRedisSerializer<>(objectMapper, ProductToUIDto.class);
        // Настройка контекста сериализации
        RedisSerializationContext<String, ProductToUIDto> serializationContext = RedisSerializationContext
                .<String, ProductToUIDto>newSerializationContext(new StringRedisSerializer())
                .value(serializer) // Используем сериализатор для значений
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
    // Настройка ReactiveRedisTemplate для чисел Long
    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveRedisTemplateForLongValues(ReactiveRedisConnectionFactory factory) {
        var serializer = new GenericToStringSerializer<>(Long.class);
        // Настройка контекста сериализации
        RedisSerializationContext<String, Long> serializationContext = RedisSerializationContext
                .<String, Long>newSerializationContext(new StringRedisSerializer())
                .value(serializer) // Используем сериализатор для значений
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
