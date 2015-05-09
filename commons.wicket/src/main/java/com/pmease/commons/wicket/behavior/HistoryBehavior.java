package com.pmease.commons.wicket.behavior;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@SuppressWarnings("serial")
public abstract class HistoryBehavior extends AbstractDefaultAjaxBehavior {

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(Method.POST);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		String encodedState = RequestCycle.get().getRequest().getPostParameters()
				.getParameterValue("state").toOptionalString();
		byte[] bytes = Base64.decodeBase64(encodedState.getBytes());
		Serializable state = (Serializable) SerializationUtils.deserialize(bytes);
		onPopState(target, state);
	}
	
	protected abstract void onPopState(AjaxRequestTarget target, Serializable state);

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("historyjs/current/scripts/bundled/html5/jquery.history.js")));

		// Use history.js instead of native history API to solve the problem that Safari 
		// (and previous versions of Chrome) fires event "onpopstate" on initial page load 
		// and this causes the page to reload infinitely with below code  
		String script = String.format(""
				+ "History.Adapter.bind(window, 'statechange', function() {"
				+ "  if (History.getState().data.component == null)"
				+ "    location.reload();"
				+ "});"); 
		response.render(JavaScriptHeaderItem.forScript(script, "reload_on_state_change"));

		script = String.format(""
				+ "History.Adapter.bind(window, 'statechange', function() {"
				+ "  if (History.getState().data.component === '%s') {"
				+ "    %s"
				+ "  }"
				+ "});", 
				getComponent().getPageRelativePath(), 
				getCallbackFunctionBody(CallbackParameter.resolved("state", "History.getState().data.state"))); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	public void pushState(AjaxRequestTarget target, String url, Serializable state) {
		String encodedState = new String(Base64.encodeBase64(SerializationUtils.serialize(state)));
		target.appendJavaScript(String.format("History.pushState({state:'%s', component:'%s'}, '', '%s');", 
				encodedState, getComponent().getPageRelativePath(), url));
	}

}
