package io.onedev.server.search.code.query.regex;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class LogicalLiterals implements Literals, Iterable<Literals> {
	
	private final List<Literals> elements;

	public LogicalLiterals(List<Literals> elements) {
		this.elements = ImmutableList.copyOf(elements);
	}
	
	public LogicalLiterals(Literals...elements) {
		this.elements = ImmutableList.copyOf(elements);
	}
	
	public List<Literals> getElements() {
		return elements;
	}

	@Override
	public Iterator<Literals> iterator() {
		return elements.iterator();
	}
	
	public <T extends Literals> T get(Class<T> clazz, int index) {
		return clazz.cast(elements.get(index));
	}
	
}
