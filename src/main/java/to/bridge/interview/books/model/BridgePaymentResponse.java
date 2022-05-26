package to.bridge.interview.books.model;

import java.util.Set;
import lombok.Data;

@Data
public class BridgePaymentResponse {
  String code;
  String id;
  String message;
  String paymentId;
  Set<String> validationErrors;
}
