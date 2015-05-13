package com.pmease.gitplex.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;

import com.pmease.gitplex.core.model.User;

import de.agilecoders.wicket.extensions.javascript.jasny.FileUploadField;

@SuppressWarnings("serial")
public class AvatarPicker extends FormComponentPanel<FileUpload> {

	private static final int MAX_IMAGE_SIZE = 2; // In megabytes
	
	private User user;
	
	private FileUploadField uploadField;
	
	public AvatarPicker(String id, User user, IModel<FileUpload> model) {
		super(id, model);
		
		this.user = user;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getForm().setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		getForm().setMultiPart(true);
		
		add(new AvatarByUser("currentAvatar", Model.of(user), false));
		
		add(uploadField = new FileUploadField("fileInput"));
		add(new AjaxLink<Void>("reset") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(user.getAvatarUploadDate() != null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				user.setAvatarUploadDate(null);
				target.add(AvatarPicker.this);
			}
			
		});
		
		add(new Label("maxSize", MAX_IMAGE_SIZE));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void convertInput() {
		setConvertedInput(uploadField.getFileUpload());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(AvatarPicker.class, "avatar.css")));
	}
	
}
