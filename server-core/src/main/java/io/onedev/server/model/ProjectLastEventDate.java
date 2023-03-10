package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.ProjectLastEventDate.PROP_ACTIVITY;
import static io.onedev.server.model.ProjectLastEventDate.PROP_COMMIT;

/**
 * Maintain high dynamic data in a separate table to avoid project second-level 
 * cache being invalidated frequently
 */
@Entity
@Table(indexes={@Index(columnList= PROP_ACTIVITY), @Index(columnList= PROP_COMMIT)})
public class ProjectLastEventDate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ACTIVITY = "activity";

	public static final String PROP_COMMIT = "commit";
	
	@Column(nullable=false)
	private Date activity = new Date();

	private Date commit;

	public Date getActivity() {
		return activity;
	}

	public void setActivity(Date activity) {
		this.activity = activity;
	}

	@Nullable
	public Date getCommit() {
		return commit;
	}
	
	public void setCommit(@Nullable Date commit) {
		this.commit = commit;
	}
	
}
