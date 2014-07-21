package com.pmease.gitplex.web.common.quantity;

import java.util.concurrent.TimeUnit;

/**
 * Provides a unit to allow conversions and unambiguous passing around of time {@link Amount}s.
 *
 * @author John Sirois
 */
public enum Time implements Unit<Time> {
  NANOSECONDS(1, TimeUnit.NANOSECONDS, "ns"),
  MICROSECONDS(1000, NANOSECONDS, TimeUnit.MICROSECONDS, "us"),
  MILLISECONDS(1000, MICROSECONDS, TimeUnit.MILLISECONDS, "ms"),
  SECONDS(1000, MILLISECONDS, TimeUnit.SECONDS, "secs"),
  MINUTES(60, SECONDS, TimeUnit.MINUTES, "mins"),
  HOURS(60, MINUTES, TimeUnit.HOURS, "hrs"),
  DAYS(24, HOURS, TimeUnit.DAYS, "days");

  private final double multiplier;
  private final TimeUnit timeUnit;
  private final String display;

  private Time(double multiplier, TimeUnit timeUnit, String display) {
    this.multiplier = multiplier;
    this.timeUnit = timeUnit;
    this.display = display;
  }

  private Time(double multiplier, Time base, TimeUnit timeUnit, String display) {
    this(multiplier * base.multiplier, timeUnit, display);
  }

  @Override
  public double multiplier() {
    return multiplier;
  }

  /**
   * Returns the equivalent {@code TimeUnit}.
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  @Override
  public String toString() {
    return display;
  }
}