package com.pmease.commons.wicket.component;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class LabelLink<T> extends Link<T> {

	private final IModel<String> labelModel;

	public LabelLink(String id, IModel<String> labelModel) {
		this(id, null, labelModel);
	}

	public LabelLink(String id, IModel<T> linkModel, IModel<String> labelModel) {
		super(id, linkModel);
		this.labelModel = labelModel;
	}
	
	@Override
	public IModel<?> getBody() {
		return labelModel;
	}

	@Override
	public void onDetach() {
		labelModel.detach();
		
		super.onDetach();
	}
}
