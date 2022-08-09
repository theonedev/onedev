package io.onedev.server.web.page.project.builds.detail.artifacts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.compress.utils.IOUtils;
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

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.util.FileUpload;

@SuppressWarnings("serial")
public abstract class ArtifactUploadPanel extends Panel {

	private String directory;
	
	private final Collection<FileUpload> uploads = new ArrayList<>();
	
	public ArtifactUploadPanel(String id) {
		super(id);
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
				new PropertyModel<Collection<FileUpload>>(this, "uploads"), 
				null, 0, maxUploadFileSize);
		dropzone.setRequired(true).setLabel(Model.of("File"));
		form.add(dropzone);
		
		form.add(new AjaxButton("upload") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (directory.contains("..")) {
					error("'..' is not allowed in the directory");
					target.add(feedback);
				} else {
					LockUtils.write(getBuild().getArtifactsLockKey(), new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							File artifactsDir = getBuild().getArtifactsDir();
							for (FileUpload upload: uploads) {
								String filePath = FilenameUtils.sanitizeFilename(upload.getFileName());
								if (directory != null)
									filePath = directory + "/" + filePath;
								File file = new File(artifactsDir, filePath);
								FileUtils.createDir(file.getParentFile());
								try (	InputStream is = upload.getInputStream();
										OutputStream os = new FileOutputStream(file)) {
									IOUtils.copy(is, os);
								} finally {
									upload.release();
								}
							}
							return null;
						}
						
					});
					
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

	public abstract void onUploaded(AjaxRequestTarget target);
	
	public abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract Build getBuild();
	
}
