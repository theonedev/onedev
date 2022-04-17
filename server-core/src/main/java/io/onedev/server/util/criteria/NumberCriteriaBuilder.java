package io.onedev.server.util.criteria;

import java.util.Collection;

public interface NumberCriteriaBuilder {

	void forRange(long min, long max);
	
	void forDiscretes(Collection<Long> numbers);
	
}
