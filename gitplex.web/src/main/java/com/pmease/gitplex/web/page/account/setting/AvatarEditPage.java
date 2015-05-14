package com.pmease.gitplex.web.page.account.setting;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.component.avatar.AvatarPicker;

@SuppressWarnings("serial")
public class AvatarEditPage extends AccountSettingPage {

	private FileUpload upload;
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		sidebar.add(new Label("title", "Change Avatar of " + getAccount().getDisplayName()));
		sidebar.add(new FeedbackPanel("feedback"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GitPlex.getInstance(AvatarManager.class).useAvatar(getAccount(), upload);
				Session.get().info("Avatar has been updated");
				backToPrevPage();
			}
			
		};
		sidebar.add(form);
		
		form.add(new AvatarPicker("avatarPicker", accountModel, new IModel<FileUpload>() {

			@Override
			public void detach() {
			}

			@Override
			public FileUpload getObject() {
				return upload;
			}

			@Override
			public void setObject(FileUpload object) {
				upload = object;
			}
			
		}));
		form.add(new Link<Void>("cancel") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(prevPageRef != null);
			}

			@Override
			public void onClick() {
				backToPrevPage();
			}
			
		});
	}

}
