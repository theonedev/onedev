package io.onedev.server.util.criteria;

import java.util.List;
import java.util.function.Predicate;

public class AndCriteria<T> implements Predicate<T> {
	
	private final List<? extends Predicate<T>> predicates;
	
	public AndCriteria(List<? extends Predicate<T>> predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean test(T t) {
		for (Predicate<T> predicate: predicates) {
			if (!predicate.test(t))
				return false;
		}
		return true;
	}

}
