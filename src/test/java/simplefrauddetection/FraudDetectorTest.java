package simplefrauddetection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FraudDetectorTest {
  @Test
  @DisplayName("Correct alerts count on example 2.1.1")
  void testCorrectAlertsForExample() {
    FraudDetector detector = new FraudDetector(3);

    detector.recordSpending(new Spending("2022-01-14", 30.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-15", 25.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-16", 5.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-16", 10.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-17", 20.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-17", 35.0));
    assertEquals(1, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-18", 20.0));
    assertEquals(1, detector.getAlertsCount());
    detector.recordSpending(new Spending("2022-01-19", 40.0));
    assertEquals(2, detector.getAlertsCount());
  }

  @Test
  void testZeroAlertsWhenNotEnoughTrailingDays() {
    FraudDetector detector = new FraudDetector(10);

    detector.recordSpending(new Spending("2013-01-14", 1.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-15", 2.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-16", 5.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-17", 10.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-18", 20.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-19", 750.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-20", 20000.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-21", 40.0));
    assertEquals(0, detector.getAlertsCount());
  }

  @Test
  void testZeroAlertsWhenSpendingDaysEqualTrailingDays() {
    FraudDetector detector = new FraudDetector(5);
    detector.recordSpending(new Spending("2013-01-10", 1.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-12", 2.4));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-13", 500.0));
    assertEquals(0, detector.getAlertsCount());
    detector.recordSpending(new Spending("2013-01-14", 1234.56));
    assertEquals(0, detector.getAlertsCount());

    assertEquals(detector.getAlertsCount(), 0);
  }
}
