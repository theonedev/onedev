package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
class PullRequestOpenedPanel extends GenericPanel<PullRequest> {

	public PullRequestOpenedPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		UserIdent userIdent = UserIdent.of(request.getSubmitter(), request.getSubmitterName());
		add(new Label("user", userIdent.getName()));
		add(new Label("age", DateUtils.formatAge(request.getSubmitDate())));
		
		add(new ProjectCommentPanel("body") {

			@Override
			protected String getComment() {
				return getPullRequest().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(PullRequestChangeManager.class).changeDescription(getPullRequest(), comment, SecurityUtils.getUser());
				send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
			}

			@Override
			protected Project getProject() {
				return getPullRequest().getTargetProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID());
			}

			@Override
			protected boolean canModifyOrDeleteComment() {
				return SecurityUtils.canModify(getPullRequest());
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
						return getPullRequest().getVersion();
					}
					
				};
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}
			
		});
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
}
