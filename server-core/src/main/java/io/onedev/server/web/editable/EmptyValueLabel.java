package io.onedev.server.web.editable;

import java.lang.reflect.AnnotatedElement;

import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@SuppressWarnings("serial")
public class EmptyValueLabel extends Label {

	public EmptyValueLabel(String id, AnnotatedElement element) {
		super(id, "<i>" + getNameOfEmptyValue(element) + "</i>");
		setEscapeModelStrings(false);
	}

	private static String getNameOfEmptyValue(AnnotatedElement element) {
		NameOfEmptyValue nameOfEmptyValue = element.getAnnotation(NameOfEmptyValue.class);
		if (nameOfEmptyValue != null)
			return HtmlEscape.escapeHtml5(nameOfEmptyValue.value());
		else
			return "Not defined";
	}
}
