package io.onedev.server.web.page.project.issues.issuedetail.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;

@SuppressWarnings("serial")
public class CommentedActivity implements IssueActivity {

	private final Long commentId;
	
	public CommentedActivity(IssueComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new CommentedPanel(panelId, new LoadableDetachableModel<IssueComment>() {

			@Override
			protected IssueComment load() {
				return getComment();
			}
			
		});
	}
	
	public IssueComment getComment() {
		return OneDev.getInstance(IssueCommentManager.class).load(commentId);
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
