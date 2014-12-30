package com.pmease.commons.wicket.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class TextLink<T> extends Link<T> {

	private final IModel<String> labelModel;

	public TextLink(String id, IModel<String> labelModel) {
		this(id, null, labelModel);
	}

	public TextLink(String id, IModel<T> linkModel, IModel<String> labelModel) {
		super(id, linkModel);
		this.labelModel = labelModel;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag) {

		// Draw anything before the body?
		if (!isLinkEnabled() && (getBeforeDisabledLink() != null)) {
			getResponse().write(getBeforeDisabledLink());
		}
		
		replaceComponentTagBody(markupStream, openTag, labelModel.getObject());

		// Draw anything after the body?
		if (!isLinkEnabled() && (getAfterDisabledLink() != null)) {
			getResponse().write(getAfterDisabledLink());
		}
	}
	
	@Override
	public void onDetach() {
		if (labelModel != null) {
			labelModel.detach();
		}
		
		super.onDetach();
	}
}
