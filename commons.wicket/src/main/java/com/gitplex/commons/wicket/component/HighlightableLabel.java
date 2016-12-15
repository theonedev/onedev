package com.gitplex.commons.wicket.component;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.commons.util.Range;
import com.gitplex.commons.util.StringUtils;

@SuppressWarnings("serial")
public class HighlightableLabel extends Label {

	public HighlightableLabel(String id, @Nullable String label, @Nullable Range highlight) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (label != null) {
					if (highlight != null) {
						String prefix = label.substring(0, highlight.getFrom());
						String middle = label.substring(highlight.getFrom(), highlight.getTo());
						String suffix = label.substring(highlight.getTo());
						return StringUtils.escapeHtml(prefix) 
								+ "<b>" 
								+ StringUtils.escapeHtml(middle) 
								+ "</b>" 
								+ StringUtils.escapeHtml(suffix);
					} else {
						return StringUtils.escapeHtml(label);
					}
				} else {
					return "";
				}
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}

}
