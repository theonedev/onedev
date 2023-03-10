package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.AgentLastUsedDate.PROP_VALUE;

/**
 * Maintain high dynamic data in a separate table to avoid agent second-level 
 * cache being invalidated frequently
 */
@Entity
@Table(indexes={@Index(columnList= PROP_VALUE)})
public class AgentLastUsedDate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_VALUE = "value";
	
	@Column
	private Date value = new Date();

	@Nullable
	public Date getValue() {
		return value;
	}

	public void setValue(@Nullable Date value) {
		this.value = value;
	}
	
}
