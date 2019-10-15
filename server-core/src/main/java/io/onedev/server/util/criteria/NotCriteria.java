package io.onedev.server.util.criteria;

import java.util.function.Predicate;

public class NotCriteria<T> implements Predicate<T> {
	
	private final Predicate<T> predicate;
	
	public NotCriteria(Predicate<T> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(T t) {
		return !predicate.test(t);
	}

}
