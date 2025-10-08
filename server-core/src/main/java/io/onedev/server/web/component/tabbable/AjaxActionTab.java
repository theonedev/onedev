package io.onedev.server.web.component.tabbable;

import com.google.common.base.Preconditions;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.jspecify.annotations.Nullable;

public abstract class AjaxActionTab extends ActionTab {

	public AjaxActionTab(IModel<String> titleModel, @Nullable IModel<String> iconModel) {
		super(titleModel, iconModel);
	}

	public AjaxActionTab(IModel<String> titleModel) {
		this(titleModel, null);
	}
	
	@Override
	public Component render(String componentId) {
		return new ActionTabHead(componentId, this) {

			@Override
			protected WebMarkupContainer newLink(String id, ActionTab tab) {
				return new AjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						AjaxActionTab.this.updateAjaxAttributes(attributes);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectTab(this);
					}
					
				};
			}
			
		};
	}
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		
	}
	
	@Override
	protected final void onSelect(Component tabLink) {
		AjaxRequestTarget target = Preconditions.checkNotNull(RequestCycle.get().find(AjaxRequestTarget.class));
		target.add(tabLink.findParent(Tabbable.class));
		onSelect(target, tabLink);
	}

	protected abstract void onSelect(AjaxRequestTarget target, Component tabLink);
}
