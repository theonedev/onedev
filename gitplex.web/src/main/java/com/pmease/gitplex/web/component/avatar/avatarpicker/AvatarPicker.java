package com.pmease.gitplex.web.component.avatar.avatarpicker;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;

@SuppressWarnings("serial")
public class AvatarPicker extends FormComponentPanel<FileUpload> {

	private static final int MAX_IMAGE_SIZE = 2; // In megabytes
	
	private final IModel<User> userModel;
	
	private BootstrapFileInputField uploadField;
	
	public AvatarPicker(String id, IModel<User> userModel, IModel<FileUpload> model) {
		super(id, model);
		
		this.userModel = userModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getForm().setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		getForm().setMultiPart(true);
		
		add(new AvatarByUser("currentAvatar", userModel, false));
		
		add(uploadField = new BootstrapFileInputField("fileInput") {

			@Override
			protected AjaxFormSubmitBehavior newAjaxFormSubmitBehavior(String event) {
		        
				return new AjaxFormSubmitBehavior(getForm(), event) {
					
		            @Override
		            protected void onSubmit(AjaxRequestTarget target) {
		            	GitPlex.getInstance(AvatarManager.class).useAvatar(userModel.getObject(), uploadField.getFileUpload());
		            	success("Avatar has been changed");
		                target.add(getForm());
		            }

					@Override
					protected void onError(AjaxRequestTarget target) {
						super.onError(target);
						target.add(getForm());
					}
		            
		        };
		        
			}
			
		});
		add(new AjaxLink<Void>("reset") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(userModel.getObject().getAvatarUploadDate() != null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(AvatarManager.class).useAvatar(userModel.getObject(), null);
				target.add(AvatarPicker.this);
				Session.get().success("Avatar has been reset");
			}
			
		});
		
		add(new Label("maxSize", MAX_IMAGE_SIZE));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void convertInput() {
		setConvertedInput(uploadField.getFileUpload());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AvatarPicker.class, "avatar-picker.css")));
	}
	
}
