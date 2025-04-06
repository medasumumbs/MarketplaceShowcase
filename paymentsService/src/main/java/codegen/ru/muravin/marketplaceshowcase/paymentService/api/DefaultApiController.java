package codegen.ru.muravin.marketplaceshowcase.paymentService.api;

import codegen.ru.muravin.marketplaceshowcase.paymentService.model.Balance;
import codegen.ru.muravin.marketplaceshowcase.paymentService.model.PaymentResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class DefaultApiController implements DefaultApi {
    @Override
    public Mono<ResponseEntity<Balance>> usersUserIdGet(Integer userId, ServerWebExchange exchange) {
        if (userId == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        if (userId != 1) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        Balance balance = new Balance();
        balance.setBalance(10000.25f);
        return Mono.just(ResponseEntity.ok().body(balance));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> usersUserIdMakePaymentPost(Float sum, Integer userId, ServerWebExchange exchange) {
        if (userId == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        if (userId != 1) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setMessage("OK");
        var restBalance = 10000.25f - sum;
        paymentResponse.setRestBalance(restBalance);
        if (restBalance < 0) {
            paymentResponse.setMessage("Превышена максимально возможная сумма платежа");
            return Mono.just(ResponseEntity.badRequest().body(paymentResponse));
        }
        return Mono.just(ResponseEntity.ok().body(paymentResponse));
    }
}
