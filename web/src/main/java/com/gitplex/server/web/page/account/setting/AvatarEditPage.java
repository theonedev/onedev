package com.gitplex.server.web.page.account.setting;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Account;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.web.component.avatar.AvatarChanged;
import com.gitplex.server.web.util.avatar.AvatarManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class AvatarEditPage extends AccountSettingPage {

	private static final int MAX_IMAGE_SIZE = 2*1024*1024;
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

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
					AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
	            	avatarManager.useAvatar(accountModel.getObject(), upload);
					form.success("Avatar has been updated.");
					send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, accountModel.getObject()));								
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
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				setVisible(avatarManager.getUploaded(accountModel.getObject()).exists());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				avatarManager.useAvatar(accountModel.getObject(), null);
				form.success("Avatar has been reset");
				target.add(form);
				send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, accountModel.getObject()));								
			}
			
		});
		
		add(new Label("maxSize", MAX_IMAGE_SIZE));
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new AvatarEditResourceReference()));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AvatarEditPage.class, paramsOf(account));
	}
	
}
