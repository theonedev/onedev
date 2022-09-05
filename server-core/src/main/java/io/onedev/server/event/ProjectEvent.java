package io.onedev.server.event;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.persistence.dao.Dao;

public abstract class ProjectEvent extends Event {

	private final Project project;
	
	private transient Optional<String> renderedMarkdown;
	
	private transient Optional<String> processedMarkdown;
	
	public ProjectEvent(User user, Date date, Project project) {
		super(user, date);
		this.project = project;
	}

	public Project getProject() {
		return project;
	}
	
	public abstract String getActivity();
	
	@Nullable
	public String getMarkdown() {
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
	public String getRenderedMarkdown() {
		if (renderedMarkdown == null) {
			String markdown = getMarkdown();
			if (markdown != null) 
				renderedMarkdown = Optional.of(OneDev.getInstance(MarkdownManager.class).render(markdown));
			else
				renderedMarkdown = Optional.empty();
		}
		return renderedMarkdown.orElse(null);
	}

	@Nullable
	public String getProcessedMarkdown() {
		if (processedMarkdown == null) {
			String renderedMarkdown = getRenderedMarkdown();
			if (renderedMarkdown != null) {
				processedMarkdown = Optional.of(OneDev.getInstance(MarkdownManager.class)
						.process(renderedMarkdown, getProject(), null, null, true));
			} else {
				processedMarkdown = Optional.empty();
			}
		}
		return processedMarkdown.orElse(null);
	}
	
	@Nullable
	public String getTextBody() {
		String markdown = getMarkdown();
		MailManager mailManager = OneDev.getInstance(MailManager.class);
		if (markdown != null && mailManager.isMailContent(markdown))  
			markdown = mailManager.toPlainText(markdown);
		
		ActivityDetail activityDetail = getActivityDetail();
		if (activityDetail != null && markdown != null)
			return activityDetail.getTextVersion() + "\n\n" + markdown;
		else if (activityDetail != null)
			return activityDetail.getTextVersion();
		else if (markdown != null)
			return markdown;
		else
			return null;
	}
	
	@Nullable
	public String getHtmlBody() {
		ActivityDetail activityDetail = getActivityDetail();
		String processedMarkdown = getProcessedMarkdown();

		if (activityDetail != null && processedMarkdown != null)
			return activityDetail.getHtmlVersion() + "<br>" + processedMarkdown;
		else if (activityDetail != null)
			return activityDetail.getHtmlVersion();
		else if (processedMarkdown != null)
			return processedMarkdown;
		else
			return null;
	}
	
	public abstract ProjectEvent cloneIn(Dao dao);
	
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(project);
	}
}
