package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.CommentPanel;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
class PullRequestOpenedPanel extends GenericPanel<PullRequest> {

	public PullRequestOpenedPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		add(new Label("user", request.getSubmitter().getDisplayName()));
		add(new Label("age", DateUtils.formatAge(request.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(request.getSubmitDate()))));
		
		add(new CommentPanel("body") {

			@Override
			protected String getComment() {
				return getPullRequest().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(PullRequestManager.class).saveDescription(getPullRequest(), comment);
			}

			@Override
			protected Project getProject() {
				return getPullRequest().getTargetProject();
			}

			@Override
			protected List<User> getMentionables() {
				UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();		
				List<User> users = new ArrayList<>(cache.getUsers());
				users.sort(cache.comparingDisplayName(getPullRequest().getParticipants()));
				return users;
			}
			
			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(getProject()));
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

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
}
