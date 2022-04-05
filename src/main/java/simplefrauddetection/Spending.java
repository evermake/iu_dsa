package simplefrauddetection;

import common.Date;

/**
 * Data class for storing spending with date and amount.
 */
public class Spending {
  private final Date date;
  private final double amount;

  public Spending(Date date, double amount) {
    this.date = date;
    this.amount = amount;
  }

  /**
   * Shortcut for {@code new Spending(Date.fromString(dateString), amount)}.
   *
   * @param dateString date in format "YYYY-MM-DD"
   * @param amount     spending amount
   */
  public Spending(String dateString, double amount) {
    this(Date.fromString(dateString), amount);
  }

  public Date getDate() {
    return date;
  }

  public double getAmount() {
    return amount;
  }
}
