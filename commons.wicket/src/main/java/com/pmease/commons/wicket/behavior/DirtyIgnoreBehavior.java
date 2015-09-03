package com.pmease.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class DirtyIgnoreBehavior extends Behavior {

	private String $forms;
	
	private boolean decorated;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
		
		if ($forms == null) 
			$forms = String.format("$('#%s').closest('form.leave-confirm')", component.getMarkupId());
	}

	public DirtyIgnoreBehavior(String $forms) {
		this.$forms = $forms;
	}
	
	public DirtyIgnoreBehavior() {
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
						if (component.isEnabled()) {
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
			
							String decoratedScript = String.format("pmease.commons.form.markClean(%s);%s", 
									$forms, script);
							tag.put("onclick", decoratedScript);
						}
					}
					
				});
			} else {
				component.add(new Behavior() {

					@Override
					public void renderHead(Component component, IHeaderResponse response) {
						super.renderHead(component, response);

						if (component.isEnabled()) {
							String script = String.format("pmease.commons.form.removeDirty('%s', %s)", 
										component.getMarkupId(true), $forms);
							AjaxRequestTarget target = component.getRequestCycle().find(AjaxRequestTarget.class);
							if (target == null) 
								response.render(OnDomReadyHeaderItem.forScript(script));
							else
								target.appendJavaScript(script);
						}
					}
					
				});
			}
			
			decorated = true;
		}
	}

}
