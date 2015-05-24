package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.web.WebSession;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RepoTabLink extends Panel {

	private final RepoTab tab;
	
	public RepoTabLink(String id, RepoTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepositoryPage page = (RepositoryPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), RepositoryPage.paramsOf(page.getRepository()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		
		if (WebSession.get().isMiniSidebar())
			link.add(new TooltipBehavior(tab.getTitleModel(), new TooltipConfig().withPlacement(Placement.right)));
		link.add(new Label("text", tab.getTitleModel()));
		add(link);
	}

}
