package io.onedev.server.web.editable;

import static io.onedev.server.web.translation.Translation._T;

import java.lang.reflect.AnnotatedElement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.util.ComponentContext;

public abstract class EmptyValueLabel extends Label {

	public EmptyValueLabel(String id) {
		super(id);
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				ComponentContext.push(new ComponentContext(EmptyValueLabel.this));
				try {
					String placeholder = EditableUtils.getPlaceholder(getElement());
					if (placeholder != null && EditableUtils.isDisplayPlaceholderAsValue(getElement()))
						return "<i>" + HtmlEscape.escapeHtml5(placeholder) + "</i>";
					else
						return "<i>" + _T("Unspecified") + "</i>";
				} finally {
					ComponentContext.pop();
				}
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}
	
	protected abstract AnnotatedElement getElement();
	
}
