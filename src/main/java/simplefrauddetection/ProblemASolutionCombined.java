/**
 * Created by Vladislav Deryabkin
 */

import java.util.Queue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.ToIntFunction;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Solution for the problem A (simple fraud detection).
 */
public class ProblemASolutionCombined {
  private static final Pattern SPENDING_PATTERN = Pattern.compile(
      "^(?<date>\\d{4}-\\d{2}-\\d{2}) \\$(?<amount>\\d+(?:\\.\\d+)?)$"
  );

  /**
   * Parses spending from the string.
   *
   * @param spending spending as String
   *
   * @return parsed {@link Spending}
   *
   * @throws InvalidInputException if given {@code spending} is in incorrect format
   */
  private static Spending parseSpending(String spending) throws InvalidInputException {
    Matcher spendingMatcher = SPENDING_PATTERN.matcher(spending);

    if (!spendingMatcher.matches()) {
      throw new InvalidInputException();
    }

    return new Spending(
        spendingMatcher.group("date"),
        Double.parseDouble(spendingMatcher.group("amount"))
    );
  }

  public static void main(String[] args) throws InvalidInputException {
    Scanner scanner = new Scanner(System.in);
    int recordsCount = scanner.nextInt();
    int trailingDaysCount = scanner.nextInt();
    scanner.nextLine();

    List<Spending> spendings = new LinkedList<>();

    for (int i = 0; i < recordsCount; i++) {
      spendings.add(parseSpending(scanner.nextLine()));
    }

    FraudDetector fraudDetector = new FraudDetector(trailingDaysCount);
    RadixSort.sort(spendings, s -> s.getDate().toInt());

    for (Spending spending : spendings) {
      fraudDetector.recordSpending(spending);
    }

    System.out.println(fraudDetector.getAlertsCount());
  }

  private static final class InvalidInputException extends Exception {
  }
}


/**
 * Wrapper for {@code java.util.Calendar} with all necessary methods
 * for solving tasks.
 */
class Date implements Comparable<Date> {
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

  /**
   * @return date as string in format YYYY-MM-DD
   */
  public String toString() {
    return String.format("%04d", (calendarDate.get(Calendar.YEAR))) + "-"
        + String.format("%02d", calendarDate.get(Calendar.MONTH) + 1) + "-"
        + String.format("%02d", calendarDate.get(Calendar.DATE));
  }
}


/**
 * Data class for storing spending with date and amount.
 */
class Spending {
  private final Date date;
  private final double amount;

  public Spending(Date date, double amount) {
    this.date = date;
    this.amount = amount;
  }

  /**
   * Shortcut for {@code new Spending(Date.fromString(dateString), amount)}.
   *
   * @param dateString date in format "YYYY-MM-DD"
   * @param amount     spending amount
   */
  public Spending(String dateString, double amount) {
    this(Date.fromString(dateString), amount);
  }

  public Date getDate() {
    return date;
  }

  public double getAmount() {
    return amount;
  }

  public String asRawInput() {
    return date.toString() + " $" + amount;
  }
}


/**
 * Class with radix sorting algorithm implementation.
 * <p>
 * <b><i>Note:</i></b> only suitable for sorting items that
 * can be represented as <b>positive integers.</b>
 */
class RadixSort {
  /**
   * Sorts a list of items of any type (by getting their positive
   * integer representations) using radix algorithm.
   *
   * @param list              list to sort
   * @param positiveIntGetter function that will be called on items to get their
   *                          positive integer representations
   * @param <T>               items type
   */
  public static <T> void sort(
      List<T> list,
      ToIntFunction<T> positiveIntGetter
  ) {
    if (list.isEmpty()) {
      return;
    }

    ArrayList<LinkedList<T>> buckets = createBuckets();

    int maxDigits = getDigitsCount(
        list.stream()
            .mapToInt(positiveIntGetter)
            .max()
            .orElse(0)
    );

    for (int i = 0; i < maxDigits; i++) {
      transferIntegersFromListToBucketsByDigit(list, buckets, i, positiveIntGetter);
      transferIntegersFromBucketsToList(buckets, list);
    }
  }

