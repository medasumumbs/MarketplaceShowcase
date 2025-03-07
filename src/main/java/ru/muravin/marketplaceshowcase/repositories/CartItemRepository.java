package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.Cart;
import ru.muravin.marketplaceshowcase.models.CartItem;
import ru.muravin.marketplaceshowcase.models.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    public Optional<CartItem> findByProductAndCart(Product product, Cart cart);

    List<CartItem> findAllByCart(Cart cart);
}
