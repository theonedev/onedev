package io.onedev.server.web.component.dropzonefield;

import io.onedev.server.OneDev;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.upload.FileUpload;
import io.onedev.server.web.upload.UploadManager;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Bytes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("serial")
public class DropzoneField extends FormComponentPanel<String> {

	private final int maxFiles;
	
	private final int maxFilesize;
	
	private final String acceptedFiles;

	private final String uploadId = UUID.randomUUID().toString();
	
	private AbstractPostAjaxBehavior uploadBehavior;
	
	private AbstractPostAjaxBehavior deleteBehavior;
	
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
	public DropzoneField(String id, IModel<String> model, @Nullable String acceptedFiles, 
			int maxFiles, int maxFilesize) {
		super(id, model);
		this.acceptedFiles = acceptedFiles;
		this.maxFiles = maxFiles;
		this.maxFilesize = maxFilesize;
	}

	private UploadManager getUploadManager() {
		return OneDev.getInstance(UploadManager.class);
	}
	
	@Override
	protected void onBeforeRender() {
		// In order to be consistent with browser side
		getUploadManager().clearUpload(uploadId);
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
					var upload = getUploadManager().getUpload(uploadId);
					if (upload == null) {
						upload = new FileUpload(uploadId, new ArrayList<>());
						getUploadManager().cacheUpload(upload);
					}
					upload.getItems().addAll(multiPartRequest.getFiles().get("file"));
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
				var upload = getUploadManager().getUpload(uploadId);
				if (upload != null) {
					for (var it = upload.getItems().iterator(); it.hasNext();) {
						var item = it.next();
						if (item.getName().equals(fileName)) {
							item.delete();
							it.remove();
						}
					}
				}
			}

		});
		
	}

	@Override
	public void convertInput() {
		var upload = getUploadManager().getUpload(uploadId);
		if (upload != null && !upload.getItems().isEmpty())
			setConvertedInput(uploadId);
		else
			setConvertedInput(null);
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
