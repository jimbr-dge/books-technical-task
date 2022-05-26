package to.bridge.interview.books;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import to.bridge.interview.books.model.Card;
import to.bridge.interview.books.model.Book;
import to.bridge.interview.books.model.CardholderDetails;
import to.bridge.interview.books.model.Order;
import to.bridge.interview.books.model.BridgePaymentRequest;
import to.bridge.interview.books.model.BridgePaymentResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class Books {

  public String URI = "https://sandbox.comcarde.com/v1/payments";

  @GetMapping
  public Book processBookData(@RequestParam("isbn") String isbn) throws ClassNotFoundException, SQLException {
      Class.forName("org.h2.Driver");
      System.out.println("Connecting to database...");
      Connection conn = DriverManager.getConnection("jdbc:h2:mem:mydb","sa","password");
      System.out.println("Connected database successfully...");
      Statement stmt = conn.createStatement();
      String sql = "SELECT isbn, title, price FROM books WHERE isbn = '" + isbn + "';";
      System.out.println("executing query: " + sql);
      ResultSet rs = stmt.executeQuery(sql);
      rs.next();
      Book book = new Book();
      book.setIsbn(isbn);
      book.setPrice(rs.getInt("price"));
      book.setTitle(rs.getString("title"));
      rs.close();
      System.out.println("returning book " + book);
      return book;
  }

  @PostMapping
  public String processOrderData(@RequestBody Order order) {
    String orderId = UUID.randomUUID().toString();
    System.out.println("assigning id " + orderId + " to order " + order);
    BridgePaymentRequest bridgePaymentRequest = toBridgePaymentRequest(order);
    System.out.println("converted order to BR-DGE payment request " + bridgePaymentRequest);
    String paymentId = WebClient.builder().build()
        .post()
        .uri(URI)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRob3JpdGllcyI6WyJQQVlNRU5UX0NSRUFUSU9OIiwiUkVGVU5EX0NSRUFUSU9OIiwiVE9LRU5fUkVUUklFVkFMIl0sInN1YiI6InB1YmxpY01lcmNoYW50QWNjb3VudCJ9.RTqsBlB6mdMjf-BJOTNzgV8vYPT30RmuVOC9j8LPEQQ")
        .body(Mono.just(bridgePaymentRequest), BridgePaymentRequest.class)
        .retrieve()
        .toEntity(BridgePaymentResponse.class)
        .block()
        .getBody()
        .getPaymentId();
    System.out.println("returning payment id " + paymentId);
    return paymentId;
  }

  private BridgePaymentRequest toBridgePaymentRequest(Order order) {
    BridgePaymentRequest request = new BridgePaymentRequest();
        request.setAmount(order.getAmount());
    request.setBillingAddress(order.getPaymentDetails().getBillingAddress());
    request.setCustomerFirstName(order.getCustomer().getEmail());
    request.setCustomerLastName(order.getCustomer().getLastName());
    request.setCustomerIpAddress(order.getCustomer().getIpAddress());
    request.setCustomerOrderCode(order.getId());
    request.setCustomerPhoneNumber(order.getCustomer().getPhoneNumber());
    request.setDeliveryAddress(order.getDeliveryAddress());
    request.setOrderDescription(order.getDescription());
    request.setPaymentInstrument(toBridgeCardPaymentInstrument(order));
    return request;
  }

  private Card toBridgeCardPaymentInstrument(Order order) {
    CardholderDetails cardholderDetails = order.getPaymentDetails().getCardholderDetails();
    Card card = new Card();
    card.setPan(cardholderDetails.getPan());
    card.setNameOnCard(cardholderDetails.getNameOnCard());
    card.setExpiryDate(cardholderDetails.getExpiryDate());
    card.setCv2(cardholderDetails.getCv2());
    card.setStartDate(cardholderDetails.getStartDate());
    card.setIssueNumber(cardholderDetails.getIssueNumber());
    return card;
  }

}
