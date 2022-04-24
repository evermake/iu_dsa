/**
 * Created by Vladislav Deryabkin
 */
package simplefrauddetection;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Solution for the problem A (simple fraud detection).
 */
public class Solution {
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
