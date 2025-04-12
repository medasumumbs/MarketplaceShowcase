package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.User;

@Repository
public interface UserReactiveRepository extends R2dbcRepository<User, Long> {
    Mono<UserDetails> findByUsername(String username);
}
