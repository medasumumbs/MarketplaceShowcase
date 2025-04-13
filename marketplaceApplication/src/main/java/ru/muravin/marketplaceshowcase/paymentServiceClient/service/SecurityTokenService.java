package ru.muravin.marketplaceshowcase.paymentServiceClient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SecurityTokenService {
    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${paymentsService.registrationId}")
    private String registrationId;

    public Mono<String> getBearerToken() {
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
            OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                    .principal("system")
                    .build()
        );
        try {
            return Mono.just(authorizedClient.getAccessToken().getTokenValue());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Mono.error(new RuntimeException("Ошибка запроса токена: "+e.getMessage()));
        }
    }
}
