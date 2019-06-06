package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class ModelVersion extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public String versionColumn;

}
