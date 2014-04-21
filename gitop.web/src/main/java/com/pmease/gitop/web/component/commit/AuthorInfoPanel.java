package com.pmease.gitop.web.component.commit;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.parboiled.common.Preconditions;

import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.PersonLink;

@SuppressWarnings("serial")
public class AuthorInfoPanel extends Panel {

	private final IModel<Mode> mode = Model.of(Mode.NAME);
	
	public AuthorInfoPanel(String id, IModel<Commit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PersonLink("author", getCommit().getAuthor().getPerson(), mode.getObject()));
		add(new AgeLabel("authordate", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getAuthor().getDate();
			}
			
		}));
	}
	
	public void setAuthorMode(Mode mode) {
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
