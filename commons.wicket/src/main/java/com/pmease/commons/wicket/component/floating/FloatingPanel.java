package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.align.AlignResourceReference;

@SuppressWarnings("serial")
public abstract class FloatingPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private final AlignWith alignWith;
	
	private final Alignment alignment;
	
	public FloatingPanel(AjaxRequestTarget target, AlignWith alignWith, Alignment alignment) {
		this(target, null, alignWith, alignment);
	}

	public FloatingPanel(AjaxRequestTarget target, IModel<?> model, 
			AlignWith alignWith, Alignment alignment) {
		super(((CommonPage)target.getPage()).getStandalones().newChildId(), model);
		
		CommonPage page = (CommonPage) target.getPage(); 
		page.getStandalones().add(this);
		target.prependJavaScript(String.format("$('body').append(\"<div id='%s'></div>\");", getMarkupId()));
		target.add(this);

		this.alignWith = alignWith;
		this.alignment = alignment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent(CONTENT_ID));
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				FloatingPanel.this.remove();
				onClosed(target);
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(FloatingPanel.class, "floating.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(FloatingPanel.class, "floating.css")));
				response.render(JavaScriptHeaderItem.forReference(AlignResourceReference.INSTANCE));
				
				ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
				String alignmentJson;
				try {
					alignmentJson = mapper.writeValueAsString(alignment);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				
				String script = String.format("pmease.commons.floating.init('%s', %s, %s, %s);", 
						getMarkupId(true), alignWith.toJSON(), 
						alignmentJson, getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		
		add(AttributeAppender.append("class", "floating"));
		setOutputMarkupId(true);
	}
	
	protected abstract Component newContent(String id);

	public Component getContent() {
		return get(CONTENT_ID); 
	}
	
	public final void close(AjaxRequestTarget target) {
		String script = String.format("pmease.commons.floating.close($('#%s'), false);", getMarkupId(true));
		target.appendJavaScript(script);
		
		remove();
		
		onClosed(target);
	}
	
	protected void onClosed(AjaxRequestTarget target) {
		
	}
}