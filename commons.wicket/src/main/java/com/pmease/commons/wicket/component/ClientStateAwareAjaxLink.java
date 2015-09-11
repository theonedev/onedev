package com.pmease.commons.wicket.component;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public abstract class ClientStateAwareAjaxLink<T> extends AjaxLink<T> {

	public ClientStateAwareAjaxLink(String id) {
		super(id);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(Method.POST);
		
		String script = String.format(""
				+ "var clientState = $('#%s').data('client_state');"
				+ "return clientState?JSON.stringify(clientState):'';", getMarkupId());
		attributes.getDynamicExtraParameters().add("return {client_state: function() {" + script + "}}");
	}

	public ClientStateAwareAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public final void onClick(AjaxRequestTarget target) {
		String value = RequestCycle.get().getRequest().getRequestParameters()
				.getParameterValue("client_state").toString();
		if (StringUtils.isNotBlank(value))
			onClick(target, value);
		else 
			onClick(target, null);
	}

	protected abstract void onClick(AjaxRequestTarget target, @Nullable String clientState);
}
