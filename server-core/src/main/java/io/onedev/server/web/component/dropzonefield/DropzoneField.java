package io.onedev.server.web.component.dropzonefield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Bytes;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
public class DropzoneField extends FormComponentPanel<Collection<FileUpload>> {

	private final int maxFiles;
	
	private final int maxFilesize;
	
	private final String acceptedFiles;
	
	private AbstractPostAjaxBehavior uploadBehavior;
	
	private AbstractPostAjaxBehavior deleteBehavior;
	
	private List<FileItem> fileItems = new ArrayList<>();
	
	/**
	 * @param id
	 * 			id of the component
	 * @param model
	 * 			model of the component
	 * @param acceptedFiles
	 * 			accepted mime types, for instance: images/*. Use <tt>null</tt> for unlimited
	 * @param maxFiles
	 * 			max number of files, use <tt>0</tt> for unlimited
	 * @param maxFilesize
	 * 			max file size in MB
	 */
	public DropzoneField(String id, IModel<Collection<FileUpload>> model, @Nullable String acceptedFiles, 
			int maxFiles, int maxFilesize) {
		super(id, model);
		this.acceptedFiles = acceptedFiles;
		this.maxFiles = maxFiles;
		this.maxFilesize = maxFilesize;
	}

	@Override
	protected void onBeforeRender() {
		// In order to be consistent with browser side
		fileItems.clear();
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(uploadBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
	            try {
	                ServletWebRequest webRequest = (ServletWebRequest) getRequest();
	                MultipartServletWebRequest multiPartRequest = webRequest.newMultipartWebRequest(
	                    Bytes.megabytes(maxFilesize), "ignored");
	                multiPartRequest.parseFileParts();
	                fileItems.addAll(multiPartRequest.getFiles().get("file"));
	            } catch (FileUploadException e) {
	            	throw new RuntimeException(e);
	            }
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMultipart(true);
			}
			
		});
		
		add(deleteBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String fileName = params.getParameterValue("name").toString();
				for (Iterator<FileItem> it = fileItems.iterator(); it.hasNext();) {
					FileItem fileItem = it.next();
					if (fileItem.getName().equals(fileName)) {
						fileItem.delete();
						it.remove();
					}
				}
			}

		});
		
	}

	@Override
	public void convertInput() {
		if (fileItems.isEmpty()) {
			setConvertedInput(null);
		} else {
			Collection<FileUpload> uploads = new ArrayList<>();
			for (FileItem fileItem: fileItems) {
				uploads.add(new FileUpload(fileItem));
			}
			setConvertedInput(uploads);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new DropzoneFieldResourceReference()));
		
		String script = String.format(
				"onedev.server.dropzone.onDomReady('%s', '%s', %s, %s, %s, %d);", 
				getMarkupId(), 
				uploadBehavior.getCallbackUrl(), 
				deleteBehavior.getCallbackFunction(CallbackParameter.explicit("name")),
				acceptedFiles!=null?"'" + acceptedFiles + "'":"null",				
				maxFiles!=0?maxFiles:"null",
				maxFilesize);
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
