/**
 * Created by Vladislav Deryabkin
 */
package common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class with utils.
 */
public class Utils {
  /**
   * Returns random integer between a and b.
   * Required for testing.
   *
   * @param a left bound [inclusive]
   * @param b right bound [inclusive]
   *
   * @return random value between a and b
   */
  public static Integer getRandomInteger(int a, int b) {
    return ThreadLocalRandom.current().nextInt(a, b + 1);
  }
}
