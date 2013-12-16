package com.pmease.commons.wicket.behavior.collapse;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.pmease.commons.wicket.behavior.modal.ModalResourceReference;

/**
 * Add collapsible components to this panel to achieve the accordion 
 * effect. Collapsible components are those referenced as target in 
 * {@link CollapseBehavior}.
 * 
 * @see CollapseBehavior
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class AccordionPanel extends WebMarkupContainer {

	List<CollapsibleBehavior> collapsibles;
	
	public AccordionPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		if (collapsibles == null) {
			collapsibles = new ArrayList<>();
			
			List<CollapsibleBehavior> behaviors = getBehaviors(CollapsibleBehavior.class);
			if (!getBehaviors(CollapsibleBehavior.class).isEmpty()) {
				collapsibles.addAll(behaviors);
			} else {
				visitChildren(Component.class, new IVisitor<Component, Component>() {
	
					@Override
					public void component(Component component, IVisit<Component> visit) {
						List<CollapsibleBehavior> behaviors = component.getBehaviors(CollapsibleBehavior.class);
						if (!behaviors.isEmpty()) {
							collapsibles.addAll(behaviors);
							visit.dontGoDeeper();
						} else if (component instanceof AccordionPanel) {
							visit.dontGoDeeper();
						}
					}
					
				});
			}
			if (!collapsibles.isEmpty()) {
				CollapsibleBehavior behavior = collapsibles.get(0);
				behavior.target.add(AttributeAppender.append("class", "in"));
				behavior.trigger.add(AttributeAppender.append("class", "expanded"));
			}
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(ModalResourceReference.get()));
	}
	
}
