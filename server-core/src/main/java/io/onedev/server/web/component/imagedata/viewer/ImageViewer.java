package io.onedev.server.web.component.imagedata.viewer;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

public class ImageViewer extends GenericPanel<String> {
	
	private final int width;
	
	private final int height;
	
	private final String backgroundColor;
	
	public ImageViewer(String id, IModel<String> model, int width, int height, String backgroundColor) {
		super(id, model);
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("img") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("src", getModelObject());
				var style = String.format("width: %dpx; height: %dpx; background-color: %s;", 
						width, height, backgroundColor);
				tag.put("style", style);
			}
		});
	}

}
