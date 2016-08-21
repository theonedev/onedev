package com.pmease.commons.hibernate;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@MappedSuperclass
@JsonIgnoreProperties("handler")
public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GenericGenerator(name="entity_id", strategy="com.pmease.commons.hibernate.IdGenerator")
	@GeneratedValue(generator="entity_id") 	
	private Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof AbstractEntity))
			return false;
		if (this == other)
			return true;
		AbstractEntity otherEntity = (AbstractEntity) other;
		if (getId() == null || otherEntity.getId() == null)
			return super.equals(other);
		else 
			return new EqualsBuilder().append(getId(), otherEntity.getId()).isEquals();
	}

	@Override
	public int hashCode() {
		if (getId() == null)
			return super.hashCode();
		else
			return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		if (getId() != null) {
			if (entity.getId() != null)
				return getId().compareTo(entity.getId());
			else
				return -1;
		} else if (entity.getId() != null) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean isNew() {
		return getId() == null;
	}
	
	/**
	 * This method is created to get identifier of entity without triggering 
	 * lazy load the whole entity object.
	 * @param entity
	 * @return
	 */
	public static @Nullable Long idOf(@Nullable AbstractEntity entity) {
		if (entity == null) {
			return null;
		} else if (entity instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) entity;
			return (Long) proxy.getHibernateLazyInitializer().getIdentifier();
		} else {
			return entity.getId();
		}
	}
	
}