package simplefrauddetection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MergeSort {
  public static <T extends Comparable<T>> void sort(List<T> list) {
    if (list.size() <= 1) {
      return;
    }

    int middle = list.size() / 2;

    List<T> left = new LinkedList<>();
    List<T> right = new LinkedList<>();

    Iterator<T> iterator = list.iterator();

    int i = 0;
    while (iterator.hasNext()) {
      T item = iterator.next();
      (i < middle ? left : right).add(item);
      iterator.remove();
      i++;
    }

    /* Sort parts */
    sort(left);
    sort(right);

    /* Merge parts */

    ListIterator<T> leftIterator = left.listIterator();
    ListIterator<T> rightIterator = right.listIterator();

    while (leftIterator.hasNext() && rightIterator.hasNext()) {
      T leftValue = leftIterator.next();
      T rightValue = rightIterator.next();
      if (leftValue.compareTo(rightValue) <= 0) {
        list.add(leftValue);
        rightIterator.previous();
      } else {
        list.add(rightValue);
        leftIterator.previous();
      }
    }

    /* Transfer remaining items from left part */
    while (leftIterator.hasNext()) {
      list.add(leftIterator.next());
    }

    /* Transfer remaining items from right part */
    while (rightIterator.hasNext()) {
      list.add(rightIterator.next());
    }
  }
}
