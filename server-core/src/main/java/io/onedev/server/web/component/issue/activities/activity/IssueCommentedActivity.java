package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Date;

@SuppressWarnings("serial")
public class IssueCommentedActivity implements IssueActivity {

	private final Long commentId;
	
	public IssueCommentedActivity(IssueComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback deleteCallback) {
		return new IssueCommentedPanel(panelId, new LoadableDetachableModel<>() {

			@Override
			protected IssueComment load() {
				return getComment();
			}

		}, target -> {
			OneDev.getInstance(IssueCommentManager.class).delete(getComment());
			deleteCallback.onDelete(target);
		});
	}
	
	public Long getCommentId() {
		return commentId;
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

	@Override
	public User getUser() {
		return getComment().getUser();
	}
	
}
