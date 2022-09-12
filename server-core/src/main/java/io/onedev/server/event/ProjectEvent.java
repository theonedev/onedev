package io.onedev.server.event;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;

public abstract class ProjectEvent extends Event {

	private final Project project;
	
	private transient Optional<CommentText> commentText;
	
	public ProjectEvent(User user, Date date, Project project) {
		super(user, date);
		this.project = project;
	}

	public Project getProject() {
		return project;
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
	
	public LastUpdate getLastUpdate() {
		LastUpdate lastUpdate = new LastUpdate();
		lastUpdate.setUser(getUser());
		lastUpdate.setActivity(getActivity());
		lastUpdate.setDate(getDate());
		return lastUpdate;
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
	
	public abstract ProjectEvent cloneIn(Dao dao);
	
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(project);
	}
}
