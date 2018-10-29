package io.onedev.server.web.page.project.setting.avatar;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.project.avatar.ProjectAvatarChanged;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class AvatarEditPage extends ProjectSettingPage {

	private static final int MAX_IMAGE_SIZE = 5;
	
	private Collection<FileUpload> uploads;
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ProjectAvatar("avatar", getProject()));
		
		Form<?> form = new Form<Void>("form");
		add(form);
		
		form.add(new NotificationPanel("feedback", form));
		form.setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		form.setOutputMarkupId(true);

		form.setMultiPart(true);
		
		IModel<Collection<FileUpload>> model = new PropertyModel<>(this, "uploads");
		
		DropzoneField dropzoneField = new DropzoneField("file", model, "image/*", 1, MAX_IMAGE_SIZE);
		dropzoneField.setRequired(true).setLabel(Model.of("Image"));
		form.add(dropzoneField);
		
		form.add(new AjaxButton("useUploaded") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				FileUpload upload = uploads.iterator().next();
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
            	avatarManager.useAvatar(projectModel.getObject().getFacade(), upload);
				send(getPage(), Broadcast.BREADTH, new ProjectAvatarChanged(target, projectModel.getObject()));	
				target.add(form);
			}
			
		});
		
		form.add(new AjaxLink<Void>("useDefault") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
				setVisible(avatarManager.getUploaded(projectModel.getObject().getFacade()).exists());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
				avatarManager.useAvatar(projectModel.getObject().getFacade(), null);
				target.add(form);
				send(getPage(), Broadcast.BREADTH, new ProjectAvatarChanged(target, projectModel.getObject()));								
			}
			
		});
		
		add(new Label("maxSize", MAX_IMAGE_SIZE));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAdministrate(getProject().getFacade());
	}
	
}
