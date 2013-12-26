package com.pmease.commons.wicket.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.pmease.commons.wicket.behavior.CollapseBehavior;

/**
 * Put collapsible components inside this accordion panel to have them behaving like 
 * accordion. Collapsible components are target components referenced via 
 * {@link CollapseBehavior}. 
 * 
 * @see CollapseBehavior
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class AccordionPanel extends WebMarkupContainer {

	public AccordionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		add(AttributeAppender.append("class", "accordion"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String script = String.format("pmease.commons.setupAccordion('%s')", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
