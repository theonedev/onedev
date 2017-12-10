package com.gitplex.server.web.page.project.blob.render.renderers.failsafe;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class FailsafeViewPanel extends BlobViewPanel {

	public FailsafeViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new FailsafeResourceReference()));
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
