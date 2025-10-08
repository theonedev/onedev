package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.OptimisticLock;

@Embeddable
public class AutoMerge implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String COLUMN_ENABLED = "AUTO_MERGE_ENABLED";
	 	
	public static final String COLUMN_COMMIT_MESSAGE = "AUTO_MERGE_COMMIT_MESSAGE";

	@Column(name=COLUMN_ENABLED, nullable = false)
	@OptimisticLock(excluded=true)
	private boolean enabled;
	
	@Column(length=1048576, name=COLUMN_COMMIT_MESSAGE)
	@OptimisticLock(excluded=true)
	private String commitMessage;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Nullable
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(@Nullable String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
