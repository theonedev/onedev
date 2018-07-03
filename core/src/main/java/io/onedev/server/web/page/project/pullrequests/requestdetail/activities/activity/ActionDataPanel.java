package io.onedev.server.web.page.project.pullrequests.requestdetail.activities.activity;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.SinceChangesLink;

@SuppressWarnings("serial")
public abstract class ActionDataPanel extends Panel {

	public ActionDataPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment fragment;
		if (getAction().getData().getCommentSupport() != null || getAction().getData().getDiffSupport() != null) {
			fragment = new Fragment("content", "withCommentOrDiffFrag", this);
			if (getAction().getData().getCommentSupport() != null) {
				fragment.add(new ActionCommentPanel("comment") {

					@Override
					protected PullRequestAction getAction() {
						return ActionDataPanel.this.getAction();
					}
					
				});
			} else {
				fragment.add(new WebMarkupContainer("comment").setVisible(false));
			}
			DiffSupport diffSupport = getAction().getData().getDiffSupport();
			if (diffSupport != null) {
				fragment.add(new PlainDiffPanel("diff", 
						diffSupport.getOldLines(), diffSupport.getOldFileName(),
						diffSupport.getNewLines(), diffSupport.getNewFileName(), 
						true));
			} else {
				fragment.add(new WebMarkupContainer("diff").setVisible(false));
			}
		} else {
			fragment = new Fragment("content", "withoutCommentAndDiffFrag", this);
		}
		User user = User.getForDisplay(getAction().getUser(), getAction().getUserName());
		if (user != null)
			fragment.add(new UserLink("user", user));
		else
			fragment.add(new WebMarkupContainer("user").setVisible(false));
		
		fragment.add(new Label("action", getAction().getData().getDescription()));
		fragment.add(new Label("age", DateUtils.formatAge(getAction().getDate())));
		fragment.add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getAction().getRequest();
			}

		}, getAction().getDate()));
		
		add(fragment);
	}

	protected abstract PullRequestAction getAction();
	
}
