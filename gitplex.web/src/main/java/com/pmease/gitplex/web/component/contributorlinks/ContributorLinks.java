package com.pmease.gitplex.web.component.contributorlinks;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.web.component.UserLink;

@SuppressWarnings("serial")
public class ContributorLinks extends Panel {

	private final PersonIdent author;
	
	private final PersonIdent committer;
	
	public ContributorLinks(String id, PersonIdent author, PersonIdent committer) {
		super(id);
		this.author = author;
		this.committer = committer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("author", author));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new UserLink("committer", committer));
		}
	}

}
