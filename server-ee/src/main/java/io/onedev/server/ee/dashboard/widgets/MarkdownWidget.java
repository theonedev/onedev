package io.onedev.server.ee.dashboard.widgets;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Markdown;
import io.onedev.server.model.support.Widget;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import javax.validation.constraints.NotEmpty;

@Editable(name="Markdown", order=10000)
public class MarkdownWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String markdown;
	
	@Editable(order=100)
	@Markdown
	@NotEmpty
	public String getMarkdown() {
		return markdown;
	}

	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}

	@Override
	protected Component doRender(String componentId) {
		return new MarkdownViewer(componentId, Model.of(markdown), null);
	}

}
