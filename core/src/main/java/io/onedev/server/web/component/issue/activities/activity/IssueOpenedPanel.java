package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
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
		add(new Label("user", UserIdent.of(UserFacade.of(issue.getSubmitter()), issue.getSubmitterName()).getName()));
		add(new Label("age", DateUtils.formatAge(issue.getSubmitDate())));
		
		add(new ProjectCommentPanel("body") {

			@Override
			protected String getComment() {
				return getIssue().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(IssueChangeManager.class).changeDescription(getIssue(), comment, SecurityUtils.getUser());
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getIssue().getUUID());
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
			protected ContentVersionSupport getContentVersionSupport() {
				return new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return getIssue().getVersion();
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
