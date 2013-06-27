package com.pmease.commons.web.behavior.confirm;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.web.behavior.modal.ModalResourceReference;

@SuppressWarnings("serial")
public class ConfirmBehavior extends Behavior {
	
	private IModel<String> messageModel;
	
	private boolean decorated;
	
	public ConfirmBehavior(IModel<String> messageModel) {
		this.messageModel = messageModel;
	}

	public ConfirmBehavior(String message) {
		this.messageModel = Model.of(message);
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new ModalResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(ConfirmBehavior.class, "confirm.js")));
	}
	
	@Override
	public void onConfigure(Component component) {
		super.onConfigure(component);
		
		if (!decorated) {

			// in order to make sure the behavior to modify onclick script is the last behavior
			// to be added.
			
			if (component.getBehaviors(AbstractAjaxBehavior.class).isEmpty()) {
				component.add(new Behavior() {
					
					@Override
					public void onComponentTag(Component component, ComponentTag tag) {
						super.onComponentTag(component, tag);
						String message = StringEscapeUtils.escapeEcmaScript(messageModel.getObject());
						CharSequence script = tag.getAttribute("onclick");
						if (script == null) {
							String href = tag.getAttribute("href");
							if (href != null) {
								script = "window.location.href='" + href + "';";
								tag.put("href", "javascript:void(0);");
							} else { 
								script = String.format("$('#%s').closest('form').submit();", component.getMarkupId());
							}
						}
		
						String decoratedScript = String.format(
								"confirmModal('%s', function() {%s}); return false;",
								message, script);
						decoratedScript = StringUtils.replace(decoratedScript, 
								"this.", "document.getElementById('" + component.getMarkupId() + "').");
						tag.put("onclick", decoratedScript);
					}
					
				});
			} else {
				component.add(new Behavior() {

					@Override
					public void renderHead(Component component, IHeaderResponse response) {
						super.renderHead(component, response);

						String message = StringEscapeUtils.escapeEcmaScript(messageModel.getObject());

						String script = String.format("setupConfirm('%s', '%s')", component.getMarkupId(), message);

						AjaxRequestTarget target = component.getRequestCycle().find(AjaxRequestTarget.class);
						if (target == null) 
							response.render(OnDomReadyHeaderItem.forScript(script));
						else
							target.appendJavaScript(script);
					}
					
				});
			}
			
			decorated = true;
		}
	}

	@Override
	public void detach(Component component) {
		messageModel.detach();
		super.detach(component);
	}
	
}
