package io.onedev.server.web.component.contributorpanel;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.PersonIdentPanel;

@SuppressWarnings("serial")
public class ContributorPanel extends Panel {

	private final PersonIdent author;
	
	private final PersonIdent committer;
	
	public ContributorPanel(String id, PersonIdent author, PersonIdent committer) {
		super(id);
		this.author = author;
		this.committer = committer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PersonIdentPanel("author", author, "Author", Mode.NAME));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new PersonIdentPanel("committer", committer, "Committer", Mode.NAME));
		}
		add(new Label("date", DateUtils.formatAge(committer.getWhen()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(committer.getWhen()))));
	}

}
