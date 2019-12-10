package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class PullRequestCommentedActivity implements PullRequestActivity {

	private final Long commentId;
	
	public PullRequestCommentedActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Component render(String componentId, DeleteCallback deleteCallback) {
		return new PullRequestCommentedPanel(componentId, new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				return getComment();
			}
			
		}, new DeleteCallback() {
			
			@Override
			public void onDelete(AjaxRequestTarget target) {
				OneDev.getInstance(PullRequestCommentManager.class).delete(getComment());
				deleteCallback.onDelete(target);
			}
		});
	}
	
	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentManager.class).load(commentId);
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public String getAnchor() {
		return getComment().getAnchor();
	}

	@Override
	public User getUser() {
		return User.from(getComment().getUser(), getComment().getUserName());
	}

}
