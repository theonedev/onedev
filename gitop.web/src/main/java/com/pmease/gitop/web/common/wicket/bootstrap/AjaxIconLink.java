package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class AjaxIconLink extends Panel {

	private static final long serialVersionUID = 1L;

	private final IModel<String> iconModel;
	private final IModel<String> labelModel;

	public AjaxIconLink(String id, IModel<String> iconModel) {
		this(id, iconModel, Model.of(""));
	}

	public AjaxIconLink(String id, IModel<String> iconModel,
			IModel<String> labelModel) {
		super(id);
		this.iconModel = iconModel;
		this.labelModel = labelModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		AjaxLink<?> link = createLink("link");
		add(link);
		link.add(new Icon("icon", iconModel));
		link.add(new Label("label", labelModel));
	}

	@Override
	protected void onDetach() {
		super.onDetach();

		if (iconModel != null) {
			iconModel.detach();
		}

		if (labelModel != null) {
			labelModel.detach();
		}
	}

	abstract protected AjaxLink<?> createLink(String id);
}