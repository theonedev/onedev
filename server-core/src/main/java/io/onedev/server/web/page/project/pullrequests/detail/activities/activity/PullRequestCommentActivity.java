package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestCommentActivity implements PullRequestActivity {

	private final Long commentId;

	public PullRequestCommentActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Component render(String componentId) {
		return new PullRequestCommentPanel(componentId);
	}

	
	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentService.class).load(commentId);
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
