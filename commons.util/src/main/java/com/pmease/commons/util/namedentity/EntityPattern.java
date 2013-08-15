package com.pmease.commons.util.namedentity;

import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.util.trimmable.Trimmable;

public class EntityPattern implements Trimmable {
	
	private final String stored;
	
	private final EntityLoader entityLoader;
	
	private EntityPattern(String stored, EntityLoader entityLoader) {
		this.stored = stored;
		this.entityLoader = entityLoader;
	}
	
	public String getStored() {
		return stored;
	}
	
	public Long asId() {
		if (!WildcardUtils.hasWildcards(stored))
			return Long.valueOf(stored);
		else
			return null;
	}
	
	public NamedEntity asEntity() {
		Long id = asId();
		if (id != null)
			return entityLoader.get(id);
		else
			return null;
	}
	
	public String asInput() {
		if (asId() == null) {
			return stored;
		} else {
			NamedEntity identifiable = asEntity();
			if (identifiable != null)
				return identifiable.getName();
			else
				return null;
		}
	}
	
	@Override
	public Trimmable trim() {
		if (asId() == null || asEntity() != null) 
			return this;
		else
			return null;
	}
	
	public static EntityPattern fromStored(String stored, EntityLoader entityLoader) {
		return new EntityPattern(stored, entityLoader);
	}
 
	public static EntityPattern fromInput(String input, EntityLoader entityLoader) {
		if (!WildcardUtils.hasWildcards(input)) {
			NamedEntity entity = entityLoader.get(input);
			if (entity != null)
				return new EntityPattern(entity.getId().toString(), entityLoader);
			else
				return null;
		} else {
			return new EntityPattern(input, entityLoader);
		}
	}
	
}