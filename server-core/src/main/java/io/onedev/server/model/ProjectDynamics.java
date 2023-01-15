package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.ProjectDynamics.PROP_LAST_ACTIVITY_DATE;
import static io.onedev.server.model.ProjectDynamics.PROP_LAST_COMMIT_DATE;

/**
 * Maintain high dynamic data in a separate table to avoid project second-level 
 * cache being stable frequently
 */
@Entity
@Table(indexes={@Index(columnList= PROP_LAST_ACTIVITY_DATE), @Index(columnList= PROP_LAST_COMMIT_DATE)})
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class ProjectDynamics extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_LAST_ACTIVITY_DATE = "lastActivityDate";

	public static final String PROP_LAST_COMMIT_DATE = "lastCommitDate";
	
	@Column(nullable=false)
	private Date lastActivityDate = new Date();

	private Date lastCommitDate;

	public Date getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(Date lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	@Nullable
	public Date getLastCommitDate() {
		return lastCommitDate;
	}
	
	public void setLastCommitDate(@Nullable Date lastCommitDate) {
		this.lastCommitDate = lastCommitDate;
	}
	
}
