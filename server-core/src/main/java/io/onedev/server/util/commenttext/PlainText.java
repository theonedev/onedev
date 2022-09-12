package io.onedev.server.util.commenttext;

import org.unbescape.html.HtmlEscape;

public class PlainText extends CommentText {

	public PlainText(String content) {
		super(content);
	}

	@Override
	public String getHtmlContent() {
		return "<pre>" + HtmlEscape.escapeHtml5(getContent()) + "</pre>";
	}

	@Override
	public String getPlainContent() {
		return getContent();
	}

}
