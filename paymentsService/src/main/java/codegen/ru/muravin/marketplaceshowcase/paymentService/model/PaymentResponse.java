package codegen.ru.muravin.marketplaceshowcase.paymentService.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PaymentResponse
 */
public class PaymentResponse {

  private @Nullable Integer restBalance;

  private @Nullable String message;

  public PaymentResponse restBalance(Integer restBalance) {
    this.restBalance = restBalance;
    return this;
  }

  /**
   * Остаточный баланс
   * @return restBalance
   */
  
  @Schema(name = "restBalance", description = "Остаточный баланс", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("restBalance")
  public Integer getRestBalance() {
    return restBalance;
  }

  public void setRestBalance(Integer restBalance) {
    this.restBalance = restBalance;
  }

  public PaymentResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Описание статуса ответа
   * @return message
   */
  
  @Schema(name = "message", example = "OK", description = "Описание статуса ответа", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentResponse paymentResponse = (PaymentResponse) o;
    return Objects.equals(this.restBalance, paymentResponse.restBalance) &&
        Objects.equals(this.message, paymentResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(restBalance, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentResponse {\n");
    sb.append("    restBalance: ").append(toIndentedString(restBalance)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

