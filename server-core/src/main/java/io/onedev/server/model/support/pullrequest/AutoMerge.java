package io.onedev.server.model.support.pullrequest;

import io.onedev.server.model.User;
import org.hibernate.annotations.OptimisticLock;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class AutoMerge implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String COLUMN_ENABLED = "AUTO_MERGE_ENABLED";
	 
	public static final String COLUMN_USER = "AUTO_MERGE_USER";
	
	public static final String COLUMN_COMMIT_MESSAGE = "AUTO_MERGE_COMMIT_MESSAGE";

	@Column(name=COLUMN_ENABLED, nullable = false)
	@OptimisticLock(excluded=true)
	private boolean enabled;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name=COLUMN_USER)
	@OptimisticLock(excluded=true)
	private User user;
	
	@Column(length=1048576, name=COLUMN_COMMIT_MESSAGE)
	@OptimisticLock(excluded=true)
	private String commitMessage;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	@Nullable
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
