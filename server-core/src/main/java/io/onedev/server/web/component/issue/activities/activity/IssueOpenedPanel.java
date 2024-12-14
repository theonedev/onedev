package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DeleteCallback;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

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

		if (issue.getOnBehalfOf() != null)
			add(new Label("onBehalfOf", " on behalf of <b>" + escapeHtml5(describe(issue.getOnBehalfOf(), canManageIssues(getIssue().getProject()))) + "</b>").setEscapeModelStrings(false));
		else 
			add(new WebMarkupContainer("onBehalfOf").setVisible(false));
		
		add(new CommentPanel("body") {
			
			@Override
			protected String getComment() {
				return getIssue().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(IssueChangeManager.class).changeDescription(getIssue(), comment);
				((BasePage)getPage()).notifyObservablesChange(target, getIssue().getChangeObservables(false));
			}

			@Override
			protected List<User> getParticipants() {
				return getIssue().getParticipants();
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
			protected boolean canManageComment() {
				return SecurityUtils.canModifyIssue(getIssue());
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
				return () -> 0;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}

			@Nullable
			@Override
			protected String getAutosaveKey() {
				return "issue:" + getIssue().getId() + ":description";
			}
			
		});
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
}
