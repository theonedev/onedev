package com.gitplex.commons.hibernate.migration;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.gitplex.commons.hibernate.AbstractEntity;

@Entity
@Table
public class VersionTable extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public String versionColumn;

}
