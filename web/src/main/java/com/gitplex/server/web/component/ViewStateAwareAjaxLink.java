package com.gitplex.server.web.component;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public abstract class ViewStateAwareAjaxLink<T> extends PreventDefaultAjaxLink<T> {

	public ViewStateAwareAjaxLink(String id) {
		super(id);
	}

	public ViewStateAwareAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(Method.POST);
		
		String script = ""
				+ "$('.autofit:visible').first().trigger('storeViewState');"
				+ "var viewState = gitplex.server.history.getViewState();"
				+ "return viewState?JSON.stringify(viewState):'';";
		attributes.getDynamicExtraParameters().add("return {view_state: function() {" + script + "}}");
	}

	@Override
	public final void onClick(AjaxRequestTarget target) {
		String value = RequestCycle.get().getRequest().getPostParameters()
				.getParameterValue("view_state").toString();
		if (StringUtils.isNotBlank(value))
			onClick(target, value);
		else 
			onClick(target, null);
	}

	protected abstract void onClick(AjaxRequestTarget target, @Nullable String viewState);
}
