package io.onedev.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A high-water mark committed in the same transaction as the corresponding entities. */
@Entity
@Table
public class EntityIdCounter extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ENTITY_NAME = "entityName";

	public static final String PROP_MAX_ID = "maxId";

	@Column(nullable = false, unique = true)
	private String entityName;

	@Column(nullable = false)
	private long maxId;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public long getMaxId() {
		return maxId;
	}

	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}

}
