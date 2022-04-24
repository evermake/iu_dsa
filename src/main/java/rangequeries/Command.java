/**
 * Created by Vladislav Deryabkin
 */
package rangequeries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum of possible commands in the task.
 * Items also contain regexp patterns for easier parsing implementation.
 */
public enum Command {
  DEPOSIT("^(\\d{4}-\\d{2}-\\d{2}) DEPOSIT (\\d+)$"),
  WITHDRAW("^(\\d{4}-\\d{2}-\\d{2}) WITHDRAW (\\d+)$"),
  REPORT("^REPORT FROM (\\d{4}-\\d{2}-\\d{2}) TO (\\d{4}-\\d{2}-\\d{2})$");

  private final Pattern pattern;

  Command(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * @param command command string to match
   *
   * @return {@link Matcher} of the command for the given string
   */
  public Matcher match(String command) {
    return pattern.matcher(command);
  }
}
