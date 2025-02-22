package io.onedev.server.web.component.fileupload;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.svg.SpriteImage;

public class FileUploadField extends org.apache.wicket.markup.html.form.upload.FileUploadField {

	public FileUploadField(String id) {
		super(id);
	}
	
	public FileUploadField(final String id, IModel<? extends List<FileUpload>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "custom-fileinput"));
	}

	@Override
	protected void onDetach() {
		setConvertedInput(null);
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new FileUploadResourceReference()));
		String script = String.format("onedev.server.fileUpload.onDomReady('%s', '%s', '%s');", 
				getMarkupId(true), getHint(), SpriteImage.getVersionedHref(getIcon()));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected String getHint() {
		return "Select file...";
	}
	
	protected String getIcon() {
		return "file";
	}
	
}