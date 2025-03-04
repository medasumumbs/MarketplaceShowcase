package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
