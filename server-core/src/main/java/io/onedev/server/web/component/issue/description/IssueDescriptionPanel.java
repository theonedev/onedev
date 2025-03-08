package io.onedev.server.web.component.issue.description;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueReactionManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.EmailAddressUtils;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DeleteCallback;

public abstract class IssueDescriptionPanel extends Panel {

	public IssueDescriptionPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		Issue issue = getIssue();
		add(new UserIdentPanel("submitterAvatar", issue.getSubmitter(), Mode.AVATAR));
		add(new Label("submitterName", issue.getSubmitter().getDisplayName()));
		add(new Label("submitDate", DateUtils.formatAge(issue.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(issue.getSubmitDate()))));

		if (issue.getOnBehalfOf() != null)
			add(new Label("submitOnBehalfOf", " on behalf of <b>" + HtmlEscape.escapeHtml5(EmailAddressUtils.describe(issue.getOnBehalfOf(), SecurityUtils.canManageIssues(getIssue().getProject()))) + "</b>").setEscapeModelStrings(false));
		else 
			add(new WebMarkupContainer("submitOnBehalfOf").setVisible(false));

		add(new CommentPanel("description") {
			
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

			@Override
			protected String getAutosaveKey() {
				return "issue:" + getIssue().getId() + ":description";
			}

			@Override
			protected Collection<? extends EntityReaction> getReactions() {
				return getIssue().getReactions();
			}

			@Override
			protected void onToggleEmoji(AjaxRequestTarget target, String emoji) {
				OneDev.getInstance(IssueReactionManager.class).toggleEmoji(
					SecurityUtils.getUser(), 
					getIssue(), 
					emoji);
			}

			@Override
			protected Component newMoreActions(String componentId) {
				var fragment = new Fragment(componentId, "linkIssuesFrag", IssueDescriptionPanel.this);
				fragment.add(new AjaxLink<Void>("linkIssues") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						
					}
				});
				return fragment;
			}

		});
        
	}
	
    protected abstract Issue getIssue();
}
