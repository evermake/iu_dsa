package simplefrauddetection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MedianBoundedQueueTest {

  @Test
  void testIsFull() {
    MedianBoundedQueue queue = new MedianBoundedQueue(5);

    queue.add(1.0);
    assertFalse(queue.isFull());
    queue.add(-3.0);
    assertFalse(queue.isFull());
    queue.add(100.0);
    assertFalse(queue.isFull());
    queue.add(11.5);
    assertFalse(queue.isFull());
    queue.add(-1.23);
    assertTrue(queue.isFull());
    queue.add(500.500);
    assertTrue(queue.isFull());
  }

  @Test
  void testNullMedianWhenEmpty() {
    for (int capacity = 1; capacity < 100; capacity++) {
      MedianBoundedQueue queue = new MedianBoundedQueue(capacity);
      assertNull(queue.getMedian());
    }
  }

  @Test
  void testCorrectMedianWithUnitCapacity() {
    MedianBoundedQueue queue = new MedianBoundedQueue(1);

    double[] testValues = {1.0, -13.51, 100.0, 123.456, 1000.0, 0.0};

    for (double value : testValues) {
      queue.add(value);
      assertEquals(value, queue.getMedian());
    }
  }

  @Test
  void testCorrectMedianWithOddCapacity() {
    MedianBoundedQueue queue = new MedianBoundedQueue(5);

    queue.add(-2.0);
    assertEquals(-2.0, queue.getMedian());

    queue.add(50.0);
    queue.add(3.0);
    assertEquals(3.0, queue.getMedian());

    queue.add(17.1234);
    queue.add(-1000.99);
    assertEquals(3.0, queue.getMedian());

    queue.add(300.001);
    assertEquals(17.1234, queue.getMedian());
  }

  @Test
  void testCorrectMedianWithEvenCapacity() {
    MedianBoundedQueue queue = new MedianBoundedQueue(6);

    queue.add(-10.0);
    queue.add(13.04);
    assertEquals((-10.0 + 13.04) / 2, queue.getMedian());

    queue.add(-1300.2);
    queue.add(17.1234);
    assertEquals((-10.0 + 13.04) / 2, queue.getMedian());

    queue.add(10.005);
    queue.add(123.45);
    assertEquals((10.005 + 13.04) / 2, queue.getMedian());

    queue.add(21.007);
    assertEquals((13.04 + 17.1234) / 2, queue.getMedian());
  }
}
