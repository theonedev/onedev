package com.turbodev.server.web.page.user;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import com.turbodev.server.TurboDev;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.avatar.Avatar;
import com.turbodev.server.web.component.avatar.AvatarChanged;
import com.turbodev.server.web.component.dropzonefield.DropzoneField;
import com.turbodev.server.web.util.avatar.AvatarManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class AvatarEditPage extends UserPage {

	private static final int MAX_IMAGE_SIZE = 5;
	
	private Collection<FileUpload> uploads;
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Avatar("avatar", getUser()));
		
		Form<?> form = new Form<Void>("form");
		add(form);
		
		form.add(new NotificationPanel("feedback", form));
		form.setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		form.setOutputMarkupId(true);

		form.setMultiPart(true);
		
		IModel<Collection<FileUpload>> model = new PropertyModel<>(this, "uploads");
		
		DropzoneField dropzoneField = new DropzoneField("file", model, "image/*", 1, MAX_IMAGE_SIZE);
		dropzoneField.setRequired(true);
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
				AvatarManager avatarManager = TurboDev.getInstance(AvatarManager.class);
            	avatarManager.useAvatar(userModel.getObject(), upload);
				send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, userModel.getObject()));	
				target.add(form);
			}
			
		});
		
		form.add(new AjaxLink<Void>("useDefault") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				AvatarManager avatarManager = TurboDev.getInstance(AvatarManager.class);
				setVisible(avatarManager.getUploaded(userModel.getObject().getFacade()).exists());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				AvatarManager avatarManager = TurboDev.getInstance(AvatarManager.class);
				avatarManager.useAvatar(userModel.getObject(), null);
				target.add(form);
				send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, userModel.getObject()));								
			}
			
		});
		
		add(new Label("maxSize", MAX_IMAGE_SIZE));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getUser());
	}
	
}
