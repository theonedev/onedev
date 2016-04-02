package com.pmease.commons.wicket.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.util.Range;
import com.pmease.commons.util.StringUtils;

@SuppressWarnings("serial")
public class EmphasizeAwareLabel extends Label {

	public EmphasizeAwareLabel(String id, String label, Range emphasize) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (emphasize != null) {
					String prefix = label.substring(0, emphasize.getFrom());
					String middle = label.substring(emphasize.getFrom(), emphasize.getTo());
					String suffix = label.substring(emphasize.getTo());
					return StringUtils.escapeHtml(prefix) 
							+ "<b>" 
							+ StringUtils.escapeHtml(middle) 
							+ "</b>" 
							+ StringUtils.escapeHtml(suffix);
				} else {
					return StringUtils.escapeHtml(label);
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
