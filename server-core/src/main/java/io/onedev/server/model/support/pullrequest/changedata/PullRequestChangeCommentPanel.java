package io.onedev.server.model.support.pullrequest.changedata;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class PullRequestChangeCommentPanel extends ProjectCommentPanel {

	public PullRequestChangeCommentPanel(String id) {
		super(id);
	}

	@Override
	protected String getComment() {
		return getChange().getData().getCommentAware().getComment();
	}

	@Override
	protected List<User> getMentionables() {
		return OneDev.getInstance(UserManager.class).queryAndSort(getChange().getRequest().getParticipants());
	}

	@Override
	protected void onSaveComment(AjaxRequestTarget target, String comment) {
		getChange().getData().getCommentAware().setComment(comment);
		OneDev.getInstance(PullRequestChangeManager.class).save(getChange());
	}

	@Override
	protected Project getProject() {
		return getChange().getRequest().getTargetProject();
	}

	@Override
	protected AttachmentSupport getAttachmentSupport() {
		return new ProjectAttachmentSupport(getProject(), getChange().getRequest().getUUID(), 
				SecurityUtils.canManagePullRequests(getProject()));
	}

	@Override
	protected boolean canModifyOrDeleteComment() {
		return SecurityUtils.canModifyOrDelete(getChange());
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
	
	protected abstract PullRequestChange getChange();
}
