package simplefrauddetection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Class with radix sorting algorithm implementation.
 * <p>
 * <b><i>Note:</i></b> only suitable for sorting items that can be represented by
 * <b>positive integers.</b>
 */
public class RadixSort {
  /**
   * Sorts a list of items of any type (by getting their positive
   * integer representations) using radix algorithm.
   *
   * @param list              list to sort
   * @param positiveIntGetter function that will be called on items to get their
   *                          positive integer representations
   * @param <T>               items type
   */
  public static <T> void sort(
      List<T> list,
      ToIntFunction<T> positiveIntGetter
  ) {
    if (list.isEmpty()) {
      return;
    }

    ArrayList<LinkedList<T>> buckets = createBuckets();

    int maxDigits = getDigitsCount(
        list.stream()
            .mapToInt(positiveIntGetter)
            .max()
            .orElse(0)
    );

    for (int i = 0; i < maxDigits; i++) {
      transferIntegersFromListToBucketsByDigit(list, buckets, i, positiveIntGetter);
      transferIntegersFromBucketsToList(buckets, list);
    }
  }

  /**
   * Shortcut for sorting an array of integers.
   *
   * @param list list of positive integers to sort
   */
  public static void sortPositiveIntegers(List<Integer> list) {
    sort(list, n -> n);
  }

  private static byte getDigit(int number, int index) {
    String s = Integer.toString(Math.abs(number));
    index = s.length() - 1 - index;

    if (index < 0) {
      return 0;
    }

    return (byte) Character.getNumericValue(s.charAt(index));
  }

  private static int getDigitsCount(int number) {
    return Integer.toString(Math.abs(number)).length();
  }

  private static <T> ArrayList<LinkedList<T>> createBuckets() {
    ArrayList<LinkedList<T>> buckets = new ArrayList<>(10);
    for (byte i = 0; i < 10; i++) {
      buckets.add(new LinkedList<>());
    }
    return buckets;
  }

  private static <T> void transferIntegersFromListToBucketsByDigit(
      List<T> list,
      ArrayList<LinkedList<T>> buckets,
      int digitIndex,
      ToIntFunction<T> positiveIntGetter
  ) {
    Iterator<T> iterator = list.iterator();
    while (iterator.hasNext()) {
      T value = iterator.next();
      int intValue = positiveIntGetter.applyAsInt(value);
      buckets.get(getDigit(intValue, digitIndex)).add(value);
      iterator.remove();
    }
  }

  private static <T> void transferIntegersFromBucketsToList(
      ArrayList<LinkedList<T>> buckets,
      List<T> list
  ) {
    for (LinkedList<T> bucket : buckets) {
      while (!bucket.isEmpty()) {
        list.add(bucket.removeFirst());
      }
    }
  }
}
