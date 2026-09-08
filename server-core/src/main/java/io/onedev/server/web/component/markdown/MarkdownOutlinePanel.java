package io.onedev.server.web.component.markdown;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class MarkdownOutlinePanel extends Panel {

	private final Component viewer;

	private final boolean sidebar;

	public MarkdownOutlinePanel(String id, Component viewer, boolean sidebar) {
		super(id);
		this.viewer = viewer;
		this.sidebar = sidebar;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("resizeHandle").setVisible(sidebar));
		add(AttributeAppender.append("class", "markdown-outline overflow-hidden "
				+ (sidebar ? "side" : "dropdown")));
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		String script = String.format("onedev.server.markdown.initOutline('%s', '%s', %s);",
				getMarkupId(), viewer.getMarkupId(true), sidebar);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
