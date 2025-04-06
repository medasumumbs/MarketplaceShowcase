package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.Cart;

@Repository
public interface CartsReactiveRepository extends R2dbcRepository<Cart, Long> {
}
