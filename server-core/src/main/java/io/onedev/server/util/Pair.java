package io.onedev.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

public class Pair<L, R> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private L left;
	
	private R right;
	
	public Pair() {
		
	}
	
	public Pair(@Nullable L left, @Nullable R right) {
		this.left = left;
		this.right = right;
	}

	@Nullable
	public L getLeft() {
		return left;
	}

	public void setLeft(@Nullable L left) {
		this.left = left;
	}

	@Nullable
	public R getRight() {
		return right;
	}

	public void setRight(@Nullable R right) {
		this.right = right;
	}
	
	public static <L, R> List<Pair<L, R>> getPairs(Map<L, R> map) {
		List<Pair<L, R>> pairs = new ArrayList<Pair<L, R>>();
		for (Map.Entry<L, R> entry: map.entrySet()) {
			pairs.add(new Pair<L, R>(entry.getKey(), entry.getValue()));
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
			.append(left, otherPair.getLeft())
			.append(right, otherPair.getRight())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(left)
			.append(right)
			.toHashCode();
	}		
}
