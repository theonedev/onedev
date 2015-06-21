package com.pmease.commons.wicket.behavior;

import static org.apache.wicket.ajax.attributes.CallbackParameter.resolved;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public abstract class HistoryBehavior extends AbstractDefaultAjaxBehavior {

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(Method.POST);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		String encodedState = params.getParameterValue("state").toString();
		
		byte[] bytes = Base64.decodeBase64(encodedState.getBytes());
		Serializable state = (Serializable) SerializationUtils.deserialize(bytes);
		onPopState(target, state);
	}
	
	protected abstract void onPopState(AjaxRequestTarget target, Serializable state);

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		String script = String.format(""
				+ "$(document).on('onpopstate', function(event, component, state) {"
				+ "  if (component === '%s') {"
				+ "    %s"
				+ "  }"
				+ "});",
				getComponent().getPageRelativePath(), 
				getCallbackFunctionBody(resolved("state", "state"))); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	public void pushState(AjaxRequestTarget target, String url, Serializable state) {
		String encodedState = new String(Base64.encodeBase64(SerializationUtils.serialize(state)));
		target.prependJavaScript(String.format("history.pushState({state:'%s', component:'%s'}, '', '%s');", 
				encodedState, getComponent().getPageRelativePath(), url));
	}

}
