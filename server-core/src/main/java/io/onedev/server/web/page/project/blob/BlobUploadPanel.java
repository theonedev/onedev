package io.onedev.server.web.page.project.blob;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

@SuppressWarnings("serial")
public abstract class BlobUploadPanel extends Panel {

	private final BlobRenderContext context;
	
	private String directory;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private final Collection<FileUpload> uploads = new ArrayList<>();
	
	public BlobUploadPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setMultiPart(true);
		form.setFileMaxSize(Bytes.megabytes(Project.MAX_UPLOAD_SIZE));
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
		
		form.add(new DropzoneField("files", 
				new PropertyModel<Collection<FileUpload>>(this, "uploads"), null, 0, Project.MAX_UPLOAD_SIZE)
				.setRequired(true).setLabel(Model.of("File")));
		form.add(new AjaxButton("upload") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				String commitMessage = summaryCommitMessage;
				if (StringUtils.isBlank(commitMessage))
					commitMessage = "Add files via upload";
				
				if (StringUtils.isNotBlank(detailCommitMessage))
					commitMessage += "\n\n" + detailCommitMessage;

				RefUpdated refUpdated = context.uploadFiles(uploads, directory, commitMessage);
				onCommitted(target, refUpdated);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}
			
		});
		
		form.add(new TextField<String>("directory", new PropertyModel<String>(this, "directory")));
		
		ReferenceInputBehavior behavior = new ReferenceInputBehavior(true) {
			
			@Override
			protected Project getProject() {
				return context.getProject();
			}
			
		};
		form.add(new TextField<String>("summaryCommitMessage", 
				new PropertyModel<String>(this, "summaryCommitMessage")).add(behavior));
		
		behavior = new ReferenceInputBehavior(true) {
			
			@Override
			protected Project getProject() {
				return context.getProject();
			}
			
		};
		form.add(new TextArea<String>("detailCommitMessage", 
				new PropertyModel<String>(this, "detailCommitMessage")).add(behavior));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}

	public abstract void onCommitted(AjaxRequestTarget target, RefUpdated refUpdated);
	
	public abstract void onCancel(AjaxRequestTarget target);
	
}
