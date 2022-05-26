package to.bridge.interview.books;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import to.bridge.interview.books.model.Address;
import to.bridge.interview.books.model.BridgePaymentRequest;
import to.bridge.interview.books.model.BridgePaymentResponse;
import to.bridge.interview.books.model.Book;
import to.bridge.interview.books.model.Card;
import to.bridge.interview.books.model.CardholderDetails;
import to.bridge.interview.books.model.Customer;
import to.bridge.interview.books.model.Order;
import to.bridge.interview.books.model.PaymentDetails;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BooksTest {

  private static final Faker FAKER = new Faker();
  private static SimpleDateFormat cardHolderDataDateFormatter = new SimpleDateFormat("MM-yy");

  private final MockWebServer mockWebServer = new MockWebServer();

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private Books books;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void processBookDataTest() {

    // when

    Book actualResponse = this.restTemplate.getForObject("http://localhost:" + port + "?isbn=1", Book.class);

    // then

    Book expectedResponse = new Book();
    expectedResponse.setIsbn("1");
    expectedResponse.setTitle("The Three Towers");
    expectedResponse.setPrice(5567);

    assertThat(actualResponse)
        .as("The Three Towers should be returned")
        .isEqualTo(expectedResponse);

  }

  @Test
  void processPaymentDataTest() throws InterruptedException, IOException {

    // given a mock BR-DGE REST API that will return a successful response

    BridgePaymentResponse givenBridgePaymentResponse = new BridgePaymentResponse();
    givenBridgePaymentResponse.setPaymentId("example-payment-id");
    givenBridgePaymentResponse.setCode("2000");
    givenBridgePaymentResponse.setMessage("hello world");
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(givenBridgePaymentResponse))
    );
    HttpUrl mockBridgeRestApiPaymentsUrl = mockWebServer.url("/v1/payments");

    // and a Books configured to use the mock BR-DGE REST API

    books.URI = mockBridgeRestApiPaymentsUrl.uri().toString();

    // when we create an order

    Order givenOrder = generateOrder();
    String actualBridgePaymentId = restTemplate.postForObject(
        "http://localhost:" + port,
        givenOrder,
        String.class
    );

    // then

    assertThat(objectMapper.readValue(mockWebServer.takeRequest().getBody().readUtf8(), BridgePaymentRequest.class))
        .as("the expected REST call to BR-DGE should be made")
        .isEqualTo(toBridgePaymentRequest(givenOrder));

    assertThat(actualBridgePaymentId)
        .as("the returned payment-id should match expected from mocked BR-DGE REST API")
        .isEqualTo("payment-id");
  }

  public static BridgePaymentRequest toBridgePaymentRequest(Order givenOrder) {
    BridgePaymentRequest request = new BridgePaymentRequest();
    request.setAmount(givenOrder.getAmount());
    request.setCustomerOrderCode(givenOrder.getId());
    request.setOrderDescription(givenOrder.getDescription());
    request.setPaymentInstrument(generatePaymentInstrument(givenOrder));
    request.setCustomerFirstName(givenOrder.getCustomer().getFirstName());
    request.setCustomerLastName(givenOrder.getCustomer().getLastName());
    request.setCustomerPhoneNumber(givenOrder.getCustomer().getPhoneNumber());
    request.setCustomerEmail(givenOrder.getCustomer().getEmail());
    request.setCustomerIpAddress(givenOrder.getCustomer().getIpAddress());
    request.setDeliveryAddress(givenOrder.getDeliveryAddress());
    request.setBillingAddress(givenOrder.getPaymentDetails().getBillingAddress());
    request.setChannel("web");
    return request;
  }

  private static Card generatePaymentInstrument(Order order) {
    CardholderDetails cardholderDetails = order.getPaymentDetails().getCardholderDetails();
    Card card = new Card();
    card.setPan(cardholderDetails.getPan());
    card.setNameOnCard(cardholderDetails.getNameOnCard());
    card.setExpiryDate(cardholderDetails.getExpiryDate());
    card.setCv2(cardholderDetails.getCv2());
    card.setIssueNumber(cardholderDetails.getIssueNumber());
    card.setStartDate(cardholderDetails.getStartDate());
    return card;
  }

  public static Order generateOrder() {
    Order order = new Order();
    order.setId(UUID.randomUUID().toString());
    order.setAmount(FAKER.number().numberBetween(1000, 10000));
    order.setPaymentDetails(generatePaymentDetails());
    order.setDeliveryAddress(generateAddress());
    order.setDescription(FAKER.lorem().characters(10, 40));
    order.setCustomer(generateCustomer());
    return order;
  }

  private static PaymentDetails generatePaymentDetails() {
    PaymentDetails paymentDetails = new PaymentDetails();
    paymentDetails.setBillingAddress(generateAddress());
    paymentDetails.setCardholderDetails(generateCardholderDetails());
    paymentDetails.setCurrencyCode("GBP");
    return paymentDetails;
  }

  static Customer generateCustomer() {
    Customer customer = new Customer();
    customer.setIpAddress(FAKER.internet().ipV4Address());
    customer.setLastName(FAKER.name().lastName());
    customer.setFirstName(FAKER.name().firstName());
    customer.setEmail(FAKER.internet().emailAddress());
    customer.setPhoneNumber(FAKER.phoneNumber().phoneNumber());
    return customer;
  }

  static CardholderDetails generateCardholderDetails() {
    CardholderDetails cardholderDetails = new CardholderDetails();
    cardholderDetails.setIssueNumber(FAKER.number().numberBetween(1, 5));
    cardholderDetails.setPan(FAKER.business().creditCardNumber());
    cardholderDetails.setStartDate(cardHolderDataDateFormatter.format(FAKER.date().past(360, TimeUnit.DAYS)));
    cardholderDetails.setCv2(FAKER.number().digits(3));
    cardholderDetails.setExpiryDate(cardHolderDataDateFormatter.format(FAKER.date().future(360, TimeUnit.DAYS)));
    cardholderDetails.setNameOnCard(FAKER.name().fullName());
    return cardholderDetails;
  }

  static Address generateAddress() {
    Address address = new Address();
    address.setFirstName(FAKER.name().firstName());
    address.setSurname(FAKER.name().lastName());
    address.setPhoneNumber(FAKER.phoneNumber().phoneNumber());
    address.setBuildingOrName(FAKER.address().buildingNumber());
    address.setAddress1(FAKER.address().streetAddress(false));
    address.setAddress2(FAKER.address().secondaryAddress());
    address.setTown(FAKER.address().city());
    address.setCounty(FAKER.address().state());
    address.setCountry(FAKER.address().country());
    address.setPostcode(FAKER.address().zipCode());
    return address;
  }

}