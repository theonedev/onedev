package com.turbodev.server.web.page.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.utils.FileUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.avatar.Avatar;
import com.turbodev.server.web.component.avatar.AvatarChanged;
import com.turbodev.server.web.util.avatar.AvatarManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class AvatarEditPage extends UserPage {

	private static final int MAX_IMAGE_SIZE = 10*1024*1024;
	
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
		form.setOutputMarkupId(true);

		// for some reason, javascript reports cross origin iframe access error when file size
		// exceeds this limit, so we check at server side for the file size instead
		// form.setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		form.setMultiPart(true);
		
		FileUploadField uploadField = new FileUploadField("file");
		form.add(uploadField);
		
		uploadField.add(new AjaxFormSubmitBehavior("change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				FileUpload upload = uploadField.getFileUpload();
				if (upload.getSize() >= MAX_IMAGE_SIZE) {
				    form.error("Upload must be less than " + FileUtils.byteCountToDisplaySize(MAX_IMAGE_SIZE));
				} else {
					AvatarManager avatarManager = TurboDev.getInstance(AvatarManager.class);
	            	avatarManager.useAvatar(userModel.getObject(), upload);
					form.success("Avatar has been updated.");
					send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, userModel.getObject()));								
				}
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				super.onError(target);
				target.add(form);
			}
			
		});
		
		form.add(new AjaxLink<Void>("reset") {

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
				form.success("Avatar has been reset");
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
