package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class OrderRequest {
  Customer customer;
  String description;
  Integer amount;
  Address deliveryAddress;
  PaymentDetails paymentDetails;
}
