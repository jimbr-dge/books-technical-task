package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class Card {
  String type = "card";
  String nameOnCard;
  String pan;
  String expiryDate;
  String startDate;
  Integer issueNumber;
  String cv2;
}
