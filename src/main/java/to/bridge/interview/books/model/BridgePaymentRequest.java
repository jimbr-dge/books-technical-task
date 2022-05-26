package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class BridgePaymentRequest {
  String channel = "web";
  String currencyCode = "GBP";
  Integer amount;
  Address billingAddress;
  String customerEmail;
  String customerFirstName;
  String customerLastName;
  String customerIpAddress;
  String customerOrderCode;
  String customerPhoneNumber;
  Address deliveryAddress;
  String orderDescription;
  Card paymentInstrument;
}
