package ru.muravin.marketplaceshowcase.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.repositories.UserReactiveRepository;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {
    private final UserReactiveRepository userReactiveRepository;

    public ReactiveUserDetailsServiceImpl(UserReactiveRepository userReactiveRepository) {
        this.userReactiveRepository = userReactiveRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userReactiveRepository.findByUsername(username);
    }
}
