package com.pmease.gitplex.web.component.commit;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;

@SuppressWarnings("serial")
public class CommitterInfoPanel extends Panel {

	private final IModel<AvatarMode> mode = Model.of(AvatarMode.NAME);
	
	public CommitterInfoPanel(String id, IModel<Commit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		

		add(new PersonLink("committer", Model.of(getCommit().getCommitter()), mode.getObject()));
		
		add(new AgeLabel("committerdate", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getCommitter().getWhen();
			}
		}));
	}
	
	private Commit getCommit() {
		return (Commit) getDefaultModelObject();
	}

	public CommitterInfoPanel committerLinkMode(AvatarMode mode) {
		this.mode.setObject(mode);
		return this;
	}
	
	@Override
	public void onDetach() {
		if (mode != null) {
			mode.detach();
		}
		
		super.onDetach();
	}
}
