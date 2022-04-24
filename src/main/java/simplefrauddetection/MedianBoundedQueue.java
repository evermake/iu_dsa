/**
 * Created by Vladislav Deryabkin
 */
package simplefrauddetection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Wrapper for {@link Queue} that bounds size to {@code capacity} and provides
 * method for calculating median value of the queue.
 */
public class MedianBoundedQueue {
  private final int capacity;
  private final Queue<Double> values;
  private Double cachedMedian;

  public MedianBoundedQueue(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be greater than 0");
    }

    this.capacity = capacity;
    this.values = new LinkedList<>();
    this.cachedMedian = null;
  }

  private static Double getMedianInSortedList(List<Double> list) {
    int size = list.size();
    int mid = size / 2;
    if (size % 2 == 1) {
      return list.get(mid);
    } else {
      return (list.get(mid - 1) + list.get(mid)) / 2;
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public void add(Double value) {
    cachedMedian = null;

    if (isFilled()) {
      values.poll();
    }
    values.add(value);
  }

  public boolean isFull() {
    return values.size() >= capacity;
  }

  public int size() {
    return values.size();
  }

  /**
   * @return current median of the queue
   */
  public Double getMedian() {
    if (values.isEmpty()) {
      return null;
    }

    // If there is no cached median, calculate it
    if (cachedMedian == null) {
      ArrayList<Double> valuesCopy = new ArrayList<>(values);
      MergeSort.sort(valuesCopy);
      cachedMedian = getMedianInSortedList(valuesCopy);
    }

    return cachedMedian;
  }

  private boolean isFilled() {
    return values.size() >= capacity;
  }
}
