package io.onedev.server.web.page.project.builds.detail;

import static io.onedev.server.web.translation.Translation._T;

import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.upload.FileUpload;
import io.onedev.server.web.upload.UploadService;

public abstract class ArtifactUploadPanel extends Panel {

	private String directory;
	
	private String uploadId;
	
	public ArtifactUploadPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SettingService settingService = OneDev.getInstance(SettingService.class);
		int maxUploadFileSize = settingService.getPerformanceSetting().getMaxUploadFileSize();
		
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
		dropzone.setRequired(true).setLabel(Model.of(_T("File")));
		form.add(dropzone);
		
		form.add(new AjaxButton("upload") {

			private String getArtifactPath(FileItem file) {
				String artifactPath = FilenameUtils.sanitizeFileName(FileUpload.getFileName(file));
				if (directory != null)
					artifactPath = directory + "/" + artifactPath;
				return artifactPath;
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (directory != null && directory.contains("..")) {
					error(_T("'..' is not allowed in the directory"));
					target.add(feedback);
				} else {
					Long projectId = getBuild().getProject().getId();
					Long buildNumber = getBuild().getNumber();
					var upload = getUploadService().getUpload(uploadId);
					try {
						for (var item : upload.getItems()) {
							try (var is = item.getInputStream()) {
								getBuildService().uploadArtifact(projectId, buildNumber, getArtifactPath(item), is);
							}
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						upload.clear();
					}
					
					onUploaded(target);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}
			
		});
		
		form.add(new TextField<String>("directory", new PropertyModel<String>(this, "directory")));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}
	
	private UploadService getUploadService() {
		return OneDev.getInstance(UploadService.class);
	}

	private BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}

	public abstract void onUploaded(AjaxRequestTarget target);
	
	public abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract Build getBuild();
	
}
