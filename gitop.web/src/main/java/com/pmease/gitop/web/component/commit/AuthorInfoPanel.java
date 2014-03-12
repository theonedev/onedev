package com.pmease.gitop.web.component.commit;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.parboiled.common.Preconditions;

import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.project.api.GitPerson;

@SuppressWarnings("serial")
public class AuthorInfoPanel extends Panel {

	private final IModel<GitPersonLink.Mode> mode = Model.of(Mode.NAME);
	
	public AuthorInfoPanel(String id, IModel<Commit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GitPersonLink("author", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getCommit().getAuthor());
			}
			
		}, mode.getObject()));
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
