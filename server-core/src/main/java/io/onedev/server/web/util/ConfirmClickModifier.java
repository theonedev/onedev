package io.onedev.server.web.util;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.AttributeModifier;

@SuppressWarnings("serial")
public class ConfirmClickModifier extends AttributeModifier {

	public ConfirmClickModifier(String message) {
		super("onclick", String.format("return !$(this).is('[disabled=disabled]') && confirm('%s');", StringEscapeUtils.escapeEcmaScript(message)));
	}

}