  /**
   * Shortcut for sorting an array of integers. (for testing)
   *
   * @param list list of positive integers to sort
   */
  public static void sortPositiveIntegers(List<Integer> list) {
    sort(list, n -> n);
  }

  /**
   * @return digit of the {@code number} at {@code index} starting from least
   *     significant digit
   */
  private static byte getDigit(int number, int index) {
    String s = Integer.toString(Math.abs(number));
    index = s.length() - 1 - index;

    if (index < 0) {
      return 0;
    }

    return (byte) Character.getNumericValue(s.charAt(index));
  }

  /**
   * @return number of digits in the {@code number}
   */
  private static int getDigitsCount(int number) {
    return Integer.toString(Math.abs(number)).length();
  }

  /**
   * @return {@link ArrayList} containing 10 empty {@link LinkedList}s.
   */
  private static <T> ArrayList<LinkedList<T>> createBuckets() {
    ArrayList<LinkedList<T>> buckets = new ArrayList<>(10);
    for (byte i = 0; i < 10; i++) {
      buckets.add(new LinkedList<>());
    }
    return buckets;
  }

  /**
   * Transfers items from the {@code list} into {@code buckets}.
   * Index of bucket to transfer item into is determined by the digit
   * of the item's integer representation at index {@code digitIndex}.
   *
   * @param list              list containing values
   * @param buckets           buckets to put items into
   * @param digitIndex        index of the digit, by which index of the bucket will be
   *                          determined
   * @param positiveIntGetter function that converts values from list into positive
   *                          integers
   */
  private static <T> void transferIntegersFromListToBucketsByDigit(
      List<T> list,
      ArrayList<LinkedList<T>> buckets,
      int digitIndex,
      ToIntFunction<T> positiveIntGetter
  ) {
    Iterator<T> iterator = list.iterator();
    while (iterator.hasNext()) {
      T value = iterator.next();
      int intValue = positiveIntGetter.applyAsInt(value);
      buckets.get(getDigit(intValue, digitIndex)).add(value);
      iterator.remove();
    }
  }

  /**
   * Transfer all values from {@code buckets} 0..9 into {@code list}.
   */
  private static <T> void transferIntegersFromBucketsToList(
      ArrayList<LinkedList<T>> buckets,
      List<T> list
  ) {
    for (LinkedList<T> bucket : buckets) {
      while (!bucket.isEmpty()) {
        list.add(bucket.removeFirst());
      }
    }
  }
}


/**
 * Class containing implementation of the Merge sorting algorithm.
 */
class MergeSort {
  public static <T extends Comparable<T>> void sort(List<T> list) {
    if (list.size() <= 1) {
      return;
    }

    int middle = list.size() / 2;

    List<T> left = new LinkedList<>();
    List<T> right = new LinkedList<>();

    Iterator<T> iterator = list.iterator();

    int i = 0;
    while (iterator.hasNext()) {
      T item = iterator.next();
      (i < middle ? left : right).add(item);
      iterator.remove();
      i++;
    }

    /* Sort parts */
    sort(left);
    sort(right);

    /* Merge parts */

    ListIterator<T> leftIterator = left.listIterator();
    ListIterator<T> rightIterator = right.listIterator();

    while (leftIterator.hasNext() && rightIterator.hasNext()) {
      T leftValue = leftIterator.next();
      T rightValue = rightIterator.next();
      if (leftValue.compareTo(rightValue) <= 0) {
        list.add(leftValue);
        rightIterator.previous();
      } else {
        list.add(rightValue);
        leftIterator.previous();
      }
    }

    /* Transfer remaining items from left part */
    while (leftIterator.hasNext()) {
      list.add(leftIterator.next());
    }

    /* Transfer remaining items from right part */
    while (rightIterator.hasNext()) {
      list.add(rightIterator.next());
    }
  }
}


