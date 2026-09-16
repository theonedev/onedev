package io.onedev.server.model;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
