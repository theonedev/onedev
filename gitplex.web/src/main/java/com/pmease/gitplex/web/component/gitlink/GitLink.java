package com.pmease.gitplex.web.component.gitlink;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.GitPlex;

@SuppressWarnings("serial")
public class GitLink extends Panel {

	private final String repoUrl;
	
	private final String commit;
	
	public GitLink(String id, String subModule) {
		super(id);

		this.repoUrl = StringUtils.substringBeforeLast(subModule, ":");
		this.commit = StringUtils.substringAfterLast(subModule, ":");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer link;
		if (repoUrl.startsWith(GitPlex.getInstance().guessServerUrl() + "/")) {
			link = new WebMarkupContainer("link");
			link.add(AttributeModifier.replace("href", repoUrl + "?revision=" + commit));
		} else {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		}
		link.add(new Label("label", repoUrl + ": " + commit));
		add(link);
	}

}
