package rangemap;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class BTreeRangeMapTest {
  private Integer getRandomInteger(int a, int b) {
    return ThreadLocalRandom.current().nextInt(a, b + 1);
  }

  @Test
  void testCorrectlyInsertsIntegers() {
    RangeMap<Integer, Integer> rangeMap = new BTreeRangeMap<>();

    rangeMap.add(1, 5);
    assertTrue(rangeMap.contains(1));

    rangeMap.add(2, 4);
    assertTrue(rangeMap.contains(2));

    rangeMap.add(3, 3);
    assertTrue(rangeMap.contains(3));

    rangeMap.add(4, 2);
    assertTrue(rangeMap.contains(4));

    rangeMap.add(5, 1);
    assertTrue(rangeMap.contains(5));

    assertEquals(5, rangeMap.lookup(1));
    assertEquals(4, rangeMap.lookup(2));
    assertEquals(3, rangeMap.lookup(3));
    assertEquals(2, rangeMap.lookup(4));
    assertEquals(1, rangeMap.lookup(5));
  }

  @Test
  void testDoesNotContainNonExistingKeys() {
    RangeMap<Integer, Integer> rangeMap = new BTreeRangeMap<>();

    assertFalse(rangeMap.contains(0));
    assertFalse(rangeMap.contains(1));
    assertFalse(rangeMap.contains(2));
    assertFalse(rangeMap.contains(3));
    assertFalse(rangeMap.contains(4));
    assertFalse(rangeMap.contains(5));
    assertFalse(rangeMap.contains(123));

    rangeMap.add(1, 10);
    rangeMap.add(2, 20);
    rangeMap.add(3, 30);
    rangeMap.add(4, 40);
    rangeMap.add(5, 50);

    assertFalse(rangeMap.contains(0));
    assertFalse(rangeMap.contains(123));
    assertFalse(rangeMap.contains(10));
    assertFalse(rangeMap.contains(20));
    assertFalse(rangeMap.contains(30));
    assertFalse(rangeMap.contains(40));
    assertFalse(rangeMap.contains(50));
  }

  @Test
  void testContainsExistingKeys() {
    RangeMap<Integer, Integer> rangeMap = new BTreeRangeMap<>();

    rangeMap.add(1, 10);
    rangeMap.add(20, -200);
    rangeMap.add(300, 3000);

    assertTrue(rangeMap.contains(1));
    assertTrue(rangeMap.contains(20));
    assertTrue(rangeMap.contains(300));
  }

  @Test
  void testLookup() {
    RangeMap<Integer, String> rangeMap = new BTreeRangeMap<>();

    assertNull(rangeMap.lookup(0));
    assertNull(rangeMap.lookup(1));
    assertNull(rangeMap.lookup(5));
    assertNull(rangeMap.lookup(100));
    assertNull(rangeMap.lookup(-123));

    rangeMap.add(156, "156 lives here");
    assertEquals("156 lives here", rangeMap.lookup(156));

    rangeMap.add(-1, "Error");
    rangeMap.add(200, "OK");
    rangeMap.add(12345, "Hello, world!");

    assertEquals("156 lives here", rangeMap.lookup(156));
    assertEquals("Hello, world!", rangeMap.lookup(12345));
    assertEquals("OK", rangeMap.lookup(200));
    assertEquals("Error", rangeMap.lookup(-1));
  }

  @RepeatedTest(100)
  void testOn1000RandomIntegers() {
    RangeMap<Integer, Integer> rangeMap = new BTreeRangeMap<>();
    assertTrue(rangeMap.isEmpty());

    List<Integer> addedIntegers = new ArrayList<>();
    int randomMax = getRandomInteger(10, 10000);

    for (int i = 0; i < 1000; i++) {
      assertEquals(i, rangeMap.size());

      Integer random = getRandomInteger(0, randomMax);
      rangeMap.add(random, random * 7 % 13);
      addedIntegers.add(random);

      assertEquals(i + 1, rangeMap.size());
      assertFalse(rangeMap.isEmpty());
    }

    for (Integer value : addedIntegers) {
      assertTrue(rangeMap.contains(value));
      assertFalse(rangeMap.contains((value + 1) * -1));
    }
  }

  @Test
  void testLookupRangeOnWeekdays() {
    RangeMap<Integer, String> rangeMap = new BTreeRangeMap<>();

    rangeMap.add(1, "Monday");
    rangeMap.add(2, "Tuesday");
    rangeMap.add(3, "Wednesday");
    rangeMap.add(4, "Thursday");
    rangeMap.add(5, "Friday");
    rangeMap.add(6, "Saturday");
    rangeMap.add(7, "Sunday");

    List<String> range1Expected = Arrays.asList("Monday", "Tuesday");
    List<String> range1Actual = rangeMap.lookupRange(1, 2);
    assertEquals(range1Expected, range1Actual);

    List<String> range2Expected = Arrays.asList(
        "Wednesday", "Thursday", "Friday", "Saturday"
    );
    List<String> range2Actual = rangeMap.lookupRange(3, 6);
    assertEquals(range2Expected, range2Actual);

    List<String> range3Expected = Arrays.asList(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );
    List<String> range3Actual = rangeMap.lookupRange(1, 7);
    assertEquals(range3Expected, range3Actual);
  }

  @Test
  void testLookupRangeWithDuplicateKeys() {
    RangeMap<Integer, Integer> rangeMap = new BTreeRangeMap<>();

    List<Integer> randomKeys = new ArrayList<>(1000);
    List<Integer> randomValues = new ArrayList<>(1000);

    for (int i = 0; i < 1000; i++) {
      int randomKey = getRandomInteger(-10, 10);
      int randomValue = getRandomInteger(-100000, 100000);

      randomKeys.add(randomKey);
      randomValues.add(randomValue);

      rangeMap.add(randomKey, randomValue);
    }

    assertEquals(1000, rangeMap.size());

    int minKey = Collections.min(randomKeys);
    int maxKey = Collections.max(randomKeys);

    List<Integer> lookupResult = rangeMap.lookupRange(minKey, maxKey);

    assertEquals(1000, lookupResult.size());

    int expectedSum = randomValues.stream().mapToInt(Integer::intValue).sum();
    int actualSum = lookupResult.stream().mapToInt(Integer::intValue).sum();

    assertEquals(expectedSum, actualSum);
  }
}
