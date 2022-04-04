package rangemap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd"
  );

  public static Date fromString(String dateString) throws ParseException {
    return DATE_FORMAT.parse(dateString);
  }
}