/**
 * Wrapper for {@link Queue} that bounds size to {@code capacity} and provides
 * method for calculating median value of the queue.
 */
class MedianBoundedQueue {
  private final int capacity;
  private final Queue<Double> values;
  private Double cachedMedian;

  public MedianBoundedQueue(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be greater than 0");
    }

    this.capacity = capacity;
    this.values = new LinkedList<>();
    this.cachedMedian = null;
  }

  private static Double getMedianInSortedList(List<Double> list) {
    int size = list.size();
    int mid = size / 2;
    if (size % 2 == 1) {
      return list.get(mid);
    } else {
      return (list.get(mid - 1) + list.get(mid)) / 2;
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public void add(Double value) {
    cachedMedian = null;

    if (isFilled()) {
      values.poll();
    }
    values.add(value);
  }

  public boolean isFull() {
    return values.size() >= capacity;
  }

  public int size() {
    return values.size();
  }

  /**
   * @return current median of the queue
   */
  public Double getMedian() {
    if (values.isEmpty()) {
      return null;
    }

    // If there is no cached median, calculate it
    if (cachedMedian == null) {
      ArrayList<Double> valuesCopy = new ArrayList<>(values);
      MergeSort.sort(valuesCopy);
      cachedMedian = getMedianInSortedList(valuesCopy);
    }

    return cachedMedian;
  }

  private boolean isFilled() {
    return values.size() >= capacity;
  }
}


/**
 * Class with fraud detection logic.
 */
class FraudDetector {
  private final MedianBoundedQueue lastTrailingDaysSpendings;
  private double currentDayTotalAmount;
  private int alertsCount;
  private Date lastSpendingDate;

  public FraudDetector(int trailingDaysCount) {
    this.lastTrailingDaysSpendings = new MedianBoundedQueue(trailingDaysCount);
    this.alertsCount = 0;
    this.currentDayTotalAmount = 0;
    this.lastSpendingDate = null;
  }

  public int getAlertsCount() {
    return alertsCount;
  }

  public void recordSpending(Spending spending) {
    long daysSinceLastSpending = getDaysSinceLastSpending(spending.getDate());

    if (daysSinceLastSpending == 0) {
      currentDayTotalAmount += spending.getAmount();
    } else if (daysSinceLastSpending > 0) {
      /* Day was incremented -> add it to trailing days */
      lastTrailingDaysSpendings.add(currentDayTotalAmount);
      currentDayTotalAmount = spending.getAmount();

      if (daysSinceLastSpending > 1) {
        /* There was a gap between last spending -> add empty trailing days */

        long zeroDaysToAdd = Math.min(
            // Minus one, since we added old currentDayTotalAmount
            daysSinceLastSpending - 1,
            lastTrailingDaysSpendings.getCapacity()
        );

        for (int i = 0; i < zeroDaysToAdd; i++) {
          lastTrailingDaysSpendings.add(0.0);
        }
      }
    }

    if (isSpendingSuspicious(currentDayTotalAmount)) {
      alertsCount++;
    }
  }

  private long getDaysSinceLastSpending(Date date) {
    if (lastSpendingDate == null) {
      lastSpendingDate = date;
      return 0;
    }

    if (date.compareTo(lastSpendingDate) < 0) {
      throw new IllegalArgumentException("spending occurred in the past");
    }

    long daysSinceLastSpending = lastSpendingDate.difference(date);
    lastSpendingDate = date;

    return daysSinceLastSpending;
  }

  private boolean isSpendingSuspicious(double amount) {
    boolean hasEnoughDataToEvaluate = lastTrailingDaysSpendings.isFull();

    if (hasEnoughDataToEvaluate) {
      double median = lastTrailingDaysSpendings.getMedian();
      return amount >= median * 2;
    }

    return false;
  }
}
