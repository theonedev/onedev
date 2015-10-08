package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class DisapproveActivityPanel extends AbstractActivityPanel {

	public DisapproveActivityPanel(String id, RenderableActivity activity) {
		super(id, activity);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(activity.getDate())));
		add(new SinceChangesLink("changes", requestModel, activity.getDate(), "Changes since this disapproval"));
	}

}
