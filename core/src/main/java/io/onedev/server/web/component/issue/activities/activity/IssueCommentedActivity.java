package io.onedev.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.util.userident.UserIdent;

@SuppressWarnings("serial")
public class IssueCommentedActivity implements IssueActivity {

	private final Long commentId;
	
	public IssueCommentedActivity(IssueComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback deleteCallback) {
		return new IssueCommentedPanel(panelId, new LoadableDetachableModel<IssueComment>() {

			@Override
			protected IssueComment load() {
				return getComment();
			}
			
		}, new DeleteCallback() {
			
			@Override
			public void onDelete(AjaxRequestTarget target) {
				OneDev.getInstance(IssueCommentManager.class).delete(getComment());
				deleteCallback.onDelete(target);
			}
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
	public UserIdent getUser() {
		return UserIdent.of(UserFacade.of(getComment().getUser()), getComment().getUserName());
	}
	
}
