package io.onedev.server.event.project;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.commenttext.CommentText;

public abstract class ProjectEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final Long userId;
	
	private final Date date;
	
	private transient Optional<CommentText> commentText;
	
	public ProjectEvent(@Nullable User user, Date date, Project project) {
		userId = User.idOf(user);
		this.date = date;
		projectId = project.getId();
	}

	public Project getProject() {
		return OneDev.getInstance(ProjectManager.class).load(projectId);
	}
	
	@Nullable
	public User getUser() {
		return userId != null? OneDev.getInstance(UserManager.class).load(userId): null;
	}

	public Date getDate() {
		return date;
	}
	
	public abstract String getActivity();
	
	@Nullable
	public final CommentText getCommentText() {
		if (commentText == null)
			commentText = Optional.ofNullable(newCommentText());
		return commentText.orElse(null);
	}
	
	@Nullable
	protected CommentText newCommentText() {
		return null;
	}
	
	@Nullable
	public ActivityDetail getActivityDetail() {
		return null;
	}
	
	public LastActivity getLastUpdate() {
		LastActivity lastActivity = new LastActivity();
		lastActivity.setUser(getUser());
		lastActivity.setDescription(getActivity());
		lastActivity.setDate(getDate());
		return lastActivity;
	}
	
	@Nullable
	public String getTextBody() {
		ActivityDetail activityDetail = getActivityDetail();
		CommentText commentText = getCommentText();
		
		if (activityDetail != null && commentText != null)
			return activityDetail.getTextVersion() + "\n\n" + commentText.getPlainContent();
		else if (activityDetail != null)
			return activityDetail.getTextVersion();
		else if (commentText != null)
			return commentText.getPlainContent();
		else
			return null;
	}
	
	@Nullable
	public String getHtmlBody() {
		ActivityDetail activityDetail = getActivityDetail();
		CommentText commentText = getCommentText();

		if (activityDetail != null && commentText != null)
			return activityDetail.getHtmlVersion() + "<br>" + commentText.getHtmlContent();
		else if (activityDetail != null)
			return activityDetail.getHtmlVersion();
		else if (commentText != null)
			return commentText.getHtmlContent();
		else
			return null;
	}
	
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getProject());
	}
	
	@Nullable
	public String getLockName() {
		return null;
	}
	
	public boolean isMinor() {
		return false;
	}
	
}
