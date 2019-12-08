package io.onedev.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Pair<FirstType, SecondType> {
	
	private FirstType first;
	
	private SecondType second;
	
	public Pair() {
		
	}
	
	public Pair(FirstType first, SecondType second) {
		this.first = first;
		this.second = second;
	}

	public FirstType getFirst() {
		return first;
	}

	public void setFirst(FirstType first) {
		this.first = first;
	}

	public SecondType getSecond() {
		return second;
	}

	public void setSecond(SecondType second) {
		this.second = second;
	}
	
	public static <FirstType, SecondType> List<Pair<FirstType, SecondType>> getPairs(Map<FirstType, SecondType> map) {
		List<Pair<FirstType, SecondType>> pairs = new ArrayList<Pair<FirstType, SecondType>>();
		for (Map.Entry<FirstType, SecondType> entry: map.entrySet()) {
			pairs.add(new Pair<FirstType, SecondType>(entry.getKey(), entry.getValue()));
		}
		return pairs;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Pair)) 
			return false;
		if (this == other)
			return true;
		Pair otherPair = (Pair) other;
		return new EqualsBuilder()
			.append(first, otherPair.getFirst())
			.append(second, otherPair.getSecond())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(first)
			.append(second)
			.toHashCode();
	}		
}
