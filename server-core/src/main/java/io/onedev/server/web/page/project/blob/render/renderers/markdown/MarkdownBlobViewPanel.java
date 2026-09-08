package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import io.onedev.server.git.Blob;
import io.onedev.server.web.component.markdown.BlobMarkdownViewer;
import io.onedev.server.web.component.markdown.MarkdownOutlinePanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;
import io.onedev.server.web.util.WicketUtils;

public class MarkdownBlobViewPanel extends BlobViewPanel {

	private static final String COOKIE_OUTLINE = "markdownBlob.outline";

	private static final String COOKIE_OUTLINE_WIDTH = "markdownBlob.outline.width";

	private MarkdownOutlinePanel outline;

	private CheckBox outlineToggle;

	public MarkdownBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
		var markdown = new BlobMarkdownViewer("markdown", Model.of(blob.getText().getContent()), null) {

			@Override
			protected BlobRenderContext getRenderContext() {
				return context;
			}
			
		};
		outline = new MarkdownOutlinePanel("outline", markdown, true);

		super.onInitialize();

		add(markdown);
		add(outline);

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		var widthCookie = request.getCookie(COOKIE_OUTLINE_WIDTH);
		String style = "width:" + (widthCookie != null ? widthCookie.getValue() : "300") + "px;";
		if (!isOutlineVisibleInitially())
			style += "display:none;";
		outline.add(AttributeAppender.replace("style", style));
	}

	@Override
	protected WebMarkupContainer newExtraOptions(String id) {
		var options = new Fragment(id, "outlineOptionsFrag", this);
		outlineToggle = new CheckBox("outline", Model.of(isOutlineVisibleInitially()));
		outlineToggle.setOutputMarkupId(true);
		outlineToggle.add(AttributeAppender.append("onchange", String.format(
				"onedev.server.markdown.toggleOutline('%s', this.checked);", outline.getMarkupId(true))));
		options.add(outlineToggle);
		return options;
	}

	private boolean isOutlineVisibleInitially() {
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		var cookie = request.getCookie(COOKIE_OUTLINE);
		if (cookie != null)
			return cookie.getValue().equals("yes");
		else
			return !WicketUtils.isDevice();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobCssResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

}
