/**
 * Created by Vladislav Deryabkin
 */
package rangequeries;

import common.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;


/**
 * Class containing solution for the Range Queries problem (Problem A on CodeForces).
 */
public class Solution {
  /**
   * Parses command from {@code commandString} and executes it on
   * {@code operationsHistory} {@link RangeMap}.
   *
   * @param commandString     command string to parse and execute
   * @param operationsHistory {@link RangeMap} object containing operations history
   *                          on which command will be executed
   *
   * @throws InvalidInputException when fails to parse the command
   */
  private static void performCommandOnOperationsHistory(
      String commandString,
      RangeMap<Date, Integer> operationsHistory
  ) throws InvalidInputException {
    Command matchedCommand = null;
    Matcher commandMatcher = null;
    for (Command command : Command.values()) {
      commandMatcher = command.match(commandString);
      if (commandMatcher.matches()) {
        matchedCommand = command;
        break;
      }
    }

    if (matchedCommand == null) {
      throw new InvalidInputException();
    }

    String arg1 = commandMatcher.group(1);
    String arg2 = commandMatcher.group(2);

    try {
      switch (matchedCommand) {
        case DEPOSIT:
          Date depositDate = Date.fromString(arg1);
          Integer depositAmount = Integer.parseInt(arg2);
          operationsHistory.add(depositDate, depositAmount);
          break;
        case WITHDRAW:
          Date withdrawDate = Date.fromString(arg1);
          Integer withdrawAmount = Integer.parseInt(arg2);
          operationsHistory.add(withdrawDate, withdrawAmount * -1);
          break;
        case REPORT:
          Date dateFrom = Date.fromString(arg1);
          Date dateTo = Date.fromString(arg2);

          if (!operationsHistory.contains(dateFrom)) {
            operationsHistory.add(dateFrom, 0);
          }

          List<Integer> operations = operationsHistory.lookupRange(dateFrom, dateTo);
          Integer rangeSum = operations.stream().mapToInt(Integer::intValue).sum();
          System.out.println(rangeSum);
          break;
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidInputException();
    }
  }

  public static void main(String[] args) throws InvalidInputException {
    RangeMap<Date, Integer> operationsHistory = new BTreeRangeMap<>();
    Scanner scanner = new Scanner(System.in);
    int n = scanner.nextInt();
    scanner.nextLine();

    for (int i = 0; i < n; i++) {
      performCommandOnOperationsHistory(
          scanner.nextLine(),
          operationsHistory
      );
    }
  }

  /**
   * {@code InvalidInputException} is thrown when program
   * reads input, which is in the incorrect format.
   */
  private static final class InvalidInputException extends Exception {
  }
}
