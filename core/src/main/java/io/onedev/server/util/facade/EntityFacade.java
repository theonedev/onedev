package io.onedev.server.util.facade;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class EntityFacade implements Serializable, Comparable<EntityFacade> {

	private static final long serialVersionUID = 1L;

	private final Long id;

	public EntityFacade(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof EntityFacade) {
			EntityFacade otherEntity = (EntityFacade) other;
			return new EqualsBuilder().append(getId(), otherEntity.getId()).isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (getId() == null)
			return super.hashCode();
		else
			return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	@Override
	public int compareTo(EntityFacade entity) {
		return getId().compareTo(entity.getId());
	}
	
}
