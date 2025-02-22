package io.onedev.server.web.component.imagedata.upload;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ImageUploadField extends FormComponentPanel<String> {
	
	private final String accept;
	
	private final int width;
	
	private final int height;
	
	private final String backgroundColor;
	
	private TextField<String> dataField;

	private AbstractPostAjaxBehavior behavior;
	
	public ImageUploadField(String id, IModel<String> model, String accept, 
							int width, int height, String backgroundColor) {
		super(id, model);
		this.accept = accept;
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
	}

	public ImageUploadField(String id, IModel<String> model) {
		this(id, model, "image/*", 128, 128, "white");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(dataField = new TextField<>("data", Model.of(getModelObject())));
		
		WebComponent fileInput = new WebComponent("fileInput");
		fileInput.add(AttributeAppender.append("accept", accept));
		fileInput.setOutputMarkupId(true);
		add(fileInput);
		
		add(new WebMarkupContainer("fileLabel") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("for", fileInput.getMarkupId());
			}
			
		});

		var style = String.format("width: %dpx; height: %dpx; background-color: %s;",
				width, height, backgroundColor);

		var image = new WebMarkupContainer("image");
		image.add(AttributeAppender.append("style", style));
		add(image);

		add(behavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				onImageUpdating(target);
			}

		});
	}

	@Override
	public void convertInput() {
		setConvertedInput(dataField.getConvertedInput());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ImageUploadFieldResourceReference()));
		
		String script = String.format("onedev.server.imageUpload.onDomReady('%s', %s);", 
				getMarkupId(), behavior.getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected void onImageUpdating(AjaxRequestTarget target) {
		
	}
}
