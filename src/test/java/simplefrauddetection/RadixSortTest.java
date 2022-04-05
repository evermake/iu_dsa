package simplefrauddetection;

import static common.Utils.getRandomInteger;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.RepeatedTest;

class RadixSortTest {
  @RepeatedTest(100)
  void testSortsRandomPositiveIntegers() {
    List<Integer> positiveIntegers = new LinkedList<>();
    int size = getRandomInteger(0, 1000);

    for (int i = 0; i < size; i++) {
      Integer randomInteger = getRandomInteger(0, 100000);
      positiveIntegers.add(randomInteger);
    }

    List<Integer> sortedCopy = new ArrayList<>(positiveIntegers);
    sortedCopy.sort(Integer::compareTo);

    RadixSort.sortPositiveIntegers(positiveIntegers);

    assertEquals(sortedCopy, positiveIntegers);
  }
}
