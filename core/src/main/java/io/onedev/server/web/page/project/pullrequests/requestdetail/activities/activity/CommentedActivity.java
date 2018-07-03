package io.onedev.server.web.page.project.pullrequests.requestdetail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.PullRequestActivity;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class CommentedActivity implements PullRequestActivity {

	private final Long commentId;
	
	public CommentedActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Component render(String componentId, DeleteCallback deleteCallback) {
		return new CommentedPanel(componentId, new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				return getComment();
			}
			
		}, deleteCallback);
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
		return User.getForDisplay(getComment().getUser(), getComment().getUserName());
	}

}
