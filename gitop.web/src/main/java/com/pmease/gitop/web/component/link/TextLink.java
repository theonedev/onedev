package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class TextLink<T> extends Link<T> {

	private final IModel<String> nameModel;

	public TextLink(String id, IModel<String> nameModel) {
		this(id, null, nameModel);
	}

	public TextLink(String id, IModel<T> model, IModel<String> nameModel) {
		super(id, model);
		this.nameModel = nameModel;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag) {

		// Draw anything before the body?
		if (!isLinkEnabled() && (getBeforeDisabledLink() != null)) {
			getResponse().write(getBeforeDisabledLink());
		}
		
		replaceComponentTagBody(markupStream, openTag, nameModel.getObject());

		// Draw anything after the body?
		if (!isLinkEnabled() && (getAfterDisabledLink() != null)) {
			getResponse().write(getAfterDisabledLink());
		}
	}
	
	@Override
	public void onDetach() {
		if (nameModel != null) {
			nameModel.detach();
		}
		
		super.onDetach();
	}
}
