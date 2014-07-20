package com.pmease.gitop.web.page.account.setting.profile;

import java.io.IOException;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import com.google.common.base.Throwables;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.form.textfield.TextFieldElement;
import com.pmease.gitop.web.component.user.AvatarByUser;
import com.pmease.gitop.web.component.user.AvatarChanged;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

import de.agilecoders.wicket.extensions.javascript.jasny.FileUploadField;

@SuppressWarnings("serial")
public class AccountProfilePage extends AccountSettingPage {

	public AccountProfilePage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected String getPageTitle() {
		return "Your Profile";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<User> userModel = new UserModel(getAccount());
		add(new ProfileForm("form", userModel));

		add(new AvatarByUser("currentavatar", userModel));
		add(new AvatarForm("avatarForm", userModel));
	}

	private class ProfileForm extends Form<User> {

		public ProfileForm(String id, IModel<User> model) {
			super(id, model);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			@SuppressWarnings("unchecked")
			final IModel<User> model = (IModel<User>) getDefaultModel();

			add(new FeedbackPanel("feedback", this));
			add(new TextFieldElement<String>("fullName", "Full Name",
					new PropertyModel<String>(model, "fullName"))
					.setRequired(false).add(new PropertyValidator<String>()));
			add(new TextFieldElement<String>("email", "Email Address",
					new PropertyModel<String>(model, "email"))
					.add(new PropertyValidator<String>()));

			add(new AjaxButton("submit", this) {
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(form);
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					User user = model.getObject();
					AppLoader.getInstance(UserManager.class).save(user);
					form.success("Account profile has been updated.");
					target.add(form);
				}
			});
		}
	}

	private class AvatarForm extends Form<User> {
		AvatarForm(String id, IModel<User> model) {
			super(id, model);
		}

		private User getUser() {
			return (User) getDefaultModelObject();
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			// limit avatar size to 2M bytes
			setMaxSize(Bytes.megabytes(2));
			setMultiPart(true);
			
			final FileUploadField uploadField = new FileUploadField("fileInput");
			uploadField.setRequired(false);
			
			add(uploadField);

			add(new FeedbackPanel("feedback", this));
			add(new AjaxButton("submit", this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					FileUpload upload = uploadField.getFileUpload();
					if (upload == null) {
						form.error("Please select an avatar file");
						target.add(form);
						return;
					}
					
					User user = getUser();
					if (upload != null) {
						try {
							upload.writeTo(user.getLocalAvatar());
							user.setAvatarUpdateDate(new Date());
							Gitop.getInstance(UserManager.class).save(user);
						} catch (IOException e) {
							throw Throwables.propagate(e);
						}

						send(getPage(), Broadcast.BREADTH, new AvatarChanged(target));
						form.success("Your avatar has been updated successfully");
						target.add(form);
					}
				}
			});
			
			add(new AjaxButton("remove", this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					User user = getUser();
					FileUtils.deleteFile(user.getLocalAvatar());
					user.setAvatarUpdateDate(null);
					Gitop.getInstance(UserManager.class).save(user);
			        
					send(getPage(), Broadcast.BREADTH, new AvatarChanged(target));
			        form.success("Your avatar has been reset to the default.");
			        target.add(form);
				}
			}.add(new ConfirmBehavior("Are you sure you want to use the default avatar?")));
		}
	}
}
