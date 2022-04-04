package rangemap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command {
  DEPOSIT("^(\\d{4}-\\d{2}-\\d{2}) DEPOSIT (\\d+)$"),
  WITHDRAW("^(\\d{4}-\\d{2}-\\d{2}) WITHDRAW (\\d+)$"),
  REPORT("^REPORT FROM (\\d{4}-\\d{2}-\\d{2}) TO (\\d{4}-\\d{2}-\\d{2})$");

  private final Pattern pattern;

  Command(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  public Matcher match(String command) {
    return pattern.matcher(command);
  }
}
