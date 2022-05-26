package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class Book {
  String id;
  String isbn;
  String title;
  Integer price;
}
