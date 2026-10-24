package io.onedev.server.model;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table
public class ModelVersion extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String PROP_VERSION_COLUMN = "versionColumn";
	
	public String versionColumn;

}
