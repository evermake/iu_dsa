package common;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for {@code java.util.Calendar} with all necessary methods
 * for solving tasks.
 */
public class Date implements Comparable<Date> {
  private final static Pattern DATE_PATTERN = Pattern.compile(
      "^(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})$"
  );
  private final Calendar calendarDate;

  public Date(Calendar calendarDate) {
    this.calendarDate = calendarDate;
  }

  /**
   * Creates a date from string.
   *
   * @param dateString date string in format <b>"YYYY-MM-DD"</b>
   *
   * @return parsed date
   *
   * @throws IllegalArgumentException if format of the string is wrong
   */
  public static Date fromString(String dateString) throws IllegalArgumentException {
    Matcher dateMatcher = DATE_PATTERN.matcher(dateString);

    if (!dateMatcher.matches()) {
      throw new IllegalArgumentException("invalid date format");
    }

    int year = Integer.parseInt(dateMatcher.group("year"));
    int month = Integer.parseInt(dateMatcher.group("month"));
    int day = Integer.parseInt(dateMatcher.group("day"));

    return new Date(
        new GregorianCalendar(
            year,
            // Subtract one from month, because
            // GregorianCalendar takes month in range 0-11
            month - 1,
            day
        )
    );
  }

  @Override
  public int compareTo(Date other) {
    return this.calendarDate.compareTo(other.calendarDate);
  }

  /**
   * @return integer representation of the date where:
   *     <ol>
   *       <li>first two digits will represent a day;</li>
   *       <li>next one or two digits will represent a month;</li>
   *       <li>remaining digits will represent a year.</li>
   *     </ol>
   */
  public int toInt() {
    return (
        calendarDate.get(Calendar.DATE)
            + (calendarDate.get(Calendar.MONTH) + 1) * 100
            + calendarDate.get(Calendar.YEAR) * 10000
    );
  }

  /**
   * @return difference between two dates in days
   */
  public long difference(Date other) {
    return ChronoUnit.DAYS.between(
        calendarDate.toInstant(),
        other.calendarDate.toInstant()
    );
  }
}
