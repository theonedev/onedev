package com.pmease.commons.wicket.behavior;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

@SuppressWarnings("serial")
public abstract class TypeAheadBehavior extends AbstractDefaultAjaxBehavior {

	@Override
	protected void respond(AjaxRequestTarget target) {
		String query = RequestCycle.get()
				.getRequest()
				.getQueryParameters()
				.getParameterValue("query")
				.toString();
		
		String jsonResponse = new JSONArray(getChoices(query)).toString();
		
		// schedule a request handler that will serve the JSON response
		TextRequestHandler jsonHandler = new TextRequestHandler("application/json", "UTF-8", jsonResponse);

		RequestCycle.get().replaceAllRequestHandlers(jsonHandler);		
	}

	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(BootstrapHeaderItem.get());
		
		String template = 
				"$('#%s').typeahead({\n" + 
				"	source: function(query, process) {\n" + 
				"		%s\n" + 
				"	}\n" + 
				"});";
		String script = String.format(
				template, 
				getComponent().getMarkupId(), 
				getCallbackFunctionBody(CallbackParameter.explicit("query")));
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		
		attributes.setWicketAjaxResponse(false);
		attributes.setDataType("json");
		
		attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
			
			@Override
			public CharSequence getSuccessHandler(Component component) {
				return new JsonFunction(
						"function(attrs, jqXHR, data, textStatus) {" +
						"	process(data);" +
						"}");
			}
			
			@Override
			public CharSequence getPrecondition(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getFailureHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getCompleteHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getBeforeHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getAfterHandler(Component component) {
				return null;
			}
		});
	}

	protected abstract Collection<String> getChoices(String query);
}
