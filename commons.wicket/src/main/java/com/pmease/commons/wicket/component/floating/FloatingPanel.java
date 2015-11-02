package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class FloatingPanel extends Panel {

	private final Component target;
	
	private final Alignment alignment;
	
	public FloatingPanel(String id, Component target, Alignment alignment) {
		super(id);
		
		this.target = target;
		this.alignment = alignment;
	}

	public FloatingPanel(String id, IModel<?> model, Component target, Alignment alignment) {
		super(id, model);
		
		this.target = target;
		this.alignment = alignment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content"));
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				FloatingPanel.this.remove();
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
				String alignmentJson;
				try {
					alignmentJson = mapper.writeValueAsString(alignment);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				
				String script = String.format("pmease.commons.floating.init('%s', '%s', %s, %s);", 
						getMarkupId(true), target.getMarkupId(true), alignmentJson, getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
	}
	
	protected abstract Component newContent(String id);

}