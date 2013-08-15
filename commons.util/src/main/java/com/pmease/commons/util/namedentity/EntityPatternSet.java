package com.pmease.commons.util.namedentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pmease.commons.util.pattern.ExclusiveAwarePattern;
import com.pmease.commons.util.pattern.PatternSet;
import com.pmease.commons.util.trimmable.Trimmable;

public class EntityPatternSet extends PatternSet implements Trimmable {

	private final EntityLoader entityLoader;

	private EntityPatternSet(List<ExclusiveAwarePattern> stored, EntityLoader entityLoader) {
		super(stored);
		
		this.entityLoader = entityLoader;
	}
	
	public List<ExclusiveAwarePattern> getStored() {
		return getPatterns();
	}
	
	@Override
	public Trimmable trim() {
		for (Iterator<ExclusiveAwarePattern> it = getStored().iterator(); it.hasNext();) {
			if (EntityPattern.fromStored(it.next().getPattern(), entityLoader).trim() == null)
				it.remove();
		}
		
		if (getStored().isEmpty())
			return null;
		else
			return this;
	}

	public List<ExclusiveAwarePattern> asInput() {
		List<ExclusiveAwarePattern> input = new ArrayList<ExclusiveAwarePattern>();
		
		for (ExclusiveAwarePattern eachStored: getStored()) {
			String str = EntityPattern.fromStored(eachStored.getPattern(), entityLoader).asInput();
			if (str != null) {
				input.add(new ExclusiveAwarePattern(str, eachStored.isExclusive()));
			}
		}
		
		return input;
	}

	public static EntityPatternSet fromInput(List<ExclusiveAwarePattern> input, EntityLoader entityLoader) {
		List<ExclusiveAwarePattern> stored = new ArrayList<ExclusiveAwarePattern>();
		for (ExclusiveAwarePattern eachInput: input) {
			EntityPattern pattern = EntityPattern.fromInput(eachInput.getPattern(), entityLoader);
			if (pattern != null)
				stored.add(new ExclusiveAwarePattern(pattern.getStored(), eachInput.isExclusive()));
		}
		return new EntityPatternSet(stored, entityLoader);
	}
	
	public static EntityPatternSet fromInput(String input, EntityLoader entityLoader) {
		return fromInput(fromString(input).getPatterns(), entityLoader);
	}
	
	public static EntityPatternSet fromStored(List<ExclusiveAwarePattern> stored, EntityLoader entityLoader) {
		return new EntityPatternSet(stored, entityLoader);
	}
	
	public static EntityPatternSet fromStored(String stored, EntityLoader entityLoader) {
		return new EntityPatternSet(fromString(stored).getPatterns(), entityLoader);
	}
	
}
