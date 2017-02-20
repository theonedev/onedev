package com.gitplex.server.migration;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.gitplex.server.model.AbstractEntity;

@Entity
@Table
public class VersionTable extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public String versionColumn;

}
