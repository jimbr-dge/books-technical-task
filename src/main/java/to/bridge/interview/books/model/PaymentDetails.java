package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class PaymentDetails {
  Address billingAddress;
  String currencyCode;
  CardholderDetails cardholderDetails;
}
