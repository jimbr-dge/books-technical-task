package to.bridge.interview.books.model;

import lombok.Data;

@Data
public class CardholderDetails {
  String pan;
  String cv2;
  String nameOnCard;
  String expiryDate;
  Integer issueNumber;
  String startDate;
}
