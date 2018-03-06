package io.onedev.server.web.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

@SuppressWarnings("serial")
public class MultilineLabel extends MultiLineLabel {
	public MultilineLabel(String id) {
		super(id);
	}

	public MultilineLabel(String id, String label) {
		super(id, label);
	}

	public MultilineLabel(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		final CharSequence body = toMultilineMarkup(getDefaultModelObjectAsString());
		replaceComponentTagBody(markupStream, openTag, body);
	}

	private CharSequence toMultilineMarkup(final CharSequence s) {
		if (s == null) {
			return null;
		}

		final AppendingStringBuffer buffer = new AppendingStringBuffer();

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);

			switch (c) {
			case '\n':
				buffer.append("<br/>");
				break;

			case '\r':
				break;

			default:
				buffer.append(c);
				break;
			}
		}
		return buffer;
	}
}
