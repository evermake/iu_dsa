/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Solution for the Problem D (Minimum Spanning Forest).
 */
public class CompanyNetwork {
  private static final Pattern ADD_BRANCH_COMMAND_PATTERN = Pattern.compile(
      "^ADD (?<branch>\\S+) (?<penalty>\\d+)$"
  );
  private static final Pattern ADD_CONNECTION_COMMAND_PATTERN = Pattern.compile(
      "^CONNECT (?<branch1>\\S+) (?<branch2>\\S+) (?<distance>\\d+)$"
  );
  private static final Map<String, Vertex<BranchInfo>> labelBranchMap = new HashMap<>();
  private static final Graph<BranchInfo, Double> network = new DynamicGraph<>();

  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);

    int commandsNumber = scanner.nextInt();
    scanner.nextLine();

    for (int i = 0; i < commandsNumber; i++) {
      parseAndExecuteCommand(scanner.nextLine());
    }
  }

  private static void parseAndExecuteCommand(String commandString) throws Exception {
    if (commandString.equals("PRINT_MIN")) {
      /* Compute and print Minimum Spanning Forest of the network */

      List<Edge<Double>> networkMSF = network.getMinimumSpanningForest(
          0.0, Double.MAX_VALUE
      );

      List<String> edgesAsStrings = networkMSF
          .stream()
          .map(Edge::toString)
          .collect(Collectors.toList());

      System.out.println(String.join(" ", edgesAsStrings));

      return;
    }

    Matcher commandMatcher;

    commandMatcher = ADD_BRANCH_COMMAND_PATTERN.matcher(commandString);
    if (commandMatcher.matches()) {
      /* Add branch into the network */

      String branchLabel = commandMatcher.group("branch");
      int branchPenalty = Integer.parseInt(commandMatcher.group("penalty"));
      BranchInfo branchInfo = new BranchInfo(branchLabel, branchPenalty);

      Vertex<BranchInfo> addedBranch = network.insertVertex(branchInfo);
      labelBranchMap.put(branchLabel, addedBranch);

      return;
    }

    commandMatcher = ADD_CONNECTION_COMMAND_PATTERN.matcher(commandString);
    if (commandMatcher.matches()) {
      /* Connect two branches */

      String branch1Label = commandMatcher.group("branch1");
      String branch2Label = commandMatcher.group("branch2");
      int distance = Integer.parseInt(commandMatcher.group("distance"));

      Vertex<BranchInfo> branch1 = labelBranchMap.get(branch1Label);
      Vertex<BranchInfo> branch2 = labelBranchMap.get(branch2Label);

      int branch1Penalty = branch1.getLabel().getPenalty();
      int branch2Penalty = branch2.getLabel().getPenalty();
      double connectionWeight = calculateConnectionWeight(
          branch1Penalty, branch2Penalty, distance
      );

      network.insertEdge(branch1, branch2, connectionWeight);
      return;
    }

    throw new Exception("invalid command");
  }

  private static double calculateConnectionWeight(
      int branch1Penalty,
      int branch2Penalty,
      int distance
  ) {
    return (double) distance / (double) (branch1Penalty + branch2Penalty);
  }

  /**
   * Data-class for storing info about the branch.
   */
  private static final class BranchInfo {
    private final String label;
    private final int penalty;

    public BranchInfo(String label, int penalty) {
      this.label = label;
      this.penalty = penalty;
    }

    public String getLabel() {
      return label;
    }

    public int getPenalty() {
      return penalty;
    }

    @Override
    public String toString() {
      return getLabel();
    }
  }
}
