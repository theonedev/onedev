package com.gitplex.server.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class ContributorAvatars extends Panel {

	private final PersonIdent author;
	
	private final PersonIdent committer;

	private final TooltipConfig tooltipConfig;
	
	public ContributorAvatars(String id, PersonIdent author, PersonIdent committer) {
		this(id, author, committer, null);
	}
	
	public ContributorAvatars(String id, PersonIdent author, PersonIdent committer, 
			@Nullable TooltipConfig tooltipConfig) {
		super(id);
		this.author = author;
		this.committer = committer;
		this.tooltipConfig = tooltipConfig;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("author", author, tooltipConfig));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new AvatarLink("committer", committer, tooltipConfig));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AvatarResourceReference()));
	}

}
