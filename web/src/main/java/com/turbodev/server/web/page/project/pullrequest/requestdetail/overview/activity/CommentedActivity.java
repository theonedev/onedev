package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.PullRequestCommentManager;
import com.turbodev.server.model.PullRequestComment;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

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
		return TurboDev.getInstance(PullRequestCommentManager.class).load(commentId);
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
