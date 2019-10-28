package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author robin
 *
 */
@Entity
@Table(indexes={@Index(columnList="name")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Role extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable=false, unique=true)
	private String name;
	
	@Column(nullable=false)
	private boolean viewIssues;
	
	private boolean viewCode;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isViewIssues() {
		return viewIssues;
	}

	public void setViewIssues(boolean viewIssues) {
		this.viewIssues = viewIssues;
	}

	public boolean isViewCode() {
		return viewCode;
	}

	public void setViewCode(boolean viewCode) {
		this.viewCode = viewCode;
	}

}
