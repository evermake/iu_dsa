/**
 * Created by Vladislav Deryabkin
 */
package simplefrauddetection;

import common.Date;

/**
 * Class with fraud detection logic.
 */
public class FraudDetector {
  private final MedianBoundedQueue lastTrailingDaysSpendings;
  private double currentDayTotalAmount;
  private int alertsCount;
  private Date lastSpendingDate;

  public FraudDetector(int trailingDaysCount) {
    this.lastTrailingDaysSpendings = new MedianBoundedQueue(trailingDaysCount);
    this.alertsCount = 0;
    this.currentDayTotalAmount = 0;
    this.lastSpendingDate = null;
  }

  public int getAlertsCount() {
    return alertsCount;
  }

  public void recordSpending(Spending spending) {
    long daysSinceLastSpending = getDaysSinceLastSpending(spending.getDate());

    if (daysSinceLastSpending == 0) {
      currentDayTotalAmount += spending.getAmount();
    } else if (daysSinceLastSpending > 0) {
      /* Day was incremented -> add it to trailing days */
      lastTrailingDaysSpendings.add(currentDayTotalAmount);
      currentDayTotalAmount = spending.getAmount();

      if (daysSinceLastSpending > 1) {
        /* There was a gap between last spending -> add empty trailing days */

        long zeroDaysToAdd = Math.min(
            // Minus one, since we added old currentDayTotalAmount
            daysSinceLastSpending - 1,
            lastTrailingDaysSpendings.getCapacity()
        );

        for (int i = 0; i < zeroDaysToAdd; i++) {
          lastTrailingDaysSpendings.add(0.0);
        }
      }
    }

    if (isSpendingSuspicious(currentDayTotalAmount)) {
      alertsCount++;
    }
  }

  private long getDaysSinceLastSpending(Date date) {
    if (lastSpendingDate == null) {
      lastSpendingDate = date;
      return 0;
    }

    if (date.compareTo(lastSpendingDate) < 0) {
      throw new IllegalArgumentException("spending occurred in the past");
    }

    long daysSinceLastSpending = lastSpendingDate.difference(date);
    lastSpendingDate = date;

    return daysSinceLastSpending;
  }

  private boolean isSpendingSuspicious(double amount) {
    boolean hasEnoughDataToEvaluate = lastTrailingDaysSpendings.isFull();

    if (hasEnoughDataToEvaluate) {
      double median = lastTrailingDaysSpendings.getMedian();
      return amount >= median * 2;
    }

    return false;
  }
}
