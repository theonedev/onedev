package com.turbodev.server.web.page.project.blob.render.renderers.gitlink;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

import com.turbodev.server.TurboDev;
import com.turbodev.server.git.Blob;
import com.turbodev.server.git.Submodule;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class GitLinkPanel extends BlobViewPanel {

	public GitLinkPanel(String id, BlobRenderContext context) {
		super(id, context);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		Submodule submodule = Submodule.fromString(blob.getText().getContent()); 
		WebMarkupContainer link;
		ConfigManager configManager = TurboDev.getInstance(ConfigManager.class);
		if (submodule.getUrl().startsWith(configManager.getSystemSetting().getServerUrl() + "/")) {
			link = new WebMarkupContainer("link");
			link.add(AttributeModifier.replace("href", submodule.getUrl() + "/blob/" + submodule.getCommitId()));
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
		
		response.render(CssHeaderItem.forReference(new GitLinkResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

	@Override
	protected boolean isBlameSupported() {
		return false;
	}

}
