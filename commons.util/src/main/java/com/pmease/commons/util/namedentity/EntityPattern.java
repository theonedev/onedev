package com.pmease.commons.util.namedentity;

import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.util.trimmable.Trimmable;

/**
 * An entity pattern refers to a string matching one or more database entities. 
 * <p>
 * When refer to only one entity, the string should represent id of the entity. 
 * This is necessary as entity name can be changed while id will never. In case 
 * the entity is deleted, the data structure can implement {@link Trimmable} to 
 * trim the pattern. 
 * <p>
 * Example entity patterns of stored form: <i>100, qa*, 5.0.?</i>
 * <p>
 * @author robin
 *
 */
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
	public Object trim(Object context) {
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