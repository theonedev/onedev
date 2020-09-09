package io.onedev.server.web.page.project.blob.render.renderers.failsafe;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class FailsafeViewPanel extends BlobViewPanel {

	public FailsafeViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return false;
	}

}
