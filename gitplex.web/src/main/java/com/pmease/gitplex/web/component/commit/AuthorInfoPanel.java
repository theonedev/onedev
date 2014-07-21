package com.pmease.gitplex.web.component.commit;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.parboiled.common.Preconditions;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;

@SuppressWarnings("serial")
public class AuthorInfoPanel extends Panel {

	private final IModel<AvatarMode> mode = Model.of(AvatarMode.NAME);
	
	public AuthorInfoPanel(String id, IModel<Commit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PersonLink("author", Model.of(getCommit().getAuthor()), mode.getObject()));
		add(new AgeLabel("authordate", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getAuthor().getWhen();
			}
			
		}));
	}
	
	public void setAuthorMode(AvatarMode mode) {
		this.mode.setObject(Preconditions.checkNotNull(mode));
	}
	
	private Commit getCommit() {
		return (Commit) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		if (mode != null) {
			mode.detach();
		}
		
		super.onDetach();
	}
}
