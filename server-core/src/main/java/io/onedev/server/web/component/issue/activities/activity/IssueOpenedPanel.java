package io.onedev.server.web.component.issue.activities.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.CommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
class IssueOpenedPanel extends GenericPanel<Issue> {

	public IssueOpenedPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Issue issue = getIssue();
		add(new Label("user", issue.getSubmitter().getDisplayName()));
		add(new Label("age", DateUtils.formatAge(issue.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(issue.getSubmitDate()))));
		
		add(new CommentPanel("body") {

			@Override
			protected String getComment() {
				return getIssue().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(IssueManager.class).saveDescription(getIssue(), comment);
			}

			@Override
			protected List<User> getMentionables() {
				UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();		
				List<User> users = new ArrayList<>(cache.getUsers());
				users.sort(cache.comparingDisplayName(getIssue().getParticipants()));
				return users;
			}
			
			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getIssue().getUUID(), 
						SecurityUtils.canManageIssues(getProject()));
			}

			@Override
			protected boolean canModifyOrDeleteComment() {
				return SecurityUtils.canModify(getIssue());
			}

			@Override
			protected String getRequiredLabel() {
				return null;
			}

			@Override
			protected String getEmptyDescription() {
				return "No description";
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return 0;
					}
					
				};
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}
			
		});
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
}
