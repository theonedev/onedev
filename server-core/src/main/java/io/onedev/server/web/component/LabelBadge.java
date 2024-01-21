package io.onedev.server.web.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.util.ColorUtils;

@SuppressWarnings("serial")
public class LabelBadge extends WebComponent {

	public LabelBadge(String id, IModel<LabelSpec> model) {
		super(id, model);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, getLabel().getName());
	}
	
	private LabelSpec getLabel() {
		return (LabelSpec) getDefaultModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		String backgroundColor = getLabel().getColor();
		if (backgroundColor == null)
			backgroundColor = "#E4E6EF";
		String fontColor = ColorUtils.isLight(backgroundColor)?"#3F4254":"white"; 
		String style = String.format(
				"background-color: %s; color: %s;", 
				backgroundColor, fontColor);
		add(AttributeAppender.append("style", style));
		add(AttributeAppender.append("class", "badge badge-sm"));
	}
	
}
