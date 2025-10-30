package io.onedev.server.web.component.avatarupload;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
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

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

public class AvatarUploadField extends FormComponentPanel<String> {

	private TextField<String> dataField;
	
	private AbstractPostAjaxBehavior behavior;
	
	public AvatarUploadField(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(dataField = new TextField<String>("data", Model.of(getModelObject())));
		
		WebComponent fileInput = new WebComponent("fileInput");
		fileInput.setOutputMarkupId(true);
		add(fileInput);
		
		add(new WebMarkupContainer("fileLabel") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("for", fileInput.getMarkupId());
			}
			
		});
		
		add(behavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				send(AvatarUploadField.this, Broadcast.BUBBLE, new AvatarFileSelected(target));	
			}
			
		});
	}

	@Override
	public void convertInput() {
		setConvertedInput(dataField.getConvertedInput());
	}
	
	public static void writeToFile(File file, String avatarData) {
		byte[] imageBytes = DatatypeConverter.parseBase64Binary(StringUtils.substringAfter(avatarData, ","));
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new AvatarUploadResourceReference()));
		
		String script = String.format("onedev.server.avatarUpload.onDomReady('%s', %s);", 
				getMarkupId(), behavior.getCallbackFunction());
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
