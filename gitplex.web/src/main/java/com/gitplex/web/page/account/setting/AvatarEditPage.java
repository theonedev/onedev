package com.gitplex.web.page.account.setting;

import java.util.Map;

import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
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
import org.apache.wicket.util.lang.Bytes;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.web.avatar.AvatarManager;
import com.gitplex.web.component.avatar.AvatarChanged;
import com.gitplex.commons.util.FileUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class AvatarEditPage extends AccountSettingPage {

	private static final int MAX_IMAGE_SIZE = 2; // In megabytes
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final Form<?> form = new Form<Void>("form") {

			@Override
			protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
				if (e instanceof SizeLimitExceededException)
				    error("Upload must be less than " + FileUtils.byteCountToDisplaySize(getMaxSize().bytes()));
			}
			
		};
		add(form);
		form.add(new NotificationPanel("feedback", form));
		form.setOutputMarkupId(true);

		form.setMaxSize(Bytes.megabytes(MAX_IMAGE_SIZE));
		form.setMultiPart(true);
		
		final FileUploadField uploadField = new FileUploadField("file");
		form.add(uploadField);
		
		uploadField.add(new AjaxFormSubmitBehavior("change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				FileUpload upload = uploadField.getFileUpload();
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
            	avatarManager.useAvatar(accountModel.getObject(), upload);
				form.success("Avatar has been updated.");
				target.add(form);
				send(getPage(), Broadcast.BREADTH, new AvatarChanged(target, accountModel.getObject()));								
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
				setVisible(accountModel.getObject().getAvatarUploadDate() != null);
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
