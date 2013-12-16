package com.pmease.commons.wicket.behavior.collapse;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

/**
 * Add this behavior to a component to have it able to show/hide another component 
 * upon click. The component to be collapsed will be hidden initially. Optionally 
 * you may add components (not necessarily to be direct children) to be collapsed 
 * to a {@link AccordionPanel} to achieve the accordion effect.    
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
		target.setOutputMarkupId(true);
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		target.add(new CollapsibleBehavior(component));
		component.add(AttributeAppender.append("class", "collapse-trigger"));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(CollapseResourceReference.get()));
		
		AccordionPanel accordion = target.visitParents(MarkupContainer.class, new IVisitor<MarkupContainer, AccordionPanel>() {

			@Override
			public void component(MarkupContainer object, IVisit<AccordionPanel> visit) {
				if (object instanceof AccordionPanel) {
					visit.stop((AccordionPanel) object);
				} else if (!object.getBehaviors(CollapsibleBehavior.class).isEmpty()) {
					visit.stop();
				}
			}
			
		});

		String script;
		if (accordion != null) {
			List<String> collapsibleIds = new ArrayList<>();
			for (CollapsibleBehavior each: accordion.collapsibles)
				collapsibleIds.add(each.target.getMarkupId());
			ObjectMapper objectMapper = AppLoader.getInstance(ObjectMapper.class);

			try {
				script = String.format("setupCollapse('%s', '%s', %s)", 
						component.getMarkupId(), target.getMarkupId(), 
						objectMapper.writeValueAsString(collapsibleIds));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			script = String.format("setupCollapse('%s', '%s', undefined)", 
					component.getMarkupId(), target.getMarkupId());
		}
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
