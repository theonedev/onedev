package io.onedev.server.util;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilterIterator<T> implements Iterable<T> {
	private Predicate<T> predicate;
	private Iterator<T> internalIterator;

	public FilterIterator(Iterator<T> iterator, Predicate<T> p) {
		this.internalIterator = iterator;
		this.predicate = p;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<T> sourceIterator = internalIterator;
			T current;
			boolean hasCurrent;

			@Override
			public boolean hasNext() {
				while (!hasCurrent) {
					if (!sourceIterator.hasNext()) {
						return false;
					}
					T next = sourceIterator.next();
					if (next != null && predicate.test(next)) {
						current = next;
						hasCurrent = true;
					}
				}
				return true;
			}

			@Override
			public T next() {
				if (!hasNext()) {
					return null;
				}
				T next = current;
				current = null;
				hasCurrent = false;
				return next;
			}
		};
	}
}
