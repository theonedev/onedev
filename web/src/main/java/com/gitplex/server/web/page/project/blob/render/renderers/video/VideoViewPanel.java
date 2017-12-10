package com.gitplex.server.web.page.project.blob.render.renderers.video;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;

import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.view.BlobViewPanel;
import com.gitplex.server.web.util.resource.RawBlobResource;
import com.gitplex.server.web.util.resource.RawBlobResourceReference;

@SuppressWarnings("serial")
public class VideoViewPanel extends BlobViewPanel {

	public VideoViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String url = RequestCycle.get().urlFor(new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())).toString();

		add(new WebMarkupContainer("video").add(AttributeAppender.append("src", url)));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new VideoViewResourceReference()));
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
