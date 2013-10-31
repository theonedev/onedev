package com.pmease.gitop.web.util;

public class Triple<First, Second, Third> {
	  private final First  first;
	  private final Second second;
	  private final Third  third;

	  private volatile String toStringResult;

	  public Triple(First first, Second second, Third third) {
	    this.first  = first;
	    this.second = second;
	    this.third  = third;
	  }

	  public First getFirst() {
	    return first;
	  }

	  public Second getSecond() {
	    return second;
	  }

	  public Third getThird() {
	    return third;
	  }

	  @SuppressWarnings("rawtypes")
	  @Override
	  public boolean equals(Object o) {
	    if (this == o) {
	      return true;
	    }

	    if (o == null || getClass() != o.getClass()) {
	      return false;
	    }

	    final Triple triple = (Triple) o;

	    if (first != null ? !first.equals(triple.first) : triple.first != null) {
	      return false;
	    }

	    if (second != null ? !second.equals(triple.second) : triple.second != null) {
	      return false;
	    }

	    if (third != null ? !third.equals(triple.third) : triple.third != null) {
	      return false;
	    }

	    return true;
	  }

	  @Override
	  public int hashCode() {
	    int result = first != null ? first.hashCode() : 0;

	    result = 31 * result + (second != null ? second.hashCode() : 0);
	    result = 31 * result + (third != null ? third.hashCode() : 0);

	    return result;
	  }

	  @Override
	  public String toString() {
	    if (toStringResult == null) {
	      toStringResult = "Triple{" +
	        "first=" + first +
	        ", second=" + second +
	        ", third=" + third +
	        '}';
	    }

	    return toStringResult;
	  }
	}
