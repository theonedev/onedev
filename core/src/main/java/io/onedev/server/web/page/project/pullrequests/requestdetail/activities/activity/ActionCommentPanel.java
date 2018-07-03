package io.onedev.server.web.page.project.pullrequests.requestdetail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.projectcomment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class ActionCommentPanel extends Panel {

	public ActionCommentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ProjectCommentPanel("comment") {

			@Override
			protected String getComment() {
				return getAction().getData().getCommentSupport().getComment();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				getAction().getData().getCommentSupport().setComment(comment);
				OneDev.getInstance(PullRequestActionManager.class).save(getAction());
			}

			@Override
			protected Project getProject() {
				return getAction().getRequest().getTargetProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getAction().getRequest().getUUID());
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModify(getAction());
			}

			@Override
			protected String getRequiredLabel() {
				return null;
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return null;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}
			
		});
	}

	protected abstract PullRequestAction getAction();
	
}
