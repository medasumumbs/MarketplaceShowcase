package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
