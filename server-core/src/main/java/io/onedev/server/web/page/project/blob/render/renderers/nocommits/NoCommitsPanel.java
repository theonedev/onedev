package io.onedev.server.web.page.project.blob.render.renderers.nocommits;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

@SuppressWarnings("serial")
public class NoCommitsPanel extends Panel {

	private final BlobRenderContext context;
	
	public NoCommitsPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("url1", context.getProject().getUrl()));
		add(new Label("url2", context.getProject().getUrl()));
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NoCommitsCssResourceReference()));
	}

}
