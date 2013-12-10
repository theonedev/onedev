package com.pmease.gitop.web.common.quantity;

/**
 * Represents a unit hierarchy for a given unit of measure; eg: time.  Instances represent specific
 * units from the hierarchy; eg: seconds.
 *
 * @param <U> the type of the concrete unit implementation
 *
 * @author John Sirois
 */
public interface Unit<U extends Unit<U>> {

  /**
   * Returns the weight of this unit relative to other units in the same hierarchy.  Typically the
   * smallest unit in the hierarchy returns 1, but this need not be the case.  It is only required
   * that each unit of the hierarchy return a multiplier relative to a common base unit for the
   * hierarchy.
   */
  double multiplier();
}