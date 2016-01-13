package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class UndoReviewActivityPanel extends AbstractActivityPanel {

	public UndoReviewActivityPanel(String id, RenderableActivity activity) {
		super(id, activity);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("avatar", userModel.getObject(), null));
		add(new UserLink("name", userModel.getObject()));
		add(new Label("age", DateUtils.formatAge(activity.getDate())));
		add(new SinceChangesLink("changes", requestModel, activity.getDate(), "Changes since this activity"));
	}

}
