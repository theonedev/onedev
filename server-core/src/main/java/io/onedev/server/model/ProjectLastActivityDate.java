package io.onedev.server.model;

import static io.onedev.server.model.ProjectLastActivityDate.PROP_VALUE;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Maintain high dynamic data in a separate table to avoid project second-level 
 * cache being invalidated frequently
 */
@Entity
@Table(indexes={@Index(columnList= PROP_VALUE)})
public class ProjectLastActivityDate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_VALUE = "value";
	
	@Column(nullable=false)
	private Date value = new Date();

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}
	
}
