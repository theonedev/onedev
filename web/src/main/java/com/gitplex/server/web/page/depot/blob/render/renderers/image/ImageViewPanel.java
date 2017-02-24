package com.gitplex.server.web.page.depot.blob.render.renderers.image;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;

import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.view.BlobViewPanel;
import com.gitplex.server.web.util.resource.RawBlobResource;
import com.gitplex.server.web.util.resource.RawBlobResourceReference;

@SuppressWarnings("serial")
public class ImageViewPanel extends BlobViewPanel {

	public ImageViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Image("img", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getDepot(), context.getBlobIdent())));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ImageViewResourceReference()));
	}

	@Override
	protected boolean canEdit() {
		return false;
	}

	@Override
	protected boolean canBlame() {
		return false;
	}

}
