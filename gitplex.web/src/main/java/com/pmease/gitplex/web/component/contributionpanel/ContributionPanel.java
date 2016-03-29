package com.pmease.gitplex.web.component.contributionpanel;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class ContributionPanel extends Panel {

	private final PersonIdent author;
	
	private final PersonIdent committer;
	
	public ContributionPanel(String id, PersonIdent author, PersonIdent committer) {
		super(id);
		this.author = author;
		this.committer = committer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccountLink("author", author));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new AccountLink("committer", committer));
		}
		add(new Label("date", DateUtils.formatAge(committer.getWhen())));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				ContributionPanel.class, "contribution.css")));
	}

}
