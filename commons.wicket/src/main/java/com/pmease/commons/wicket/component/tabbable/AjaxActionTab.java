package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public abstract class AjaxActionTab extends ActionTab {

	public AjaxActionTab(IModel<String> titleModel) {
		super(titleModel);
	}

	@Override
	public Component render(String componentId) {
		return new ActionTabLink(componentId, this) {

			@Override
			protected WebMarkupContainer newLink(String id, ActionTab tab) {
				return new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectTab(this);
					}
					
				};
			}
			
		};
	}

	@Override
	protected final void onSelect() {
		AjaxRequestTarget target = (AjaxRequestTarget) RequestCycle.get().getRequestHandlerScheduledAfterCurrent();
		onSelect(target);
	}

	protected abstract void onSelect(AjaxRequestTarget target);
}
