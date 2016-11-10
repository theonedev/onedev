package com.gitplex.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.gitplex.commons.wicket.component.AccordionPanel;

/**
 * Add this behavior to a component to have it able to show/hide another component 
 * upon click. The component to be collapsed will be hidden initially. Note that 
 * if collapsible components are put inside a {@link AccordionPanel}, they will behave 
 * together like an accordion.
 * 
 * @see AccordionPanel
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class CollapseBehavior extends Behavior {
	
	private Component target;
	
	public CollapseBehavior(Component target) {
		this.target = target;
		target.add(AttributeAppender.append("class", "collapse"));
		target.setOutputMarkupId(true);
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		String script = String.format("gitplex.commons.setupCollapse('%s', '%s')", component.getMarkupId(), target.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
