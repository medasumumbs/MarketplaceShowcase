package ru.muravin.marketplaceshowcase.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2BackendSecurityConfig {
    @Value("${paymentsService.client}")
    private String client;

    @Value("${paymentsService.secret}")
    private String secret;

    @Value("${paymentsService.scope}")
    private String scope;

    @Value("${paymentsService.registrationId}")
    private String registrationId;

    @Value("${paymentsService.tokenUri}")
    private String tokenUri;


    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository(),authorizedClientService());
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials() // Client Credentials Flow
                .refreshToken()     // Защита от захвата токенов
                .build());
        return manager;
    }


    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId(registrationId)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(tokenUri)
                .scope(scope)
                .clientId(client)
                .clientSecret(secret)
                .build());
    }
}
