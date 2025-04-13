package ru.muravin.marketplaceshowcase.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.services.ReactiveUserDetailsServiceImpl;

import java.net.URI;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl
    ) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsServiceImpl);

        manager.setPasswordEncoder(encoder());
        return manager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveAuthenticationManager authenticationManager) {
        AuthenticationWebFilter authFilter = new AuthenticationWebFilter(authenticationManager);
        authFilter.setServerAuthenticationConverter(new ServerHttpBasicAuthenticationConverter());

        return http
            .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .formLogin(Customizer.withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .logout(logoutSpec ->
                logoutSpec.logoutSuccessHandler(new ServerLogoutSuccessHandler() {
                    @Override
                    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
                        ServerHttpResponse response = exchange.getExchange().getResponse();
                        response.setStatusCode(HttpStatus.FOUND);
                        response.getHeaders().setLocation(URI.create("/login?logout"));
                        response.getCookies().remove("JSESSIONID");
                        return exchange.getExchange().getSession()
                                .flatMap(WebSession::invalidate);
                    }
                })
            )
            .authorizeExchange(exchange->{
                exchange.pathMatchers("/cart").authenticated()
                        .pathMatchers("/cart/**").authenticated()
                        .pathMatchers("/orders/**").authenticated()
                        .pathMatchers("/logout", "/login").permitAll()
                        .pathMatchers("/orders").authenticated()
                        .pathMatchers("/products/uploadCSV").hasRole("ADMIN")
                        .pathMatchers("/products/changeCartItemQuantity/**").authenticated()
                        .pathMatchers("products/cart/changeCartItemQuantity/**").authenticated()
                        .pathMatchers("/products/cart/**").authenticated()
                        .pathMatchers("/products/{id}/changeCartItemQuantity/**").authenticated()
                        .anyExchange().permitAll();
            })
            .securityContextRepository(new WebSessionServerSecurityContextRepository())
            .build();
    }
}
