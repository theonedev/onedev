package io.onedev.server.web.component.markdown;

import io.onedev.server.web.util.AjaxPayload;
import org.apache.wicket.ajax.AjaxRequestTarget;

public class ContentQuoted extends AjaxPayload {

	private final String content;
	
	public ContentQuoted(AjaxRequestTarget target, String content) {
		super(target);
		this.content = content;
	}

	public String getContent() {
		return content;
	}
}