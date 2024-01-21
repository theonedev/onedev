package io.onedev.server.web.page.project.blob;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.upload.UploadManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.eclipse.jgit.lib.ObjectId;

@SuppressWarnings("serial")
public abstract class BlobUploadPanel extends Panel {

	private final BlobRenderContext context;
	
	private String directory;
	
	private String commitMessage;
	
	private String uploadId;
	
	public BlobUploadPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		int maxUploadFileSize = OneDev.getInstance(SettingManager.class)
				.getPerformanceSetting().getMaxUploadFileSize();
		
		Form<?> form = new Form<Void>("form");
		form.setMultiPart(true);
		form.setFileMaxSize(Bytes.megabytes(maxUploadFileSize));
		add(form);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form);
		feedback.setOutputMarkupPlaceholderTag(true);
		form.add(feedback);
		
		DropzoneField dropzone = new DropzoneField(
				"files", 
				new PropertyModel<String>(this, "uploadId"), 
				null, 0, maxUploadFileSize);
		dropzone.setRequired(true).setLabel(Model.of("File"));
		form.add(dropzone);
		
		form.add(new AjaxButton("upload") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				String commitMessage = BlobUploadPanel.this.commitMessage;
				if (StringUtils.isBlank(commitMessage))
					commitMessage = "Add files via upload";
				
				var upload = getUploadManager().getUpload(uploadId);
				try {
					onCommitted(target, context.uploadFiles(upload, directory, commitMessage));
				} finally {
					upload.clear();
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}
			
		});
		
		form.add(new TextField<String>("directory", new PropertyModel<String>(this, "directory")));
		
		ReferenceInputBehavior behavior = new ReferenceInputBehavior() {
			
			@Override
			protected Project getProject() {
				return context.getProject();
			}
			
		};
		form.add(new TextArea<String>("commitMessage", 
				new PropertyModel<String>(this, "commitMessage")).add(behavior));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}
	
	private UploadManager getUploadManager() {
		return OneDev.getInstance(UploadManager.class);
	}

	public abstract void onCommitted(AjaxRequestTarget target, ObjectId commitId);
	
	public abstract void onCancel(AjaxRequestTarget target);
	
}
