package com.pmease.gitop.web.page.account.setting.profile;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.SitePaths;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmButton;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
import com.pmease.gitop.web.component.avatar.AvatarChanged;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountProfilePage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Your Profile";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PROFILE;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		IModel<User> userModel = new UserModel(getAccount());
		add(new ProfileForm("form", userModel));

		add(new AvatarImage("currentavatar", userModel));
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
			add(new TextFieldElement<String>("displayName", "Display Name",
					new PropertyModel<String>(model, "displayName"))
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
					Messenger.success("Account profile has been updated.")
							.run(target);
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

			add(new FeedbackPanel("feedback"));
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
						File dir = SitePaths.get().userAvatarDir(user);
						if (!dir.exists()) {
							try {
								FileUtils.forceMkdir(dir);
							} catch (IOException e) {
								throw Throwables.propagate(e);
							}
						}

						String filename = upload.getClientFileName();
						String ext = Files.getFileExtension(filename);
						File avatarFile;

						// delete old avatar file
						String avatarPath = user.getAvatarUrl();
						if (!Strings.isNullOrEmpty(avatarPath)
								&& !(avatarPath.startsWith("http") || avatarPath
										.startsWith("https"))) {
							avatarFile = new File(dir, user.getAvatarUrl());
							// TODO: lock avatarFile for write
							if (avatarFile.exists()) {
								try {
									FileUtils.forceDelete(avatarFile);
								} catch (IOException e) {
									throw Throwables.propagate(e);
								}
							}
						}

						avatarFile = new File(dir, "avatar." + ext);
						try {
							upload.writeTo(avatarFile);
						} catch (IOException e) {
							throw Throwables.propagate(e);
						}

						user.setAvatarUrl(avatarFile.getName());
						AppLoader.getInstance(UserManager.class).save(user);

						send(getPage(), Broadcast.BREADTH, new AvatarChanged(target));
						target.add(form);
						Messenger.success("Your avatar has been changed!").run(target);;
					}
				}
			});
			
			add(new AjaxConfirmButton("remove", this, 
					Model.of("Are you sure you want to use the default avatar?"),
					null, Model.of("Yes"), Model.of("No"), null) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					User user = getUser();
			        user.setAvatarUrl(null);
			        AppLoader.getInstance(UserManager.class).save(user);
			        
			        send(getPage(), Broadcast.BREADTH, new AvatarChanged(target));
			        Messenger.success("Your avatar has been reset to the default.").run(target);
			        target.add(form);
				}
			});
		}
	}
}
