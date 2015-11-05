package com.pmease.commons.wicket;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.AttributeModifier;

@SuppressWarnings("serial")
public class ConfirmOnClick extends AttributeModifier {

	public ConfirmOnClick(String message) {
		super("onclick", String.format("return confirm('%s');", StringEscapeUtils.escapeEcmaScript(message)));
	}

}
