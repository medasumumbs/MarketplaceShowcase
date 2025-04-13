package ru.muravin.marketplaceshowcase.services;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.User;

@Service
public class CurrentUserService {
    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
            var user = securityContext.getAuthentication().getPrincipal();
            if (!(user instanceof User)) {
                return 0l;
            }
            return ((User) user).getId();
        }).defaultIfEmpty(0L);
    }

    public Mono<Boolean> isCurrentUserAdmin() {
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
            var auth = securityContext.getAuthentication();
            if (auth == null) {
                return false;
            }
            var adminAuthority = auth.getAuthorities()
                    .stream().filter(authority -> authority.getAuthority().equals("ROLE_ADMIN")).findFirst();
            return adminAuthority.isPresent();
        }).switchIfEmpty(Mono.just(false));
    }
}
