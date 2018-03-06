package io.onedev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class CommentedActivity implements PullRequestActivity {

	private final Long commentId;
	
	public CommentedActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new CommentedPanel(panelId, new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				return getComment();
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

}
