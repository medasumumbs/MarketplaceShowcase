package ru.muravin.marketplaceshowcase.paymentServiceClient.api;

import org.springframework.stereotype.Component;
import ru.muravin.marketplaceshowcase.paymentServiceClient.server.ApiClient;

import ru.muravin.marketplaceshowcase.paymentServiceClient.model.Balance;
import ru.muravin.marketplaceshowcase.paymentServiceClient.model.PaymentResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.paymentServiceClient.service.SecurityTokenService;

@Component
public class PaymentServiceClientAPI {

    private ApiClient apiClient;
    private SecurityTokenService securityTokenService;

    public PaymentServiceClientAPI() {
        this(new ApiClient());
    }

    @Autowired
    public PaymentServiceClientAPI(ApiClient apiClient, SecurityTokenService securityTokenService) {
        this.apiClient = apiClient;
        this.securityTokenService = securityTokenService;
    }

    public PaymentServiceClientAPI(ApiClient apiClient) {
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выяснении баланса
     * @param userId Идентификатор пользователя
     * @return Balance
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec usersUserIdGetRequestCreation(Integer userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling usersUserIdGet", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        System.out.println("bearerToken!!!: "+ securityTokenService.getBearerToken().block());
        headerParams.add("Authorization","Bearer "+ securityTokenService.getBearerToken().block());

        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        return apiClient.invokeAPI("/users/{userId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выяснении баланса
     * @param userId Идентификатор пользователя
     * @return Balance
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Balance> usersUserIdGet(Integer userId) throws WebClientResponseException {
        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        var emptyBalance = new Balance();
        emptyBalance.setBalance(0f);
        var errorBalance = new Balance();
        errorBalance.setBalance(-1f);
        return usersUserIdGetRequestCreation(userId).bodyToMono(localVarReturnType).flatMap(body1->{
                    System.out.println("get balance response: "+body1);
            return Mono.just(body1);
        }).onErrorResume(e-> {
            System.out.println(e.getMessage());
            return Mono.just(errorBalance);
        })
        .switchIfEmpty(Mono.just(emptyBalance));
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выяснении баланса
     * @param userId Идентификатор пользователя
     * @return ResponseEntity&lt;Balance&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Balance>> usersUserIdGetWithHttpInfo(Integer userId) throws WebClientResponseException {
        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        return usersUserIdGetRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выяснении баланса
     * @param userId Идентификатор пользователя
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec usersUserIdGetWithResponseSpec(Integer userId) throws WebClientResponseException {
        return usersUserIdGetRequestCreation(userId);
    }

    /**
     * Совершить платеж (совершить списание денег со счёта)
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>400</b> - Превышена максимально возможная сумма платежа
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выполнении платежа
     * @param sum The sum parameter
     * @param userId Идентификатор пользователя
     * @return PaymentResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec usersUserIdMakePaymentPostRequestCreation(Float sum, Integer userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sum' is set
        if (sum == null) {
            throw new WebClientResponseException("Missing the required parameter 'sum' when calling usersUserIdMakePaymentPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling usersUserIdMakePaymentPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sum", sum));
        
        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };
        headerParams.add("Authorization","Bearer "+ securityTokenService.getBearerToken().block());
        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return apiClient.invokeAPI("/users/{userId}/makePayment", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Совершить платеж (совершить списание денег со счёта)
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>400</b> - Превышена максимально возможная сумма платежа
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выполнении платежа
     * @param sum The sum parameter
     * @param userId Идентификатор пользователя
     * @return PaymentResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PaymentResponse> usersUserIdMakePaymentPost(Float sum, Integer userId) throws WebClientResponseException {
        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return usersUserIdMakePaymentPostRequestCreation(sum, userId).bodyToMono(localVarReturnType).flatMap(body->{
            System.out.println("RESPONSE BODY: " + body);
            return Mono.just(body);
        });
    }

    /**
     * Совершить платеж (совершить списание денег со счёта)
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>400</b> - Превышена максимально возможная сумма платежа
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выполнении платежа
     * @param sum The sum parameter
     * @param userId Идентификатор пользователя
     * @return ResponseEntity&lt;PaymentResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PaymentResponse>> usersUserIdMakePaymentPostWithHttpInfo(Float sum, Integer userId) throws WebClientResponseException {
        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return usersUserIdMakePaymentPostRequestCreation(sum, userId).toEntity(localVarReturnType);
    }

    /**
     * Совершить платеж (совершить списание денег со счёта)
     * 
     * <p><b>200</b> - Успешный ответ
     * <p><b>400</b> - Превышена максимально возможная сумма платежа
     * <p><b>404</b> - Пользователь не найден
     * <p><b>5XX</b> - Внутренняя ошибка сервера
     * <p><b>0</b> - Неизвестная ошибка при выполнении платежа
     * @param sum The sum parameter
     * @param userId Идентификатор пользователя
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec usersUserIdMakePaymentPostWithResponseSpec(Float sum, Integer userId) throws WebClientResponseException {
        return usersUserIdMakePaymentPostRequestCreation(sum, userId);
    }
}
