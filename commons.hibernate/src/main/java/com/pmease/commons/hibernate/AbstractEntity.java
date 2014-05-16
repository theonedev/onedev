package com.pmease.commons.hibernate;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@MappedSuperclass
@JsonIgnoreProperties("handler")
public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {

	private static final long serialVersionUID = 1L;
	
	@Id
	/*
	@GenericGenerator(name="table-hilo-generator", strategy="org.hibernate.id.TableHiLoGenerator")
	@GeneratedValue(generator="table-hilo-generator")
	*/
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

}