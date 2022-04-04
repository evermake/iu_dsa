package common;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
  public static Integer getRandomInteger(int a, int b) {
    return ThreadLocalRandom.current().nextInt(a, b + 1);
  }
}
