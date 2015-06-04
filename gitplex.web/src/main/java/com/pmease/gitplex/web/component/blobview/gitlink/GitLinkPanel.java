package com.pmease.gitplex.web.component.blobview.gitlink;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Submodule;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class GitLinkPanel extends BlobViewPanel {

	public GitLinkPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Submodule submodule = Submodule.fromString(context.getBlob().getText().getContent()); 
		WebMarkupContainer link;
		ConfigManager configManager = GitPlex.getInstance(ConfigManager.class);
		if (submodule.getUrl().startsWith(configManager.getSystemSetting().getServerUrl() + "/")) {
			link = new WebMarkupContainer("link");
			link.add(AttributeModifier.replace("href", submodule.getUrl() + "/browse?revision=" + submodule.getCommitId()));
		} else {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		}
		link.add(new Label("label", submodule));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(GitLinkPanel.class, "git-link.css")));
	}

}
