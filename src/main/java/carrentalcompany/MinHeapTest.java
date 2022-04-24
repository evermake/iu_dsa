/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Solution for the Problem C (min-heap test).
 */
public class MinHeapTest {
  private static final Pattern ADD_BRANCH_COMMAND_PATTERN = Pattern.compile(
      "^ADD (?<branch>\\S+) (?<amount>-?\\d+)$"
  );

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    PriorityQueue<Long, String> queue = new FibonacciHeap<>();

    int commandsCount = scanner.nextInt();
    scanner.nextLine();

    // Read and execute commands
    for (int i = 0; i < commandsCount; i++) {
      String cmd = scanner.nextLine();
      Matcher matcher = ADD_BRANCH_COMMAND_PATTERN.matcher(cmd);

      // Parse and execute necessary command
      if (cmd.equals("PRINT_MIN")) {
        String minBranch = queue.extractMin().value;
        if (minBranch == null) {
          throw new IllegalArgumentException();
        }
        System.out.println(minBranch);
      } else if (matcher.matches()) {
        queue.insert(new Node<>(
            Long.parseLong(matcher.group("amount")),
            matcher.group("branch")
        ));
      } else {
        throw new IllegalArgumentException();
      }
    }
  }
}
