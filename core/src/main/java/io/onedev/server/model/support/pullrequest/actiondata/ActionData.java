package io.onedev.server.model.support.pullrequest.actiondata;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.ActionDataPanel;

public abstract class ActionData implements Serializable {

	private static final long serialVersionUID = 1L;

	public Component render(String componentId, PullRequestAction action) {
		return new ActionDataPanel(componentId) {

			private static final long serialVersionUID = 1L;

			@Override
			protected PullRequestAction getAction() {
				return action;
			}

		};
	}
	
	public abstract String getDescription();
	
	@Nullable
	public CommentSupport getCommentSupport() {
		return null;
	}
	
	@Nullable
	public DiffSupport getDiffSupport() {
		return null;
	}
	
}
